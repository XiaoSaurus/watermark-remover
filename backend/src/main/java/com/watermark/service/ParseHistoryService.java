package com.watermark.service;

import com.watermark.model.ParseHistory;
import org.springframework.data.domain.Page;

public interface ParseHistoryService {
    ParseHistory save(ParseHistory h);
    Page<ParseHistory> list(int page, int size);
    void delete(Long id);
    void clear();
}
