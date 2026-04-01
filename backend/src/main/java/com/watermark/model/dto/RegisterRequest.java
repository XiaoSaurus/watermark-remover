package com.watermark.model.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String type;
    private String phone;
    private String code;
    private String password;
    private String confirmPassword;
    private String username;
}
