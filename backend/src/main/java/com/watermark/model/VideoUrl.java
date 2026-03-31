package com.watermark.model;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class VideoUrl {
    /** 清晰度描述，如 1080p / 720p / 原画 */
    private String quality;
    /** 下载地址 */
    private String url;
}