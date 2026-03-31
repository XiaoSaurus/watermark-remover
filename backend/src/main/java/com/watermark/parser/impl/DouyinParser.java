package com.watermark.parser.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.watermark.model.VideoInfo;
import com.watermark.model.VideoUrl;
import com.watermark.parser.HttpUtil;
import com.watermark.parser.VideoParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DouyinParser implements VideoParser {

    @Override
    public boolean supports(String url) {
        return url.contains("douyin.com") || url.contains("iesdouyin.com")
                || url.contains("v.douyin.com");
    }

    @Override
    public VideoInfo parse(String url) throws Exception {
        // 1. 短链接展开
        String realUrl = url;
        if (url.contains("v.douyin.com") || url.length() < 60) {
            realUrl = HttpUtil.resolveRedirect(url);
            log.info("抖音真实URL: {}", realUrl);
        }

        // 2. 提取 video_id
        String videoId = extractVideoId(realUrl);
        if (videoId == null) throw new Exception("无法提取抖音视频ID");

        // 3. 调用抖音移动端 API
        String apiUrl = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=" + videoId;
        String body = HttpUtil.get(apiUrl, HttpUtil.UA_MOBILE);
        JSONObject json = JSON.parseObject(body);

        JSONObject item = json.getJSONArray("item_list").getJSONObject(0);
        String title = item.getString("desc");
        String author = item.getJSONObject("author").getString("nickname");
        String cover = item.getJSONObject("video")
                .getJSONObject("cover").getJSONArray("url_list").getString(0);

        // 4. 获取无水印视频地址
        // play_addr 是有水印版，download_addr 是无水印版
        JSONObject downloadAddr = item.getJSONObject("video").getJSONObject("download_addr");
        String noWatermarkUrl = downloadAddr.getJSONArray("url_list").getString(0);
        // 替换域名为无水印地址
        noWatermarkUrl = noWatermarkUrl.replace("playwm", "play");

        List<VideoUrl> videoUrls = new ArrayList<>();
        videoUrls.add(new VideoUrl("无水印", noWatermarkUrl));

        return VideoInfo.builder()
                .platform("douyin")
                .title(title)
                .author(author)
                .cover(cover)
                .videoUrls(videoUrls)
                .shareUrl(url)
                .build();
    }

    private String extractVideoId(String url) {
        // 匹配 /video/1234567890
        Pattern p = Pattern.compile("/video/(\\d+)");
        Matcher m = p.matcher(url);
        if (m.find()) return m.group(1);
        // 匹配 ?aweme_id=xxx
        p = Pattern.compile("[?&]aweme_id=(\\d+)");
        m = p.matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }
}