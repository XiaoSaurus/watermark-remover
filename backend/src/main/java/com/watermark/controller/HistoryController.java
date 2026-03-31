package com.watermark.controller;

import com.watermark.model.DownloadHistory;
import com.watermark.model.Result;
import com.watermark.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "下载历史")
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @Operation(summary = "保存下载记录")
    @PostMapping
    public Result<DownloadHistory> save(@RequestBody SaveRequest req) {
        DownloadHistory h = DownloadHistory.builder()
                .platform(req.getPlatform())
                .title(req.getTitle())
                .cover(req.getCover())
                .quality(req.getQuality())
                .videoUrl(req.getVideoUrl())
                .shareUrl(req.getShareUrl())
                .status(req.getStatus())
                .errMsg(req.getErrMsg())
                .client(req.getClient())
                .build();
        return Result.success(historyService.save(h));
    }

    @Operation(summary = "获取历史记录列表（分页）")
    @GetMapping
    public Result<Page<DownloadHistory>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(historyService.list(page, size));
    }

    @Operation(summary = "删除单条记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        historyService.delete(id);
        return Result.success(null);
    }

    @Operation(summary = "清空所有记录")
    @DeleteMapping
    public Result<Void> clear() {
        historyService.clear();
        return Result.success(null);
    }

    @Data
    public static class SaveRequest {
        private String platform;
        private String title;
        private String cover;
        private String quality;
        private String videoUrl;
        private String shareUrl;
        private String status;
        private String errMsg;
        private String client;
    }
}
