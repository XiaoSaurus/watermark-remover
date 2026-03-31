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

/**
 * 抖音无水印解析器
 * 核心方案：从 iesdouyin H5 页面提取 window._ROUTER_DATA，获取真实视频地址
 */
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

        // 3. 从 iesdouyin H5 页面提取 _ROUTER_DATA（最稳定方案）
        VideoInfo info = parseFromRouterData(videoId, url);
        if (info != null) return info;

        // 4. 降级：旧版 iesdouyin API
        info = tryLegacyApi(videoId, url);
        if (info != null) return info;

        throw new Exception("抖音视频解析失败，请稍后重试");
    }

    /**
     * 主方案：从 iesdouyin H5 页面的 window._ROUTER_DATA 提取视频信息
     */
    private VideoInfo parseFromRouterData(String videoId, String shareUrl) {
        try {
            String pageUrl = "https://www.iesdouyin.com/share/video/" + videoId + "/";
            String html = HttpUtil.get(pageUrl, HttpUtil.UA_MOBILE);

            // 提取 window._ROUTER_DATA = {...}
            Pattern p = Pattern.compile("<script>window\\._ROUTER_DATA\\s*=\\s*(.+?)</script>",
                    Pattern.DOTALL);
            Matcher m = p.matcher(html);
            if (!m.find()) {
                log.warn("未找到 _ROUTER_DATA");
                return null;
            }

            JSONObject data = JSON.parseObject(m.group(1));
            JSONObject loaderData = data.getJSONObject("loaderData");
            if (loaderData == null) return null;

            // key 可能是 "video_(id)/page" 或 "video_[id]/page"
            JSONObject pageData = null;
            for (String key : loaderData.keySet()) {
                if (key.contains("video") && key.contains("page")) {
                    pageData = loaderData.getJSONObject(key);
                    break;
                }
            }
            if (pageData == null) return null;

            JSONObject videoInfoRes = pageData.getJSONObject("videoInfoRes");
            if (videoInfoRes == null) return null;

            JSONArray itemList = videoInfoRes.getJSONArray("item_list");
            if (itemList == null || itemList.isEmpty()) return null;

            JSONObject item = itemList.getJSONObject(0);
            String title = item.getString("desc");
            String author = item.getJSONObject("author").getString("nickname");

            JSONObject video = item.getJSONObject("video");
            String cover = "";
            try {
                cover = video.getJSONObject("cover").getJSONArray("url_list").getString(0);
            } catch (Exception ignored) {}

            List<VideoUrl> urls = new ArrayList<>();

            // play_addr 替换 playwm -> play 得到无水印
            JSONObject playAddr = video.getJSONObject("play_addr");
            if (playAddr != null) {
                JSONArray urlList = playAddr.getJSONArray("url_list");
                if (urlList != null && !urlList.isEmpty()) {
                    String u = urlList.getString(0).replace("playwm", "play");
                    urls.add(new VideoUrl("无水印高清", u));
                }
            }

            // download_addr 通常也是无水印
            JSONObject downloadAddr = video.getJSONObject("download_addr");
            if (downloadAddr != null) {
                JSONArray urlList = downloadAddr.getJSONArray("url_list");
                if (urlList != null && !urlList.isEmpty()) {
                    String u = urlList.getString(0);
                    if (urls.isEmpty()) urls.add(new VideoUrl("无水印", u));
                }
            }

            if (urls.isEmpty()) return null;

            return VideoInfo.builder()
                    .platform("douyin")
                    .title(title)
                    .author(author)
                    .cover(cover)
                    .videoUrls(urls)
                    .shareUrl(shareUrl)
                    .build();

        } catch (Exception e) {
            log.warn("_ROUTER_DATA 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 降级方案：旧版 iesdouyin API
     */
    private VideoInfo tryLegacyApi(String videoId, String shareUrl) {
        try {
            String apiUrl = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=" + videoId;
            String body = HttpUtil.get(apiUrl, HttpUtil.UA_MOBILE);
            JSONObject json = JSON.parseObject(body);
            if (json == null) return null;
            JSONArray itemList = json.getJSONArray("item_list");
            if (itemList == null || itemList.isEmpty()) return null;

            JSONObject item = itemList.getJSONObject(0);
            String title = item.getString("desc");
            String author = item.getJSONObject("author").getString("nickname");
            JSONObject video = item.getJSONObject("video");
            String cover = "";
            try { cover = video.getJSONObject("cover").getJSONArray("url_list").getString(0); } catch (Exception ignored) {}

            List<VideoUrl> urls = new ArrayList<>();
            JSONObject playAddr = video.getJSONObject("play_addr");
            if (playAddr != null) {
                String u = playAddr.getJSONArray("url_list").getString(0).replace("playwm", "play");
                urls.add(new VideoUrl("无水印", u));
            }
            if (urls.isEmpty()) return null;

            return VideoInfo.builder()
                    .platform("douyin").title(title).author(author)
                    .cover(cover).videoUrls(urls).shareUrl(shareUrl).build();
        } catch (Exception e) {
            log.warn("旧版 API 失败: {}", e.getMessage());
            return null;
        }
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