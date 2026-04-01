package com.watermark.service;

import com.watermark.model.Result;

import java.util.Map;

public interface WechatWebAuthService {
    /**
     * 生成微信扫码登录二维码URL
     */
    String generateQrUrl(String scene);

    /**
     * 检查扫码状态
     */
    Result<?> checkScanStatus(String scene);

    /**
     * 处理微信回调
     */
    void handleCallback(String code, String state) throws Exception;
}
