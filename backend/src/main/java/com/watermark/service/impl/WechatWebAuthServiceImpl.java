package com.watermark.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.watermark.model.Result;
import com.watermark.model.User;
import com.watermark.model.dto.UserVO;
import com.watermark.repository.UserRepository;
import com.watermark.service.WechatWebAuthService;
import com.watermark.util.JwtUtil;
import com.watermark.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatWebAuthServiceImpl implements WechatWebAuthService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final SnowflakeIdGenerator snowflake;
    private final PasswordEncoder passwordEncoder;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Value("${wechat.open.app-id:}") private String openAppId;
    @Value("${wechat.open.app-secret:}") private String openAppSecret;
    @Value("${wechat.open.redirect-uri:}") private String redirectUri;

    private static final String SCAN_STATUS_PREFIX = "wechat:scan:";
    private static final long QR_EXPIRE_SECONDS = 300; // 5分钟过期

    @Override
    public String generateQrUrl(String scene) {
        // 先在Redis中设置pending状态
        String key = SCAN_STATUS_PREFIX + scene;
        redisTemplate.opsForValue().set(key, "pending", QR_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 构造微信开放平台扫码登录URL
        // https://open.weixin.qq.com/connect/qrconnect?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=snsapi_login&state=STATE
        String encodedRedirectUri;
        try {
            encodedRedirectUri = java.net.URLEncoder.encode(redirectUri, "UTF-8");
        } catch (Exception e) {
            encodedRedirectUri = redirectUri;
        }
        return String.format(
            "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect",
            openAppId, encodedRedirectUri, scene
        );
    }

    @Override
    public Result<?> checkScanStatus(String scene) {
        String key = SCAN_STATUS_PREFIX + scene;
        String value = redisTemplate.opsForValue().get(key);
        Map<String, Object> result = new HashMap<>();

        if (value == null) {
            result.put("status", "expired");
            return Result.success(result);
        }

        if ("pending".equals(value)) {
            result.put("status", "pending");
            return Result.success(result);
        }

        if ("scanned".equals(value)) {
            result.put("status", "scanned");
            return Result.success(result);
        }

        // 已确认登录，value是JSON格式的用户数据
        if (value.startsWith("{")) {
            JSONObject json = JSON.parseObject(value);
            result.put("status", "confirmed");
            result.put("token", json.getString("token"));
            result.put("user", json.getJSONObject("user"));
            // 删除key，防止重复使用
            redisTemplate.delete(key);
            return Result.success(result);
        }

        result.put("status", "pending");
        return Result.success(result);
    }

    @Override
    public void handleCallback(String code, String state) throws Exception {
        String key = SCAN_STATUS_PREFIX + state;

        // 检查state是否有效
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            throw new Exception("二维码已过期，请重新获取");
        }

        // 标记为已扫码
        redisTemplate.opsForValue().set(key, "scanned", QR_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 获取access_token
        String tokenUrl = String.format(
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code",
            openAppId, openAppSecret, code
        );
        String tokenBody = httpGet(tokenUrl);
        JSONObject tokenJson = JSON.parseObject(tokenBody);

        if (tokenJson.containsKey("errcode")) {
            throw new Exception("微信授权失败：" + tokenJson.getString("errmsg"));
        }

        String accessToken = tokenJson.getString("access_token");
        String openId = tokenJson.getString("openid");
        String unionId = tokenJson.getString("unionid");

        // 获取用户信息
        String infoUrl = String.format(
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s&lang=zh_CN",
            accessToken, openId
        );
        String infoBody = httpGet(infoUrl);
        JSONObject infoJson = JSON.parseObject(infoBody);

        // 查找或创建用户
        User user = findOrCreateUser(openId, unionId, infoJson);

        // 生成JWT
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        UserVO userVO = UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .loginType(user.getLoginType())
                .token(token)
                .build();

        // 更新Redis状态为已确认
        JSONObject resultJson = new JSONObject();
        resultJson.put("token", token);
        resultJson.put("user", JSON.parseObject(JSON.toJSONString(userVO)));

        // 设置剩余过期时间
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl != null && ttl > 0) {
            redisTemplate.opsForValue().set(key, resultJson.toJSONString(), ttl, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, resultJson.toJSONString(), 60, TimeUnit.SECONDS);
        }

        log.info("微信网页授权登录成功: userId={}, openId={}", user.getId(), openId);
    }

    private User findOrCreateUser(String openId, String unionId, JSONObject infoJson) {
        Optional<User> opt;
        if (unionId != null) {
            opt = userRepo.findByWxUnionId(unionId);
        } else {
            opt = userRepo.findByWxWebOpenId(openId);
        }

        if (opt.isPresent()) {
            User user = opt.get();
            // 更新信息
            user.setWxWebOpenId(openId);
            if (unionId != null && user.getWxUnionId() == null) {
                user.setWxUnionId(unionId);
            }
            if (infoJson.getString("headimgurl") != null) {
                user.setAvatar(infoJson.getString("headimgurl"));
            }
            if (infoJson.getString("nickname") != null) {
                user.setNickname(infoJson.getString("nickname"));
            }
            return userRepo.save(user);
        }

        // 创建新用户
        User user = User.builder()
                .id(snowflake.nextId())
                .username(generateUsername())
                .password(passwordEncoder.encode("123456"))
                .wxWebOpenId(openId)
                .wxUnionId(unionId)
                .avatar(infoJson.getString("headimgurl"))
                .nickname(infoJson.getString("nickname"))
                .loginType("wechat")
                .build();
        return userRepo.save(user);
    }

    private String generateUsername() {
        String letters = "abcdefghijklmnopqrstuvwxyz";
        Random rand = new Random();
        for (int i = 0; i < 20; i++) {
            String username = "用户"
                    + letters.charAt(rand.nextInt(letters.length()))
                    + letters.charAt(rand.nextInt(letters.length()))
                    + String.format("%04d", rand.nextInt(10000));
            if (!userRepo.existsByUsername(username)) return username;
        }
        return "用户" + System.currentTimeMillis();
    }

    private String httpGet(String url) throws Exception {
        Request req = new Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0").build();
        try (Response resp = httpClient.newCall(req).execute()) {
            return resp.body().string();
        }
    }
}
