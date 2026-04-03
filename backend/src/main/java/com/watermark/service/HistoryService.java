package com.watermark.service;

import com.watermark.model.DownloadHistory;
import org.springframework.data.domain.Page;

public interface HistoryService {
    DownloadHistory save(Long userId, DownloadHistory history);
    Page<DownloadHistory> list(Long userId, int page, int size);
    void delete(Long userId, Long id);
    void clear(Long userId);
}
