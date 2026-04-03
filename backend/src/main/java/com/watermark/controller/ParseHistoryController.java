package com.watermark.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.watermark.model.ParseHistory;
import com.watermark.model.Result;
import com.watermark.model.VideoUrl;
import com.watermark.service.ParseHistoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parse-history")
@RequiredArgsConstructor
public class ParseHistoryController {

    private final ParseHistoryService service;
    private final ObjectMapper objectMapper;

    /** 从 Authentication 中获取用户ID，未登录返回 null */
    private Long getUserId(Authentication auth) {
        if (auth == null) return null;
        try { return Long.parseLong(auth.getName()); }
        catch (Exception e) { return null; }
    }

    /** 保存解析记录（需要认证） */
    @PostMapping
    public Result<ParseHistory> save(Authentication auth, @RequestBody SaveReq req) throws Exception {
        Long userId = getUserId(auth);
        if (userId == null) return Result.error(401, "请先登录");
        ParseHistory h = ParseHistory.builder()
                .platform(req.getPlatform())
                .title(req.getTitle())
                .cover(req.getCover())
                .author(req.getAuthor())
                .shareUrl(req.getShareUrl())
                .videoUrlsJson(objectMapper.writeValueAsString(req.getVideoUrls()))
                .client(req.getClient())
                .build();
        return Result.success(service.save(userId, h));
    }

    /** 分页查询（需要认证，含 videoUrls 反序列化） */
    @GetMapping
    public Result<Page<ParseHistoryVO>> list(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {
        Long userId = getUserId(auth);
        Page<ParseHistory> raw = service.list(userId, page, size);
        Page<ParseHistoryVO> vo = raw.map(h -> {
            ParseHistoryVO v = new ParseHistoryVO();
            v.setId(h.getId());
            v.setPlatform(h.getPlatform());
            v.setTitle(h.getTitle());
            v.setCover(h.getCover());
            v.setAuthor(h.getAuthor());
            v.setShareUrl(h.getShareUrl());
            v.setClient(h.getClient());
            v.setCreatedAt(h.getCreatedAt() != null ? h.getCreatedAt().toString() : null);
            try {
                if (h.getVideoUrlsJson() != null) {
                    v.setVideoUrls(objectMapper.readValue(h.getVideoUrlsJson(),
                            new TypeReference<List<Map<String, String>>>() {}));
                }
            } catch (Exception ignored) {}
            return v;
        });
        return Result.success(vo);
    }

    /** 逻辑删除单条（需要认证） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        Long userId = getUserId(auth);
        service.delete(userId, id);
        return Result.success(null);
    }

    /** 逻辑删除全部（需要认证） */
    @DeleteMapping
    public Result<Void> clear(Authentication auth) {
        Long userId = getUserId(auth);
        service.clear(userId);
        return Result.success(null);
    }

    @Data
    public static class SaveReq {
        private String platform;
        private String title;
        private String cover;
        private String author;
        private String shareUrl;
        private List<Map<String, String>> videoUrls;
        private String client;
    }

    @Data
    public static class ParseHistoryVO {
        private Long id;
        private String platform;
        private String title;
        private String cover;
        private String author;
        private String shareUrl;
        private List<Map<String, String>> videoUrls;
        private String client;
        private String createdAt;
    }
}
