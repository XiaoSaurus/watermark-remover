package com.watermark.controller;

import com.watermark.model.Result;
import com.watermark.model.WatermarkTask;
import com.watermark.service.WatermarkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "去水印接口")
@RestController
@RequestMapping("/api/watermark")
@RequiredArgsConstructor
public class WatermarkController {

    private final WatermarkService watermarkService;

    @Operation(summary = "上传文件并提交去水印任务")
    @PostMapping("/submit")
    public Result<WatermarkTask> submit(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "image") String type,
            @RequestParam(value = "x", defaultValue = "0") int x,
            @RequestParam(value = "y", defaultValue = "0") int y,
            @RequestParam(value = "width", defaultValue = "100") int width,
            @RequestParam(value = "height", defaultValue = "50") int height) {
        WatermarkTask task = watermarkService.submitTask(file, type, x, y, width, height);
        return Result.success(task);
    }

    @Operation(summary = "查询任务状态")
    @GetMapping("/task/{taskId}")
    public Result<WatermarkTask> getTask(@PathVariable String taskId) {
        WatermarkTask task = watermarkService.getTask(taskId);
        if (task == null) return Result.error("任务不存在");
        return Result.success(task);
    }

    @Operation(summary = "获取所有任务列表")
    @GetMapping("/tasks")
    public Result<List<WatermarkTask>> listTasks() {
        return Result.success(watermarkService.listTasks());
    }

    @Operation(summary = "下载处理结果")
    @GetMapping("/download/{taskId}")
    public ResponseEntity<byte[]> download(@PathVariable String taskId) throws Exception {
        byte[] data = watermarkService.downloadResult(taskId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=result_" + taskId)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}