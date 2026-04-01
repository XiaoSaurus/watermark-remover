package com.watermark.service;

public interface SmsService {
    void sendCode(String phone, String scene) throws Exception;
    boolean verifyCode(String phone, String scene, String code);
}
