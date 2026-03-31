package com.watermark.controller;

import com.watermark.model.Result;
import com.watermark.model.VideoInfo;
import com.watermark.service.WatermarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "短视频去水印")
@RestController
@RequestMapping("/api/video")
@RequiredArgsConstructor
public class WatermarkController {

    private final WatermarkService watermarkService;

    @Operation(summary = "解析分享链接，获取无水印视频地址",
               description = "支持抖音、快手、B站、微博、小红书。可直接粘贴分享文案，自动提取链接。")
    @PostMapping("/parse")
    public Result<VideoInfo> parse(@RequestBody ParseRequest request) {
        try {
            VideoInfo info = watermarkService.parseVideo(request.getUrl());
            return Result.success(info);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @lombok.Data
    public static class ParseRequest {
        private String url;
    }
}