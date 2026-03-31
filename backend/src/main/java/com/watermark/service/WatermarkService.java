package com.watermark.service;

import com.watermark.model.WatermarkTask;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WatermarkService {
    WatermarkTask submitTask(MultipartFile file, String type, int x, int y, int width, int height);
    WatermarkTask getTask(String taskId);
    List<WatermarkTask> listTasks();
    byte[] downloadResult(String taskId) throws Exception;
}