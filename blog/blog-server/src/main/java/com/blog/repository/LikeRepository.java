package com.blog.repository;

import com.blog.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Like Repository
 * Handles database operations for Like entity
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    
    /**
     * Find like by user and article
     */
    Optional<Like> findByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * Check if user liked an article
     */
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * Find all likes by user
     */
    Page<Like> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find all likes by user ordered by created date descending
     */
    @EntityGraph(attributePaths = {"user", "article", "article.user", "article.category"})
    Page<Like> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Count likes for an article
     */
    long countByArticleId(Long articleId);
    
    /**
     * Delete like by user and article
     */
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
}
