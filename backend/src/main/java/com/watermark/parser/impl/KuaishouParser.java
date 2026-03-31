package com.watermark.parser.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.watermark.model.VideoInfo;
import com.watermark.model.VideoUrl;
import com.watermark.parser.HttpUtil;
import com.watermark.parser.VideoParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class KuaishouParser implements VideoParser {

    @Override
    public boolean supports(String url) {
        return url.contains("kuaishou.com") || url.contains("gifshow.com")
                || url.contains("v.kuaishou.com");
    }

    @Override
    public VideoInfo parse(String url) throws Exception {
        // 展开短链
        String realUrl = url;
        if (url.contains("v.kuaishou.com")) {
            realUrl = HttpUtil.resolveRedirect(url);
            log.info("快手真实URL: {}", realUrl);
        }

        // 提取 photoId
        String photoId = extractPhotoId(realUrl);
        if (photoId == null) throw new Exception("无法提取快手视频ID");

        // 调用快手 API
        String apiUrl = "https://www.kuaishou.com/graphql";
        String query = "{\"operationName\":\"visionVideoDetail\",\"variables\":{\"photoId\":\"" + photoId
                + "\",\"page\":\"detail\"},\"query\":\"query visionVideoDetail($photoId: String, $page: String) {\\n  visionVideoDetail(photoId: $photoId, page: $page) {\\n    photo {\\n      id\\n      caption\\n      coverUrl\\n      photoUrl\\n      videoResource {\\n        h264 {\\n          adaptationSet {\\n            representation {\\n              url\\n              qualityLabel\\n            }\\n          }\\n        }\\n      }\\n    }\\n    author {\\n      name\\n    }\\n  }\\n}\\n\"}";

        OkHttpClient client = HttpUtil.getClient();
        RequestBody body = RequestBody.create(query, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(apiUrl)
                .post(body)
                .header("User-Agent", HttpUtil.UA_PC)
                .header("Referer", "https://www.kuaishou.com/")
                .header("Content-Type", "application/json")
                .build();

        String respBody;
        try (Response response = client.newCall(request).execute()) {
            respBody = response.body().string();
        }

        JSONObject json = JSON.parseObject(respBody);
        JSONObject photo = json.getJSONObject("data")
                .getJSONObject("visionVideoDetail").getJSONObject("photo");

        String title = photo.getString("caption");
        String cover = photo.getString("coverUrl");
        String author = json.getJSONObject("data")
                .getJSONObject("visionVideoDetail")
                .getJSONObject("author").getString("name");

        // 获取多清晰度视频
        List<VideoUrl> videoUrls = new ArrayList<>();
        try {
            photo.getJSONObject("videoResource")
                    .getJSONObject("h264")
                    .getJSONArray("adaptationSet")
                    .forEach(set -> {
                        JSONObject s = (JSONObject) set;
                        s.getJSONArray("representation").forEach(rep -> {
                            JSONObject r = (JSONObject) rep;
                            videoUrls.add(new VideoUrl(r.getString("qualityLabel"), r.getString("url")));
                        });
                    });
        } catch (Exception e) {
            // 降级：直接用 photoUrl
            videoUrls.add(new VideoUrl("原画", photo.getString("photoUrl")));
        }

        return VideoInfo.builder()
                .platform("kuaishou")
                .title(title)
                .author(author)
                .cover(cover)
                .videoUrls(videoUrls)
                .shareUrl(url)
                .build();
    }

    private String extractPhotoId(String url) {
        Pattern p = Pattern.compile("/short-video/([a-zA-Z0-9_-]+)");
        Matcher m = p.matcher(url);
        if (m.find()) return m.group(1);
        p = Pattern.compile("[?&]photoId=([a-zA-Z0-9_-]+)");
        m = p.matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }
}