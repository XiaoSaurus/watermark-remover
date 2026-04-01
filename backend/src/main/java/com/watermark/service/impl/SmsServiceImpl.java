package com.watermark.service.impl;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.watermark.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Value("${aliyun.sms.access-key-id:}") 
    private String accessKeyId;
    
    @Value("${aliyun.sms.access-key-secret:}") 
    private String accessKeySecret;
    
    @Value("${aliyun.sms.sign-name:去水印工具}") 
    private String signName;
    
    @Value("${aliyun.sms.template-code:}") 
    private String templateCode;

    private final StringRedisTemplate redis;
    private Client aliClient;
    private final Object clientLock = new Object();

    public SmsServiceImpl(StringRedisTemplate redis) {
        this.redis = redis;
    }

    // Redis key prefixes
    private static final String SMS_CODE_PREFIX = "sms:code:";
    private static final String SMS_LIMIT_SECOND_PREFIX = "sms:limit:sec:";
    private static final String SMS_LIMIT_DAILY_PHONE_PREFIX = "sms:limit:daily:phone:";
    private static final String SMS_LIMIT_HOURLY_IP_PREFIX = "sms:limit:hourly:ip:";
    private static final String SMS_ERROR_COUNT_PREFIX = "sms:error:";
    
    // Constants
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRY_COUNT = 3;
    private static final int MAX_ERROR_COUNT = 5;
    
    // Rate limits
    private static final int LIMIT_SECONDS = 60;           // 同一手机号60秒内只能发1次
    private static final int LIMIT_DAILY_PHONE = 5;         // 同一手机号每天最多5次
    private static final int LIMIT_HOURLY_IP = 10;          // 同一IP每小时最多10次

    /**
     * 检查是否为开发模式（未配置阿里云AccessKey）
     */
    private boolean isDevMode() {
        return accessKeyId == null || accessKeyId.isBlank() 
            || accessKeyId.startsWith("YOUR_") || accessKeyId.equals("");
    }

    /**
     * 获取阿里云短信客户端（懒加载，线程安全）
     */
    private Client getOrCreateClient() throws Exception {
        if (aliClient != null) return aliClient;
        synchronized (clientLock) {
            if (aliClient != null) return aliClient;
            Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dysmsapi.aliyuncs.com");
            aliClient = new Client(config);
            return aliClient;
        }
    }

    /**
     * 手机号脱敏：138****8000
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 验证手机号格式（11位，以1开头）
     */
    private void validatePhone(String phone) throws Exception {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new Exception("手机号格式不正确");
        }
    }

    /**
     * 验证场景参数
     */
    private void validateScene(String scene) throws Exception {
        if (scene == null || (!scene.equals("login") && !scene.equals("register") && !scene.equals("reset"))) {
            throw new Exception("无效的场景参数");
        }
    }

    @Override
    public void sendCode(String phone, String scene) throws Exception {
        validatePhone(phone);
        validateScene(scene);

        // 检查60秒内是否已发送（频率限制）
        String secondLimitKey = SMS_LIMIT_SECOND_PREFIX + phone + ":" + scene;
        if (Boolean.TRUE.equals(redis.hasKey(secondLimitKey))) {
            Long ttl = redis.getExpire(secondLimitKey, TimeUnit.SECONDS);
            throw new Exception("请" + (ttl != null ? ttl : 60) + "秒后再试");
        }

        // 检查同一手机号每天发送次数
        String dailyLimitKey = SMS_LIMIT_DAILY_PHONE_PREFIX + phone;
        String dailyCount = redis.opsForValue().get(dailyLimitKey);
        if (dailyCount != null && Integer.parseInt(dailyCount) >= LIMIT_DAILY_PHONE) {
            throw new Exception("今日发送次数已达上限（" + LIMIT_DAILY_PHONE + "次）");
        }

        // 生成6位随机验证码
        String code = generateCode();

        // 发送短信
        boolean sendSuccess = false;
        Exception lastError = null;

        if (isDevMode()) {
            // 开发模式：打印到控制台
            log.info("[SMS DEV] 手机号: {}, 场景: {}, 验证码: {}", maskPhone(phone), scene, code);
            System.out.println("========================================");
            System.out.println("[SMS DEV] 验证码发送成功");
            System.out.println("  手机号: " + maskPhone(phone));
            System.out.println("  场景: " + scene);
            System.out.println("  验证码: " + code);
            System.out.println("  有效期: " + CODE_EXPIRE_MINUTES + "分钟");
            System.out.println("========================================");
            sendSuccess = true;
        } else {
            // 生产模式：真实调用阿里云短信API（带重试）
            for (int i = 0; i < MAX_RETRY_COUNT && !sendSuccess; i++) {
                try {
                    Client client = getOrCreateClient();
                    SendSmsRequest req = new SendSmsRequest()
                        .setPhoneNumbers(phone)
                        .setSignName(signName)
                        .setTemplateCode(templateCode)
                        .setTemplateParam("{\"code\":\"" + code + "\"}");
                    SendSmsResponse resp = client.sendSms(req);
                    
                    if ("OK".equals(resp.getBody().getCode())) {
                        sendSuccess = true;
                        log.info("[SMS] 发送成功: phone={}, scene={}", maskPhone(phone), scene);
                    } else {
                        String errMsg = resp.getBody().getMessage();
                        log.warn("[SMS] 发送失败(重试{}/{}): phone={}, error={}", 
                            i + 1, MAX_RETRY_COUNT, maskPhone(phone), errMsg);
                        lastError = new Exception("短信发送失败：" + errMsg);
                        Thread.sleep(500); // 重试间隔
                    }
                } catch (Exception e) {
                    log.error("[SMS] 发送异常(重试{}/{}): phone={}, error={}", 
                        i + 1, MAX_RETRY_COUNT, maskPhone(phone), e.getMessage());
                    lastError = e;
                    Thread.sleep(500);
                }
            }
        }

        if (!sendSuccess && !isDevMode()) {
            throw lastError != null ? lastError : new Exception("短信发送失败，请稍后重试");
        }

        // 保存验证码到Redis（5分钟过期）
        String codeKey = SMS_CODE_PREFIX + scene + ":" + phone;
        redis.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 设置60秒内禁止重复发送
        redis.opsForValue().set(secondLimitKey, "1", LIMIT_SECONDS, TimeUnit.SECONDS);

        // 增加每日发送计数（24小时过期）
        redis.opsForValue().increment(dailyLimitKey);
        redis.expire(dailyLimitKey, 24, TimeUnit.HOURS);
    }

    @Override
    public boolean verifyCode(String phone, String scene, String code) {
        if (phone == null || scene == null || code == null) {
            return false;
        }

        // 检查验证码错误次数（防止暴力破解）
        String errorCountKey = SMS_ERROR_COUNT_PREFIX + phone;
        String errorCount = redis.opsForValue().get(errorCountKey);
        if (errorCount != null && Integer.parseInt(errorCount) >= MAX_ERROR_COUNT) {
            // 错误次数达到上限，删除验证码
            String codeKey = SMS_CODE_PREFIX + scene + ":" + phone;
            redis.delete(codeKey);
            redis.delete(errorCountKey);
            log.warn("[SMS] 验证码错误次数超限: phone={}", maskPhone(phone));
            return false;
        }

        String codeKey = SMS_CODE_PREFIX + scene + ":" + phone;
        String storedCode = redis.opsForValue().get(codeKey);
        
        if (storedCode != null && storedCode.equals(code)) {
            // 验证成功，删除验证码和错误计数
            redis.delete(codeKey);
            redis.delete(errorCountKey);
            return true;
        } else {
            // 验证失败，增加错误计数（30分钟过期）
            redis.opsForValue().increment(errorCountKey);
            redis.expire(errorCountKey, 30, TimeUnit.MINUTES);
            log.warn("[SMS] 验证码错误: phone={}, scene={}", maskPhone(phone), scene);
            return false;
        }
    }

    /**
     * 生成6位随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = random.nextInt(1000000);
        return String.format("%06d", code);
    }

    /**
     * IP频率限制检查（可选，需要从请求中获取IP）
     * 在Controller中调用此方法
     */
    public void checkIpRateLimit(String ip) throws Exception {
        String ipLimitKey = SMS_LIMIT_HOURLY_IP_PREFIX + ip;
        String count = redis.opsForValue().get(ipLimitKey);
        if (count != null && Integer.parseInt(count) >= LIMIT_HOURLY_IP) {
            throw new Exception("当前网络发送频繁，请1小时后再试");
        }
    }

    /**
     * 增加IP发送计数
     */
    public void incrementIpCount(String ip) {
        String ipLimitKey = SMS_LIMIT_HOURLY_IP_PREFIX + ip;
        redis.opsForValue().increment(ipLimitKey);
        redis.expire(ipLimitKey, 1, TimeUnit.HOURS);
    }
}
