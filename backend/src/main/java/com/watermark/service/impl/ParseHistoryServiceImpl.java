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

    @Override public ParseHistory save(ParseHistory h) { return repo.save(h); }

    @Override public Page<ParseHistory> list(int page, int size) {
        return repo.findByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Override public void delete(Long id) { repo.softDelete(id); }

    @Override public void clear() { repo.softDeleteAll(); }
}
