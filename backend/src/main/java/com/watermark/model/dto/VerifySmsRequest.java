package com.watermark.model.dto;

import lombok.Data;

@Data
public class VerifySmsRequest {
    private String phone;
    private String code;
    private String scene;
}
