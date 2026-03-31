package com.watermark.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "download_history")
public class DownloadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 平台：douyin/kuaishou/bilibili/weibo/xiaohongshu */
    @Column(length = 32)
    private String platform;

    /** 视频标题 */
    @Column(length = 512)
    private String title;

    /** 封面图地址 */
    @Column(length = 1024)
    private String cover;

    /** 清晰度标签 */
    @Column(length = 64)
    private String quality;

    /** 原始视频地址（用于再次下载） */
    @Column(length = 2048)
    private String videoUrl;

    /** 分享原始链接 */
    @Column(length = 1024)
    private String shareUrl;

    /** 下载状态：success / fail */
    @Column(length = 16)
    private String status;

    /** 失败原因 */
    @Column(length = 256)
    private String errMsg;

    /** 客户端标识（web / miniprogram） */
    @Column(length = 32)
    private String client;

    /** 创建时间 */
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
