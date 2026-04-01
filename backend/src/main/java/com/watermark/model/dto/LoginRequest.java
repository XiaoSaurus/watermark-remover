package com.watermark.model.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String type;
    private String phone;
    private String code;
    private String password;
}
