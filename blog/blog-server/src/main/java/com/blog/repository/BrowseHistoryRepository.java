package com.blog.repository;

import com.blog.entity.BrowseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * BrowseHistory Repository
 * Handles database operations for BrowseHistory entity
 */
@Repository
public interface BrowseHistoryRepository extends JpaRepository<BrowseHistory, Long> {
    
    /**
     * Find browse history by user ID
     */
    @EntityGraph(attributePaths = {"user", "article", "article.user", "article.category"})
    Page<BrowseHistory> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find browse history record for user and article
     */
    Optional<BrowseHistory> findByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * Delete browse history by user and article
     */
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * Delete all browse history for a user
     */
    void deleteByUserId(Long userId);
    
    /**
     * Update timestamp for existing browse history
     */
    @Modifying
    @Query("UPDATE BrowseHistory bh SET bh.updatedAt = CURRENT_TIMESTAMP WHERE bh.userId = :userId AND bh.articleId = :articleId")
    void updateTimestamp(@Param("userId") Long userId, @Param("articleId") Long articleId);
    
    /**
     * Count browse history records for a user
     */
    long countByUserId(Long userId);
}
