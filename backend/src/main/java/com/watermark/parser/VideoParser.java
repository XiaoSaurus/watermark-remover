package com.watermark.parser;

import com.watermark.model.VideoInfo;

public interface VideoParser {
    /** 判断该解析器是否支持此链接 */
    boolean supports(String url);
    /** 解析链接，返回无水印视频信息 */
    VideoInfo parse(String url) throws Exception;
}