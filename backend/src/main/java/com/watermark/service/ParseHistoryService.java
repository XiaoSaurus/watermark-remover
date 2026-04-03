package com.watermark.service;

import com.watermark.model.ParseHistory;
import org.springframework.data.domain.Page;

public interface ParseHistoryService {
    ParseHistory save(Long userId, ParseHistory h);
    Page<ParseHistory> list(Long userId, int page, int size);
    void delete(Long userId, Long id);
    void clear(Long userId);
}
