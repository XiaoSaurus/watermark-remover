package com.watermark.service.impl;

import com.watermark.model.VideoInfo;
import com.watermark.parser.VideoParser;
import com.watermark.service.WatermarkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatermarkServiceImpl implements WatermarkService {

    private final List<VideoParser> parsers;

    @Override
    public VideoInfo parseVideo(String shareUrl) throws Exception {
        // 清理链接（去除多余空格、中文字符等分享文案）
        String cleanUrl = extractUrl(shareUrl);
        log.info("解析链接: {}", cleanUrl);

        for (VideoParser parser : parsers) {
            if (parser.supports(cleanUrl)) {
                log.info("使用解析器: {}", parser.getClass().getSimpleName());
                return parser.parse(cleanUrl);
            }
        }
        throw new Exception("暂不支持该平台，目前支持：抖音、快手、B站、微博、小红书");
    }

    /**
     * 从分享文案中提取 URL
     * 例如："3.14 复制打开抖音，看看【xxx】的作品 https://v.douyin.com/xxx/"
     */
    private String extractUrl(String text) {
        if (text == null) return "";
        text = text.trim();
        // 用正则提取 http/https URL
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("https?://[^\\s\\u4e00-\\u9fa5，。！？、]+");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) return m.group().trim();
        return text;
    }
}