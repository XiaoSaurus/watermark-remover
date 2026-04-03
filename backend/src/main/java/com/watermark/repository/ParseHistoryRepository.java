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

    /** 按用户ID分页查询 */
    Page<ParseHistory> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying @Transactional
    @Query("UPDATE ParseHistory p SET p.deleted = true WHERE p.id = :id")
    void softDelete(Long id);

    /** 按用户ID软删除单条 */
    @Modifying @Transactional
    @Query("UPDATE ParseHistory p SET p.deleted = true WHERE p.id = :id AND p.userId = :userId")
    void softDeleteByIdAndUserId(Long id, Long userId);

    @Modifying @Transactional
    @Query("UPDATE ParseHistory p SET p.deleted = true")
    void softDeleteAll();

    /** 按用户ID软删除全部 */
    @Modifying @Transactional
    @Query("UPDATE ParseHistory p SET p.deleted = true WHERE p.userId = :userId")
    void softDeleteAllByUserId(Long userId);
}
