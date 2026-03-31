package com.watermark.parser;

import okhttp3.*;
import java.util.concurrent.TimeUnit;

public class HttpUtil {

    private static final OkHttpClient CLIENT = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    public static final String UA_MOBILE =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) " +
            "AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";

    public static final String UA_PC =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /** GET 请求，返回响应体字符串 */
    public static String get(String url, String userAgent) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Referer", "https://www.douyin.com/")
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new Exception("HTTP " + response.code());
            return response.body().string();
        }
    }

    /** 跟随重定向，返回最终真实 URL */
    public static String resolveRedirect(String shortUrl) throws Exception {
        Request request = new Request.Builder()
                .url(shortUrl)
                .header("User-Agent", UA_MOBILE)
                .build();
        try (Response response = CLIENT.newCall(request).execute()) {
            return response.request().url().toString();
        }
    }

    public static OkHttpClient getClient() {
        return CLIENT;
    }
}