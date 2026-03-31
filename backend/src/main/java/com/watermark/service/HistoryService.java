package com.watermark.service;

import com.watermark.model.DownloadHistory;
import org.springframework.data.domain.Page;

public interface HistoryService {
    DownloadHistory save(DownloadHistory history);
    Page<DownloadHistory> list(int page, int size);
    void delete(Long id);
    void clear();
}
