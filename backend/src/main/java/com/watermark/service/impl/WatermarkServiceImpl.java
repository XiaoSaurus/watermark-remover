package com.watermark.service.impl;

import com.watermark.model.WatermarkTask;
import com.watermark.service.WatermarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class WatermarkServiceImpl implements WatermarkService {

    @Value("${watermark.upload-dir}")
    private String uploadDir;

    @Value("${watermark.output-dir}")
    private String outputDir;

    @Value("${watermark.ffmpeg-path}")
    private String ffmpegPath;

    private final Map<String, WatermarkTask> taskStore = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public WatermarkTask submitTask(MultipartFile file, String type, int x, int y, int width, int height) {
        String taskId = UUID.randomUUID().toString();
        String originalName = file.getOriginalFilename();

        WatermarkTask task = new WatermarkTask();
        task.setId(taskId);
        task.setOriginalFileName(originalName);
        task.setFileType(type);
        task.setStatus("pending");
        task.setCreatedAt(LocalDateTime.now());
        taskStore.put(taskId, task);

        executor.submit(() -> processTask(task, file, type, x, y, width, height));
        return task;
    }

    private void processTask(WatermarkTask task, MultipartFile file, String type,
                              int x, int y, int width, int height) {
        try {
            task.setStatus("processing");
            Path uploadPath = Paths.get(uploadDir);
            Path outputPath = Paths.get(outputDir);
            Files.createDirectories(uploadPath);
            Files.createDirectories(outputPath);

            String ext = getExtension(task.getOriginalFileName());
            Path inputFile = uploadPath.resolve(task.getId() + "." + ext);
            Path outputFile = outputPath.resolve(task.getId() + "_out." + ext);

            file.transferTo(inputFile.toFile());

            if ("video".equals(type)) {
                removeVideoWatermark(inputFile.toString(), outputFile.toString(), x, y, width, height);
            } else {
                removeImageWatermark(inputFile.toString(), outputFile.toString(), x, y, width, height);
            }

            task.setOutputPath(outputFile.toString());
            task.setStatus("done");
            task.setFinishedAt(LocalDateTime.now());
            log.info("Task {} completed", task.getId());
        } catch (Exception e) {
            task.setStatus("failed");
            task.setErrorMsg(e.getMessage());
            log.error("Task {} failed: {}", task.getId(), e.getMessage());
        }
    }

    private void removeVideoWatermark(String input, String output, int x, int y, int w, int h) throws Exception {
        // 使用 FFmpeg delogo 滤镜去除视频水印
        String filter = String.format("delogo=x=%d:y=%d:w=%d:h=%d", x, y, w, h);
        ProcessBuilder pb = new ProcessBuilder(
            ffmpegPath, "-i", input, "-vf", filter, "-c:a", "copy", output, "-y"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg 处理失败，退出码: " + exitCode);
        }
    }

    private void removeImageWatermark(String input, String output, int x, int y, int w, int h) throws Exception {
        // 使用 FFmpeg 对图片水印区域进行模糊处理
        String filter = String.format(
            "crop=%d:%d:%d:%d,boxblur=10:10[blurred];[0:v][blurred]overlay=%d:%d",
            w, h, x, y, x, y
        );
        ProcessBuilder pb = new ProcessBuilder(
            ffmpegPath, "-i", input, "-filter_complex", filter, output, "-y"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("图片处理失败，退出码: " + exitCode);
        }
    }

    @Override
    public WatermarkTask getTask(String taskId) {
        return taskStore.get(taskId);
    }

    @Override
    public List<WatermarkTask> listTasks() {
        return new ArrayList<>(taskStore.values());
    }

    @Override
    public byte[] downloadResult(String taskId) throws Exception {
        WatermarkTask task = taskStore.get(taskId);
        if (task == null || !"done".equals(task.getStatus())) {
            throw new RuntimeException("任务不存在或未完成");
        }
        return Files.readAllBytes(Paths.get(task.getOutputPath()));
    }

    private String getExtension(String filename) {
        if (filename == null) return "tmp";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "tmp";
    }
}