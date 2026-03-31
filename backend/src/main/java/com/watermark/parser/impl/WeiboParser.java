package com.watermark.parser.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.watermark.model.VideoInfo;
import com.watermark.model.VideoUrl;
import com.watermark.parser.HttpUtil;
import com.watermark.parser.VideoParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class WeiboParser implements VideoParser {

    @Override
    public boolean supports(String url) {
        return url.contains("weibo.com") || url.contains("weibo.cn")
                || url.contains("t.cn");
    }

    @Override
    public VideoInfo parse(String url) throws Exception {
        String realUrl = url;
        if (url.contains("t.cn")) {
            realUrl = HttpUtil.resolveRedirect(url);
            log.info("微博真实URL: {}", realUrl);
        }

        // 提取微博 ID
        String weiboId = extractWeiboId(realUrl);
        if (weiboId == null) throw new Exception("无法提取微博ID");

        String apiUrl = "https://weibo.com/ajax/statuses/show?id=" + weiboId;
        String body = HttpUtil.get(apiUrl, HttpUtil.UA_PC);
        JSONObject json = JSON.parseObject(body);

        String title = json.getString("text_raw");
        if (title != null && title.length() > 50) title = title.substring(0, 50) + "...";
        String author = json.getJSONObject("user").getString("screen_name");
        String cover = "";

        List<VideoUrl> videoUrls = new ArrayList<>();
        try {
            JSONObject pageInfo = json.getJSONObject("page_info");
            if (pageInfo != null && pageInfo.containsKey("media_info")) {
                JSONObject mediaInfo = pageInfo.getJSONObject("media_info");
                cover = pageInfo.getString("page_pic");
                // 获取各清晰度
                String hd = mediaInfo.getString("mp4_hd_url");
                String sd = mediaInfo.getString("mp4_sd_url");
                if (hd != null && !hd.isEmpty()) videoUrls.add(new VideoUrl("高清", hd));
                if (sd != null && !sd.isEmpty()) videoUrls.add(new VideoUrl("标清", sd));
            }
        } catch (Exception e) {
            log.warn("微博视频解析异常: {}", e.getMessage());
        }

        if (videoUrls.isEmpty()) throw new Exception("未找到视频资源，该微博可能不含视频");

        return VideoInfo.builder()
                .platform("weibo")
                .title(title)
                .author(author)
                .cover(cover)
                .videoUrls(videoUrls)
                .shareUrl(url)
                .build();
    }

    private String extractWeiboId(String url) {
        Pattern p = Pattern.compile("/(?:status|detail)/(\\w+)");
        Matcher m = p.matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }
}