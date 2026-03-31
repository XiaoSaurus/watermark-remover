package com.watermark.service.impl;

import com.watermark.model.DownloadHistory;
import com.watermark.repository.DownloadHistoryRepository;
import com.watermark.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {

    private final DownloadHistoryRepository repo;

    @Override
    public DownloadHistory save(DownloadHistory history) {
        return repo.save(history);
    }

    @Override
    public Page<DownloadHistory> list(int page, int size) {
        return repo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Override
    public void clear() {
        repo.deleteAll();
    }
}
