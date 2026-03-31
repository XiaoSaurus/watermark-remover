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
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class XiaohongshuParser implements VideoParser {

    @Override
    public boolean supports(String url) {
        return url.contains("xiaohongshu.com") || url.contains("xhslink.com");
    }

    @Override
    public VideoInfo parse(String url) throws Exception {
        String realUrl = url;
        if (url.contains("xhslink.com")) {
            realUrl = HttpUtil.resolveRedirect(url);
            log.info("小红书真实URL: {}", realUrl);
        }

        // 提取笔记 ID
        String noteId = extractNoteId(realUrl);
        if (noteId == null) throw new Exception("无法提取小红书笔记ID");

        // 小红书需要解析页面中的 __INITIAL_STATE__
        String html = HttpUtil.get(realUrl, HttpUtil.UA_MOBILE);
        Document doc = Jsoup.parse(html);

        String title = "";
        String author = "";
        String cover = "";
        List<VideoUrl> videoUrls = new ArrayList<>();

        // 从页面 script 中提取数据
        for (Element script : doc.select("script")) {
            String scriptText = script.html();
            if (scriptText.contains("__INITIAL_STATE__")) {
                // 提取 JSON 数据
                Pattern p = Pattern.compile("window\\.__INITIAL_STATE__\\s*=\\s*(\\{.+\\})");
                Matcher m = p.matcher(scriptText);
                if (m.find()) {
                    JSONObject state = JSON.parseObject(m.group(1));
                    try {
                        JSONObject note = state.getJSONObject("note").getJSONObject("noteDetailMap")
                                .getJSONObject(noteId).getJSONObject("note");
                        title = note.getString("title");
                        author = note.getJSONObject("user").getString("nickname");
                        cover = note.getString("cover");

                        // 视频信息
                        JSONObject video = note.getJSONObject("video");
                        if (video != null) {
                            JSONObject media = video.getJSONObject("media");
                            if (media != null) {
                                String videoUrl = media.getString("url");
                                if (videoUrl != null) {
                                    videoUrls.add(new VideoUrl("原画", videoUrl));
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("小红书数据解析异常: {}", e.getMessage());
                    }
                }
                break;
            }
        }

        if (videoUrls.isEmpty()) throw new Exception("未找到视频资源，该笔记可能不含视频");

        return VideoInfo.builder()
                .platform("xiaohongshu")
                .title(title)
                .author(author)
                .cover(cover)
                .videoUrls(videoUrls)
                .shareUrl(url)
                .build();
    }

    private String extractNoteId(String url) {
        Pattern p = Pattern.compile("/explore/([a-f0-9]+)");
        Matcher m = p.matcher(url);
        if (m.find()) return m.group(1);
        p = Pattern.compile("/discovery/item/([a-f0-9]+)");
        m = p.matcher(url);
        if (m.find()) return m.group(1);
        return null;
    }
}