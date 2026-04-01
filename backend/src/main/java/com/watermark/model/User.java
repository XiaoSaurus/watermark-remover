package com.watermark.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Entity @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "`user`", indexes = {
    @Index(name = "idx_phone", columnList = "phone"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_wx_openid", columnList = "wxOpenId"),
    @Index(name = "idx_wx_unionid", columnList = "wxUnionId"),
    @Index(name = "idx_wx_web_openid", columnList = "wxWebOpenId")
})
public class User {
    @Id
    private Long id;

    @Column(length = 64, unique = true)
    private String username;

    @Column(length = 128)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(length = 64)
    private String wxOpenId;

    @Column(length = 64)
    private String wxUnionId;

    @Column(length = 64)
    private String wxWebOpenId;

    @Column(length = 256)
    private String avatar;

    @Column(length = 64)
    private String nickname;

    @Column(length = 200)
    private String bio;

    @Column(columnDefinition = "TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女'")
    @Builder.Default
    private Integer gender = 0;

    private LocalDate birthday;

    @Column(columnDefinition = "INT DEFAULT 1 COMMENT '用户等级 1-10'")
    @Builder.Default
    private Integer level = 1;

    @Column(columnDefinition = "INT DEFAULT 0 COMMENT '积分'")
    @Builder.Default
    private Integer points = 0;

    private LocalDateTime vipExpireAt;

    @Column(length = 16)
    @Builder.Default
    private String status = "normal";

    @Column(length = 16)
    @Builder.Default
    private String loginType = "tourist";

    @Builder.Default
    private Boolean deleted = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    @Column(columnDefinition = "INT DEFAULT 0 COMMENT '登录次数'")
    @Builder.Default
    private Integer loginCount = 0;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.lastLoginAt = LocalDateTime.now();
    }
}
