package com.watermark.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

@Data @Entity @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "parse_history")
public class ParseHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 32)
    private String platform;

    @Column(length = 512)
    private String title;

    @Column(length = 1024)
    private String cover;

    @Column(length = 1024)
    private String author;

    /** 原始分享链接 */
    @Column(length = 1024)
    private String shareUrl;

    /** 视频地址列表，JSON 存储 */
    @Column(columnDefinition = "TEXT")
    private String videoUrlsJson;

    /** 客户端：web / miniprogram */
    @Column(length = 32)
    private String client;

    /** 逻辑删除 */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() { this.createdAt = LocalDateTime.now(); }
}
