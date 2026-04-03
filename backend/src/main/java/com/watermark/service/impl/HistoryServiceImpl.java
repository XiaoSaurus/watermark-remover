package com.watermark.service.impl;

import com.watermark.model.DownloadHistory;
import com.watermark.repository.DownloadHistoryRepository;
import com.watermark.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final DownloadHistoryRepository repo;

    @Override public DownloadHistory save(Long userId, DownloadHistory h) {
        h.setUserId(userId);
        return repo.save(h);
    }

    @Override public Page<DownloadHistory> list(Long userId, int page, int size) {
        if (userId == null) {
            return repo.findByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        return repo.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    @Override public void delete(Long userId, Long id) { repo.softDeleteByIdAndUserId(id, userId); }

    @Override public void clear(Long userId) { repo.softDeleteAllByUserId(userId); }
}
