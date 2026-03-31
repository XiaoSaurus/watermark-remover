package com.watermark.parser;

import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    // 不跟随重定向的客户端，用于获取真实跳转 URL
    private static final OkHttpClient NO_REDIRECT_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(false)
            .followSslRedirects(false)
            .build();

    public static final String UA_MOBILE =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) " +
            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1 " +
            "TikTok/26.2.0 JsSdk/2.0";

    public static final String UA_PC =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    /** GET 请求，返回响应体字符串 */
    public static String get(String url, String userAgent) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Referer", "https://www.douyin.com/")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "zh-CN,zh;q=0.9")
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new Exception("HTTP " + response.code());
            return response.body().string();
        }
    }

    /** GET 请求，支持自定义额外 headers */
    public static String getWithHeaders(String url, String userAgent, String[][] extraHeaders) throws Exception {
        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Referer", "https://www.douyin.com/")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "zh-CN,zh;q=0.9");
        if (extraHeaders != null) {
            for (String[] h : extraHeaders) {
                builder.header(h[0], h[1]);
            }
        }
        try (Response response = CLIENT.newCall(builder.build()).execute()) {
            if (!response.isSuccessful()) throw new Exception("HTTP " + response.code());
            return response.body().string();
        }
    }

    /** 跟随重定向，返回最终真实 URL（支持多级跳转） */
    public static String resolveRedirect(String shortUrl) throws Exception {
        String current = shortUrl;
        int maxRedirects = 10;
        for (int i = 0; i < maxRedirects; i++) {
            Request request = new Request.Builder()
                    .url(current)
                    .header("User-Agent", UA_MOBILE)
                    .header("Accept", "text/html,application/xhtml+xml,*/*")
                    .build();
            try (Response response = NO_REDIRECT_CLIENT.newCall(request).execute()) {
                int code = response.code();
                if (code >= 300 && code < 400) {
                    String location = response.header("Location");
                    if (location == null) break;
                    // 处理相对路径
                    if (location.startsWith("/")) {
                        HttpUrl base = HttpUrl.parse(current);
                        location = base.scheme() + "://" + base.host() + location;
                    }
                    current = location;
                } else {
                    // 最终 URL
                    return response.request().url().toString();
                }
            }
        }
        return current;
    }

    public static OkHttpClient getClient() {
        return CLIENT;
    }
}