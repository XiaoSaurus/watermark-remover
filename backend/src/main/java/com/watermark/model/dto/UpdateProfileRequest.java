package com.watermark.model.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 50, message = "昵称最多50个字符")
    private String nickname;

    @Size(max = 200, message = "个人简介最多200个字符")
    private String bio;

    private Integer gender; // 0未知 1男 2女

    private String birthday; // 格式: YYYY-MM-DD
}
