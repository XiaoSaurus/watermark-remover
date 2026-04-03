package com.watermark.service.impl;

import com.watermark.model.ParseHistory;
import com.watermark.repository.ParseHistoryRepository;
import com.watermark.service.ParseHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class ParseHistoryServiceImpl implements ParseHistoryService {

    private final ParseHistoryRepository repo;

    @Override public ParseHistory save(Long userId, ParseHistory h) {
        h.setUserId(userId);
        return repo.save(h);
    }

    @Override public Page<ParseHistory> list(Long userId, int page, int size) {
        // 未登录时返回所有记录，已登录时返回当前用户的记录
        if (userId == null) {
            return repo.findByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(page, size));
        }
        return repo.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    @Override public void delete(Long userId, Long id) { repo.softDeleteByIdAndUserId(id, userId); }

    @Override public void clear(Long userId) { repo.softDeleteAllByUserId(userId); }
}
