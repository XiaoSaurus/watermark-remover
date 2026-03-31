package com.watermark.parser.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
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
public class BilibiliParser implements VideoParser {

    private static final java.util.Map<Integer, String> QUALITY_MAP = new java.util.HashMap<>() {{
        put(127, "8K 超高清"); put(126, "杜比视界"); put(125, "HDR 真彩");
        put(120, "4K 超清"); put(116, "1080P 60帧"); put(112, "1080P 高码率");
        put(80, "1080P 高清"); put(64, "720P 高清"); put(32, "480P 清晰");
        put(16, "360P 流畅");
    }};

    @Override
    public boolean supports(String url) {
        return url.contains("bilibili.com") || url.contains("b23.tv");
    }

    @Override
    public VideoInfo parse(String url) throws Exception {
        // 展开短链
        String realUrl = url;
        if (url.contains("b23.tv")) {
            realUrl = HttpUtil.resolveRedirect(url);
            log.info("B站真实URL: {}", realUrl);
        }

        // 提取 BV 号或 AV 号
        String bvid = extractBvid(realUrl);
        if (bvid == null) throw new Exception("无法提取B站视频ID");

        // 获取视频信息
        String infoUrl = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;
        String infoBody = HttpUtil.get(infoUrl, HttpUtil.UA_PC);
        JSONObject infoJson = JSON.parseObject(infoBody).getJSONObject("data");

        String title = infoJson.getString("title");
        String author = infoJson.getJSONObject("owner").getString("name");
        String cover = infoJson.getString("pic");
        long cid = infoJson.getLongValue("cid");

        // 获取视频流（不登录只能获取到 480P，登录后可获取更高清晰度）
        String playUrl = "https://api.bilibili.com/x/player/playurl?bvid=" + bvid
                + "&cid=" + cid + "&qn=0&fnval=4048&fourk=1";
        String playBody = HttpUtil.get(playUrl, HttpUtil.UA_PC);
        JSONObject playJson = JSON.parseObject(playBody).getJSONObject("data");

        List<VideoUrl> videoUrls = new ArrayList<>();
        String audioUrl = null;

        // DASH 格式（音视频分离）
        if (playJson.containsKey("dash")) {
            JSONObject dash = playJson.getJSONObject("dash");
            JSONArray videos = dash.getJSONArray("video");
            JSONArray audios = dash.getJSONArray("audio");

            // 取最高质量视频
            if (videos != null && !videos.isEmpty()) {
                JSONObject best = videos.getJSONObject(0);
                int qn = best.getIntValue("id");
                String quality = QUALITY_MAP.getOrDefault(qn, qn + "P");
                videoUrls.add(new VideoUrl(quality, best.getString("baseUrl")));
            }

            // 音频
            if (audios != null && !audios.isEmpty()) {
                audioUrl = audios.getJSONObject(0).getString("baseUrl");
            }
        } else {
            // FLV 格式（旧版）
            JSONArray durl = playJson.getJSONArray("durl");
            if (durl != null) {
                int qn = playJson.getIntValue("quality");
                String quality = QUALITY_MAP.getOrDefault(qn, qn + "P");
                videoUrls.add(new VideoUrl(quality, durl.getJSONObject(0).getString("url")));
            }
        }

        return VideoInfo.builder()
                .platform("bilibili")
                .title(title)
                .author(author)
                .cover(cover)
                .videoUrls(videoUrls)
                .audioUrl(audioUrl)
                .shareUrl(url)
                .build();
    }

    private String extractBvid(String url) {
        Pattern p = Pattern.compile("BV([a-zA-Z0-9]+)");
        Matcher m = p.matcher(url);
        if (m.find()) return "BV" + m.group(1);
        return null;
    }
}