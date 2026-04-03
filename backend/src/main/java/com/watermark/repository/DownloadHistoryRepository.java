package com.watermark.repository;

import com.watermark.model.DownloadHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DownloadHistoryRepository extends JpaRepository<DownloadHistory, Long> {

    Page<DownloadHistory> findByDeletedFalseOrderByCreatedAtDesc(Pageable pageable);

    /** 按用户ID分页查询 */
    Page<DownloadHistory> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying @Transactional
    @Query("UPDATE DownloadHistory d SET d.deleted = true WHERE d.id = :id")
    void softDelete(Long id);

    /** 按用户ID软删除单条 */
    @Modifying @Transactional
    @Query("UPDATE DownloadHistory d SET d.deleted = true WHERE d.id = :id AND d.userId = :userId")
    void softDeleteByIdAndUserId(Long id, Long userId);

    @Modifying @Transactional
    @Query("UPDATE DownloadHistory d SET d.deleted = true")
    void softDeleteAll();

    /** 按用户ID软删除全部 */
    @Modifying @Transactional
    @Query("UPDATE DownloadHistory d SET d.deleted = true WHERE d.userId = :userId")
    void softDeleteAllByUserId(Long userId);
}
