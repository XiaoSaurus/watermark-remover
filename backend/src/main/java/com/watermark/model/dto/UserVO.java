package com.watermark.model.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserVO {
    private Long id;
    private String username;
    private String phone;
    private String avatar;
    private String nickname;
    private String loginType;
    private String token;
}
