package com.watermark.controller;

import com.watermark.model.Result;
import com.watermark.model.dto.*;
import com.watermark.service.SmsService;
import com.watermark.service.UserService;
import com.watermark.service.impl.SmsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SmsService smsService;
    private final SmsServiceImpl smsServiceImpl;

    @Operation(summary = "发送短信验证码")
    @PostMapping("/sms/send")
    public Result<Void> sendSms(@RequestBody SmsRequest req, HttpServletRequest request) {
        try {
            // 获取客户端IP
            String ip = getClientIp(request);
            
            // IP频率限制检查
            try {
                smsServiceImpl.checkIpRateLimit(ip);
            } catch (Exception e) {
                return Result.error(429, e.getMessage());
            }
            
            // 发送验证码
            smsService.sendCode(req.getPhone(), req.getScene());
            
            // 增加IP发送计数
            smsServiceImpl.incrementIpCount(ip);
            
            return Result.success(null);
        } catch (Exception e) {
            String msg = e.getMessage();
            // 根据错误信息返回不同错误码
            if (msg.contains("手机号格式")) {
                return Result.error(400, msg);
            } else if (msg.contains("秒后再试") || msg.contains("发送太频繁")) {
                return Result.error(429, msg);
            } else if (msg.contains("今日发送次数")) {
                return Result.error(429, msg);
            } else if (msg.contains("网络发送频繁")) {
                return Result.error(429, msg);
            } else {
                return Result.error(500, "发送失败，请稍后重试");
            }
        }
    }

    @Operation(summary = "验证短信验证码")
    @PostMapping("/sms/verify")
    public Result<Map<String, Boolean>> verifySms(@RequestBody VerifySmsRequest req) {
        boolean valid = smsService.verifyCode(req.getPhone(), req.getScene(), req.getCode());
        Map<String, Boolean> data = new HashMap<>();
        data.put("valid", valid);
        
        if (valid) {
            return Result.success(data);
        } else {
            return Result.error(400, "验证码错误或已过期");
        }
    }

    @Operation(summary = "登录（phone/wechat_miniprogram/wechat_web/tourist）")
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginRequest req) {
        try {
            UserVO vo = switch (req.getType()) {
                case "phone" -> userService.loginByPhone(req);
                case "wechat_miniprogram" -> userService.loginByWechatMiniprogram(req);
                case "wechat_web" -> userService.loginByWechatWeb(req);
                case "tourist" -> userService.loginAsTourist();
                default -> throw new Exception("不支持的登录方式：" + req.getType());
            };
            return Result.success(vo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "注册（手机号）")
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterRequest req) {
        try {
            return Result.success(userService.register(req));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "忘记密码/重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            userService.resetPassword(req);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserVO> me(Authentication auth) {
        if (auth == null) return Result.error("未登录");
        Long userId = (Long) auth.getPrincipal();
        UserVO vo = userService.getUserInfo(userId);
        return vo != null ? Result.success(vo) : Result.error("用户不存在");
    }

    // ===== 新增用户资料相关接口 =====

    @Operation(summary = "获取用户详细资料")
    @GetMapping("/profile")
    public Result<UserProfileVO> profile(Authentication auth) {
        if (auth == null) return Result.error("未登录");
        Long userId = (Long) auth.getPrincipal();
        UserProfileVO vo = userService.getUserProfile(userId);
        return vo != null ? Result.success(vo) : Result.error("用户不存在");
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(Authentication auth, @RequestBody UpdateProfileRequest req) {
        if (auth == null) return Result.error("未登录");
        Long userId = (Long) auth.getPrincipal();
        try {
            userService.updateProfile(userId, req);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(Authentication auth, @RequestBody ChangePasswordRequest req) {
        if (auth == null) return Result.error("未登录");
        Long userId = (Long) auth.getPrincipal();
        try {
            userService.changePassword(userId, req);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "绑定手机号")
    @PostMapping("/bind-phone")
    public Result<Void> bindPhone(Authentication auth, @RequestBody BindPhoneRequest req) {
        if (auth == null) return Result.error("未登录");
        Long userId = (Long) auth.getPrincipal();
        try {
            userService.bindPhone(userId, req);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个代理IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown";
    }
}
