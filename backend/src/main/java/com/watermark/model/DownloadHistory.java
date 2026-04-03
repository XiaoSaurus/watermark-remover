package com.watermark.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data @Entity @Builder @NoArgsConstructor @AllArgsConstructor
@Table(name = "download_history")
public class DownloadHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 用户ID，关联用户表 */
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 32)
    private String platform;

    @Column(length = 512)
    private String title;

    @Column(length = 1024)
    private String cover;

    @Column(length = 64)
    private String quality;

    @Column(length = 2048)
    private String videoUrl;

    @Column(length = 1024)
    private String shareUrl;

    /** success / fail */
    @Column(length = 16)
    private String status;

    @Column(length = 256)
    private String errMsg;

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
