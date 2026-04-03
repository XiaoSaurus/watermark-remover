package com.watermark.controller;

import com.watermark.model.DownloadHistory;
import com.watermark.model.Result;
import com.watermark.service.HistoryService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    /** 从 Authentication 中获取用户ID，未登录返回 null */
    private Long getUserId(Authentication auth) {
        if (auth == null) return null;
        try { return Long.parseLong(auth.getName()); }
        catch (Exception e) { return null; }
    }

    @PostMapping
    public Result<DownloadHistory> save(Authentication auth, @Valid @RequestBody SaveRequest req) {
        Long userId = getUserId(auth);
        if (userId == null) return Result.error(401, "请先登录");
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
        return Result.success(historyService.save(userId, h));
    }

    @GetMapping
    public Result<Page<DownloadHistory>> list(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = getUserId(auth);
        return Result.success(historyService.list(userId, page, size));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = getUserId(auth);
        historyService.delete(userId, id);
        return Result.success(null);
    }

    @DeleteMapping
    public Result<Void> clear(Authentication auth) {
        Long userId = getUserId(auth);
        historyService.clear(userId);
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
