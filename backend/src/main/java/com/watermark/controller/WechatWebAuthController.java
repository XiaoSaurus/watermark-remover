package com.watermark.controller;

import com.watermark.model.Result;
import com.watermark.service.WechatWebAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Tag(name = "微信网页授权")
@Slf4j
@RestController
@RequestMapping("/api/auth/wechat/web")
@RequiredArgsConstructor
public class WechatWebAuthController {

    private final WechatWebAuthService wechatWebAuthService;

    @Operation(summary = "生成扫码登录二维码链接")
    @GetMapping("/qr-url")
    public Result<Map<String, String>> getQrUrl() {
        String scene = UUID.randomUUID().toString().replace("-", "");
        String qrUrl = wechatWebAuthService.generateQrUrl(scene);
        return Result.success(Map.of("qrUrl", qrUrl, "scene", scene));
    }

    @Operation(summary = "轮询扫码状态")
    @GetMapping("/status")
    public Result<?> getStatus(@RequestParam String scene) {
        return wechatWebAuthService.checkScanStatus(scene);
    }

    @Operation(summary = "微信开放平台回调")
    @GetMapping("/callback")
    public String wechatCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state) {
        try {
            if (code == null || code.isEmpty()) {
                return buildCallbackHtml("授权失败", "用户取消授权", "error");
            }
            wechatWebAuthService.handleCallback(code, state);
            return buildCallbackHtml("授权成功", "请在原页面继续操作", "success");
        } catch (Exception e) {
            log.error("微信回调处理失败", e);
            return buildCallbackHtml("授权失败", e.getMessage(), "error");
        }
    }

    private String buildCallbackHtml(String title, String message, String status) {
        return "<!DOCTYPE html><html><head><meta charset='utf-8'><title>" + title + "</title>" +
                "<style>body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;" +
                "display:flex;justify-content:center;align-items:center;min-height:100vh;margin:0;" +
                "background:linear-gradient(135deg,#667eea 0%,#764ba2 100%);}" +
                ".card{background:#fff;border-radius:16px;padding:40px;text-align:center;box-shadow:0 10px 40px rgba(0,0,0,0.2);}" +
                ".icon{font-size:64px;margin-bottom:20px;}" +
                "h1{color:#333;margin:0 0 10px;font-size:24px;}" +
                "p{color:#666;margin:0;font-size:14px;}" +
                ".success .icon{color:#52c41a;}" +
                ".error .icon{color:#f5222d;}</style></head><body>" +
                "<div class='card " + status + "'>" +
                "<div class='icon'>" + (status.equals("success") ? "✅" : "❌") + "</div>" +
                "<h1>" + title + "</h1><p>" + message + "</p></div></body></html>";
    }
}
