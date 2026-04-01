package com.watermark.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.watermark.model.Avatar;
import com.watermark.model.User;
import com.watermark.model.dto.*;
import com.watermark.repository.AvatarRepository;
import com.watermark.repository.UserRepository;
import com.watermark.service.AvatarService;
import com.watermark.service.SmsService;
import com.watermark.service.UserService;
import com.watermark.util.JwtUtil;
import com.watermark.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final AvatarRepository avatarRepository;
    private final AvatarService avatarService;
    private final SmsService smsService;
    private final JwtUtil jwtUtil;
    private final SnowflakeIdGenerator snowflake;
    private final PasswordEncoder passwordEncoder;
    private final OkHttpClient httpClient = new OkHttpClient();

    @Value("${wechat.miniprogram.app-id:}") private String mpAppId;
    @Value("${wechat.miniprogram.app-secret:}") private String mpAppSecret;
    @Value("${wechat.open.app-id:}") private String openAppId;
    @Value("${wechat.open.app-secret:}") private String openAppSecret;

    @Override
    public UserVO loginByPhone(LoginRequest req) throws Exception {
        if (!smsService.verifyCode(req.getPhone(), "login", req.getCode())) {
            throw new Exception("验证码错误或已过期");
        }
        Optional<User> opt = userRepo.findByPhone(req.getPhone());
        User user = opt.orElseGet(() -> createPhoneUser(req.getPhone()));
        return buildVO(user);
    }

    @Override
    public UserVO loginByWechatMiniprogram(LoginRequest req) throws Exception {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + mpAppId
                + "&secret=" + mpAppSecret + "&js_code=" + req.getCode() + "&grant_type=authorization_code";
        String body = httpGet(url);
        JSONObject json = JSON.parseObject(body);
        if (json.containsKey("errcode") && json.getIntValue("errcode") != 0) {
            throw new Exception("微信登录失败：" + json.getString("errmsg"));
        }
        String openId = json.getString("openid");
        String unionId = json.getString("unionid");
        Optional<User> opt = userRepo.findByWxOpenId(openId);
        User user;
        if (opt.isPresent()) {
            user = opt.get();
            if (unionId != null && user.getWxUnionId() == null) {
                user.setWxUnionId(unionId);
                userRepo.save(user);
            }
        } else {
            // 微信小程序新用户，分配随机头像
            Avatar randomAvatar = avatarService.getRandomAvatar();
            user = User.builder()
                    .id(snowflake.nextId())
                    .username(generateUsername())
                    .password(passwordEncoder.encode("123456"))
                    .wxOpenId(openId)
                    .wxUnionId(unionId)
                    .avatar(randomAvatar.getUrl())
                    .loginType("wechat")
                    .build();
            userRepo.save(user);
        }
        return buildVO(user);
    }

    @Override
    public UserVO loginByWechatWeb(LoginRequest req) throws Exception {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + openAppId
                + "&secret=" + openAppSecret + "&code=" + req.getCode()
                + "&grant_type=authorization_code";
        String body = httpGet(url);
        JSONObject json = JSON.parseObject(body);
        if (json.containsKey("errcode")) {
            throw new Exception("微信扫码登录失败：" + json.getString("errmsg"));
        }
        String openId = json.getString("openid");
        String unionId = json.getString("unionid");
        String accessToken = json.getString("access_token");
        String infoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + accessToken
                + "&openid=" + openId + "&lang=zh_CN";
        String infoBody = httpGet(infoUrl);
        JSONObject info = JSON.parseObject(infoBody);
        Optional<User> opt = unionId != null
                ? userRepo.findByWxUnionId(unionId)
                : userRepo.findByWxWebOpenId(openId);
        User user;
        if (opt.isPresent()) {
            user = opt.get();
            user.setWxWebOpenId(openId);
            if (unionId != null) user.setWxUnionId(unionId);
            if (info.getString("headimgurl") != null) user.setAvatar(info.getString("headimgurl"));
            if (info.getString("nickname") != null) user.setNickname(info.getString("nickname"));
            userRepo.save(user);
        } else {
            user = User.builder()
                    .id(snowflake.nextId())
                    .username(generateUsername())
                    .password(passwordEncoder.encode("123456"))
                    .wxWebOpenId(openId)
                    .wxUnionId(unionId)
                    .avatar(info.getString("headimgurl"))
                    .nickname(info.getString("nickname"))
                    .loginType("wechat")
                    .build();
            userRepo.save(user);
        }
        return buildVO(user);
    }

    @Override
    public UserVO loginAsTourist() throws Exception {
        String username = generateUsername();
        // 游客登录时分配随机头像
        Avatar randomAvatar = avatarService.getRandomAvatar();
        User user = User.builder()
                .id(snowflake.nextId())
                .username(username)
                .password(passwordEncoder.encode("123456"))
                .avatar(randomAvatar.getUrl())
                .loginType("tourist")
                .build();
        userRepo.save(user);
        log.info("游客登录成功，分配头像: {}", randomAvatar.getUrl());
        return buildVO(user);
    }

    @Override
    public UserVO register(RegisterRequest req) throws Exception {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new Exception("两次密码不一致");
        }
        if (userRepo.existsByPhone(req.getPhone())) {
            throw new Exception("该手机号已注册");
        }
        if (!smsService.verifyCode(req.getPhone(), "register", req.getCode())) {
            throw new Exception("验证码错误或已过期");
        }
        User user = createPhoneUser(req.getPhone());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        // 注册时分配随机头像
        Avatar randomAvatar = avatarService.getRandomAvatar();
        user.setAvatar(randomAvatar.getUrl());
        userRepo.save(user);
        log.info("用户注册成功，分配头像: {}", randomAvatar.getUrl());
        return buildVO(user);
    }

    @Override
    public void resetPassword(ResetPasswordRequest req) throws Exception {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new Exception("两次密码不一致");
        }
        if (!smsService.verifyCode(req.getPhone(), "reset", req.getCode())) {
            throw new Exception("验证码错误或已过期");
        }
        User user = userRepo.findByPhone(req.getPhone())
                .orElseThrow(() -> new Exception("该手机号未注册"));
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepo.save(user);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        return userRepo.findById(userId).map(this::buildVO).orElse(null);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userRepo.existsByUsername(username);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setAvatar(avatarUrl);
        userRepo.save(user);
        log.info("用户 {} 更新头像成功: {}", userId, avatarUrl);
    }

    @Override
    public void updateAvatarFromList(Long userId, Long avatarId) {
        Avatar avatar = avatarRepository.findById(avatarId)
                .orElseThrow(() -> new RuntimeException("头像不存在"));
        updateAvatar(userId, avatar.getUrl());
    }

    // ===== 新增用户资料相关方法实现 =====

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return buildProfileVO(user);
    }

    @Override
    public void updateProfile(Long userId, UpdateProfileRequest req) throws Exception {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getBio() != null) {
            user.setBio(req.getBio());
        }
        if (req.getGender() != null) {
            user.setGender(req.getGender());
        }
        if (req.getBirthday() != null) {
            try {
                user.setBirthday(LocalDate.parse(req.getBirthday()));
            } catch (Exception e) {
                throw new Exception("生日格式不正确，请使用 YYYY-MM-DD 格式");
            }
        }
        
        userRepo.save(user);
        log.info("用户 {} 更新资料成功", userId);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest req) throws Exception {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new Exception("两次密码不一致");
        }
        if (req.getNewPassword().length() < 6) {
            throw new Exception("密码长度至少6位");
        }
        
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        // 游客用户没有旧密码，跳过验证
        if (!"tourist".equals(user.getLoginType()) && user.getPassword() != null) {
            if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
                throw new Exception("旧密码不正确");
            }
        }
        
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepo.save(user);
        log.info("用户 {} 修改密码成功", userId);
    }

    @Override
    public void bindPhone(Long userId, BindPhoneRequest req) throws Exception {
        if (!smsService.verifyCode(req.getPhone(), "bind", req.getCode())) {
            throw new Exception("验证码错误或已过期");
        }
        
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new Exception("用户不存在"));
        
        // 检查手机号是否已被其他用户绑定
        Optional<User> existingUser = userRepo.findByPhone(req.getPhone());
        if (existingUser.isPresent() && !existingUser.get().getId().equals(userId)) {
            throw new Exception("该手机号已被其他账号绑定");
        }
        
        user.setPhone(req.getPhone());
        // 如果之前是游客登录，绑定手机号后改为手机号登录
        if ("tourist".equals(user.getLoginType())) {
            user.setLoginType("phone");
        }
        userRepo.save(user);
        log.info("用户 {} 绑定手机号成功: {}", userId, req.getPhone());
    }

    @Override
    public void addPoints(Long userId, int points) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) return;
        
        int newPoints = (user.getPoints() == null ? 0 : user.getPoints()) + points;
        user.setPoints(newPoints);
        
        // 自动计算等级
        int newLevel = calculateLevel(newPoints);
        user.setLevel(newLevel);
        
        userRepo.save(user);
        log.info("用户 {} 增加 {} 积分，当前积分: {}, 等级: {}", userId, points, newPoints, newLevel);
    }

    @Override
    public int calculateLevel(int points) {
        // 等级定义：1-新手(0), 2-初级(100), 3-中级(500), 4-高级(1000), 5-专家(5000)
        // 6-大师(10000), 7-宗师(20000), 8-王者(50000), 9-传奇(100000), 10-至尊(200000)
        if (points >= 200000) return 10;
        if (points >= 100000) return 9;
        if (points >= 50000) return 8;
        if (points >= 20000) return 7;
        if (points >= 10000) return 6;
        if (points >= 5000) return 5;
        if (points >= 1000) return 4;
        if (points >= 500) return 3;
        if (points >= 100) return 2;
        return 1;
    }

    private String getLevelName(int level) {
        String[] names = {"", "新手", "初级", "中级", "高级", "专家", "大师", "宗师", "王者", "传奇", "至尊"};
        return level >= 1 && level <= 10 ? names[level] : "未知";
    }

    private int getNextLevelPoints(int level) {
        // 返回升到下一级所需积分
        int[] thresholds = {0, 100, 500, 1000, 5000, 10000, 20000, 50000, 100000, 200000, Integer.MAX_VALUE};
        return level >= 0 && level < thresholds.length - 1 ? thresholds[level] : Integer.MAX_VALUE;
    }

    private String getVipStatus(LocalDateTime vipExpireAt) {
        if (vipExpireAt == null) return "normal";
        if (vipExpireAt.isBefore(LocalDateTime.now())) return "expired";
        return "active";
    }

    private UserProfileVO buildProfileVO(User user) {
        int level = user.getLevel() != null ? user.getLevel() : 1;
        int points = user.getPoints() != null ? user.getPoints() : 0;
        
        return UserProfileVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .phoneMasked(maskPhone(user.getPhone()))
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .bio(user.getBio())
                .gender(user.getGender())
                .genderLabel(getGenderLabel(user.getGender()))
                .birthday(user.getBirthday())
                .level(level)
                .levelName(getLevelName(level))
                .points(points)
                .nextLevelPoints(getNextLevelPoints(level))
                .vipExpireAt(user.getVipExpireAt())
                .vipStatus(getVipStatus(user.getVipExpireAt()))
                .loginType(user.getLoginType())
                .loginTypeLabel(getLoginTypeLabel(user.getLoginType()))
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .loginCount(user.getLoginCount() != null ? user.getLoginCount() : 0)
                .build();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String getGenderLabel(Integer gender) {
        if (gender == null) return "未知";
        return switch (gender) {
            case 1 -> "男";
            case 2 -> "女";
            default -> "未知";
        };
    }

    private String getLoginTypeLabel(String loginType) {
        if (loginType == null) return "未知";
        return switch (loginType) {
            case "phone" -> "手机号登录";
            case "wechat" -> "微信登录";
            case "tourist" -> "游客登录";
            default -> loginType;
        };
    }

    private User createPhoneUser(String phone) {
        // 手机号注册新用户，分配随机头像
        Avatar randomAvatar = avatarService.getRandomAvatar();
        User user = User.builder()
                .id(snowflake.nextId())
                .username(generateUsername())
                .password(passwordEncoder.encode("123456"))
                .phone(phone)
                .avatar(randomAvatar.getUrl())
                .loginType("phone")
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

    private UserVO buildVO(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .loginType(user.getLoginType())
                .token(token)
                .build();
    }

    private String httpGet(String url) throws Exception {
        Request req = new Request.Builder().url(url)
                .header("User-Agent", "Mozilla/5.0").build();
        try (Response resp = httpClient.newCall(req).execute()) {
            return resp.body().string();
        }
    }
}
