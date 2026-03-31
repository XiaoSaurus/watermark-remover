package com.watermark.controller;

import com.watermark.model.DownloadHistory;
import com.watermark.model.Result;
import com.watermark.service.HistoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

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

    @GetMapping
    public Result<Page<DownloadHistory>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(historyService.list(page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        historyService.delete(id);
        return Result.success(null);
    }

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
