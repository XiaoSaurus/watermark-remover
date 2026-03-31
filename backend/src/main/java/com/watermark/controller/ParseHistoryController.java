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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parse-history")
@RequiredArgsConstructor
public class ParseHistoryController {

    private final ParseHistoryService service;
    private final ObjectMapper objectMapper;

    /** 保存解析记录 */
    @PostMapping
    public Result<ParseHistory> save(@RequestBody SaveReq req) throws Exception {
        ParseHistory h = ParseHistory.builder()
                .platform(req.getPlatform())
                .title(req.getTitle())
                .cover(req.getCover())
                .author(req.getAuthor())
                .shareUrl(req.getShareUrl())
                .videoUrlsJson(objectMapper.writeValueAsString(req.getVideoUrls()))
                .client(req.getClient())
                .build();
        return Result.success(service.save(h));
    }

    /** 分页查询（含 videoUrls 反序列化） */
    @GetMapping
    public Result<Page<ParseHistoryVO>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {
        Page<ParseHistory> raw = service.list(page, size);
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

    /** 逻辑删除单条 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.success(null);
    }

    /** 逻辑删除全部 */
    @DeleteMapping
    public Result<Void> clear() {
        service.clear();
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
