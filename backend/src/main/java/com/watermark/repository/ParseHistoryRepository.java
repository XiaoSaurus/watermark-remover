package com.watermark.repository;

import com.watermark.model.ParseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ParseHistoryRepository extends JpaRepository<ParseHistory, Long> {

    Page<ParseHistory> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    @Modifying @Transactional
    @Query("UPDATE ParseHistory p SET p.deleted = true WHERE p.id = :id")
    void softDelete(Long id);

    @Modifying @Transactional
    @Query("UPDATE ParseHistory p SET p.deleted = true")
    void softDeleteAll();
}
