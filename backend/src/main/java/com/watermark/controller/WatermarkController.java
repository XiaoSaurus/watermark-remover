package com.watermark.controller;

import com.watermark.model.Result;
import com.watermark.model.VideoInfo;
import com.watermark.service.WatermarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Slf4j
@Tag(name = "短视频去水印")
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class WatermarkController {

    private final WatermarkService watermarkService;

    private static final OkHttpClient DOWNLOAD_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    @Operation(summary = "解析分享链接，获取无水印视频地址")
    @PostMapping("/parse")
    public Result<VideoInfo> parse(@RequestBody ParseRequest request) {
        try {
            VideoInfo info = watermarkService.parseVideo(request.getUrl());
            return Result.success(info);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "代理下载视频（解决403问题）")
    @GetMapping("/download")
    public void download(
            @RequestParam String url,
            @RequestParam(defaultValue = "video.mp4") String filename,
            HttpServletResponse response) {
        try {
            log.info("代理下载: {}", url.substring(0, Math.min(80, url.length())));

            // 根据平台设置合适的 Referer，避免 403
            String referer = getRefererForUrl(url);
            Request req = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Referer", referer)
                    .header("Accept", "*/*")
                    .header("Accept-Language", "zh-CN,zh;q=0.9")
                    .header("Range", "bytes=0-")
                    .build();

            try (Response upstream = DOWNLOAD_CLIENT.newCall(req).execute()) {
                if (!upstream.isSuccessful()) {
                    response.sendError(upstream.code(), "上游服务器返回 " + upstream.code());
                    return;
                }

                String contentType = upstream.header("Content-Type", "video/mp4");
                long contentLength = upstream.body().contentLength();

                response.setContentType(contentType);
                if (contentLength > 0) response.setContentLengthLong(contentLength);

                // 触发浏览器下载
                String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                        .replace("+", "%20");
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + encodedName + "\"; filename*=UTF-8''" + encodedName);
                response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

                // 流式转发
                try (InputStream in = upstream.body().byteStream();
                     OutputStream out = response.getOutputStream()) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        out.write(buf, 0, n);
                    }
                    out.flush();
                }
            }
        } catch (Exception e) {
            log.error("代理下载失败: {}", e.getMessage());
            try { response.sendError(500, e.getMessage()); } catch (Exception ignored) {}
        }
    }

    @lombok.Data
    public static class ParseRequest {
        private String url;
    }

    /** 根据视频 URL 返回对应的 Referer，避免 403 */
    private String getRefererForUrl(String url) {
        if (url == null) return "https://www.douyin.com/";
        if (url.contains("douyin.com") || url.contains("iesdouyin.com")) return "https://www.douyin.com/";
        if (url.contains("kuaishou.com")) return "https://www.kuaishou.com/";
        if (url.contains("bilibili.com") || url.contains("b23.tv")) return "https://www.bilibili.com/";
        if (url.contains("weibo.com") || url.contains("weibo.cn") || url.contains("t.cn")) return "https://weibo.com/";
        if (url.contains("xiaohongshu.com") || url.contains("xhslink.com")) return "https://www.xiaohongshu.com/";
        return "https://www.douyin.com/";
    }
}