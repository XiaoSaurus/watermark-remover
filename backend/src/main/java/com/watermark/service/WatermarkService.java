package com.watermark.service;

import com.watermark.model.VideoInfo;

public interface WatermarkService {
    VideoInfo parseVideo(String shareUrl) throws Exception;
}