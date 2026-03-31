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
public class DouyinParser implements VideoParser {

    @Override
    public boolean supports(String url) {
        return url.contains("douyin.com") || url.contains("iesdouyin.com")
                || url.contains("v.douyin.com");
    }

    @Override
    public VideoInfo parse(String url) throws Exception {
        // 1. 短链接展开
        String realUrl = HttpUtil.resolveRedirect(url);
        log.info("抖音真实URL: {}", realUrl);

        // 2. 提取 video_id
        String videoId = extractVideoId(realUrl);
        if (videoId == null) throw new Exception("无法提取抖音视频ID，URL=" + realUrl);
        log.info("抖音视频ID: {}", videoId);

        // 3. 用干净的 URL 请求 H5 页面（不带多余参数，避免重定向超时）
        VideoInfo info = parseFromRouterData(videoId, url);
        if (info != null) return info;

        throw new Exception("抖音视频解析失败，请稍后重试（videoId=" + videoId + "）");
    }

    private VideoInfo parseFromRouterData(String videoId, String shareUrl) throws Exception {
        // 用干净的 videoId 直接构造 URL，避免带参数的长 URL 触发额外重定向
        String pageUrl = "https://www.iesdouyin.com/share/video/" + videoId + "/";
        log.info("请求 H5 页面: {}", pageUrl);

        String html = HttpUtil.get(pageUrl, HttpUtil.UA_MOBILE);
        log.info("H5 页面长度: {}", html.length());

        Pattern p = Pattern.compile("<script>window\\._ROUTER_DATA\\s*=\\s*(.+?)</script>",
                Pattern.DOTALL);
        Matcher m = p.matcher(html);
        if (!m.find()) {
            log.warn("未找到 _ROUTER_DATA，页面前200字符: {}", html.substring(0, Math.min(200, html.length())));
            throw new Exception("抖音页面结构变化，未找到视频数据");
        }

        JSONObject data = JSON.parseObject(m.group(1));
        JSONObject loaderData = data.getJSONObject("loaderData");
        if (loaderData == null) throw new Exception("loaderData 为空");

        JSONObject pageData = null;
        for (String key : loaderData.keySet()) {
            if (key.contains("video") && key.contains("page")) {
                pageData = loaderData.getJSONObject(key);
                break;
            }
        }
        if (pageData == null) throw new Exception("未找到 video page 数据");

        JSONObject videoInfoRes = pageData.getJSONObject("videoInfoRes");
        if (videoInfoRes == null) throw new Exception("videoInfoRes 为空");

        JSONArray itemList = videoInfoRes.getJSONArray("item_list");
        if (itemList == null || itemList.isEmpty()) throw new Exception("item_list 为空");

        JSONObject item = itemList.getJSONObject(0);
        String title = item.getString("desc");
        String author = "";
        try { author = item.getJSONObject("author").getString("nickname"); } catch (Exception ignored) {}

        JSONObject video = item.getJSONObject("video");
        String cover = "";
        try { cover = video.getJSONObject("cover").getJSONArray("url_list").getString(0); } catch (Exception ignored) {}

        List<VideoUrl> urls = new ArrayList<>();
        try {
            JSONObject playAddr = video.getJSONObject("play_addr");
            if (playAddr != null) {
                String u = playAddr.getJSONArray("url_list").getString(0).replace("playwm", "play");
                urls.add(new VideoUrl("无水印高清", u));
            }
        } catch (Exception ignored) {}

        try {
            JSONObject downloadAddr = video.getJSONObject("download_addr");
            if (downloadAddr != null && urls.isEmpty()) {
                String u = downloadAddr.getJSONArray("url_list").getString(0);
                urls.add(new VideoUrl("无水印", u));
            }
        } catch (Exception ignored) {}

        if (urls.isEmpty()) throw new Exception("未获取到视频下载地址");

        log.info("解析成功: title={}, urls={}", title, urls.size());
        return VideoInfo.builder()
                .platform("douyin").title(title).author(author)
                .cover(cover).videoUrls(urls).shareUrl(shareUrl).build();
    }

    private String extractVideoId(String url) {
        Pattern p = Pattern.compile("/video/(\\d+)");
        Matcher m = p.matcher(url);
        if (m.find()) return m.group(1);
        p = Pattern.compile("/share/video/(\\d+)");
        m = p.matcher(url);
        if (m.find()) return m.group(1);
        p = Pattern.compile("[?&]aweme_id=(\\d+)");
        m = p.matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }
}