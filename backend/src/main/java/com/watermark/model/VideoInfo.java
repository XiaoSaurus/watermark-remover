package com.watermark.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;

@Data
@Builder
public class VideoInfo {
    /** 平台名称：douyin / kuaishou / bilibili / weibo / xiaohongshu */
    private String platform;
    /** 视频标题 */
    private String title;
    /** 作者昵称 */
    private String author;
    /** 封面图 URL */
    private String cover;
    /** 无水印视频下载地址（可能有多个清晰度） */
    private List<VideoUrl> videoUrls;
    /** 音频地址（B站等音视频分离的平台） */
    private String audioUrl;
    /** 原始分享链接 */
    private String shareUrl;
}