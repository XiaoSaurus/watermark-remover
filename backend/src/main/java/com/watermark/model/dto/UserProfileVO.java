package com.watermark.model.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserProfileVO {
    private Long id;
    private String username;
    private String phone;
    private String phoneMasked; // 脱敏手机号
    private String avatar;
    private String nickname;
    private String bio;
    private Integer gender;
    private String genderLabel; // 性别文本
    private LocalDate birthday;
    private Integer level;
    private String levelName; // 等级名称
    private Integer points;
    private Integer nextLevelPoints; // 升级所需积分
    private LocalDateTime vipExpireAt;
    private String vipStatus; // VIP状态: normal/expired/active
    private String loginType;
    private String loginTypeLabel;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Integer loginCount;
}
