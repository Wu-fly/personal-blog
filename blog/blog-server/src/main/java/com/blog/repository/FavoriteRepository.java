package com.blog.repository;

import com.blog.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Favorite Repository
 * Handles database operations for Favorite entity
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    /**
     * Find favorite by user and article
     */
    Optional<Favorite> findByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * Check if user favorited an article
     */
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * Find all favorites by user
     */
    @EntityGraph(attributePaths = {"user", "article", "article.user", "article.category"})
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Count favorites for an article
     */
    long countByArticleId(Long articleId);
    
    /**
     * Delete favorite by user and article
     */
    void deleteByUserIdAndArticleId(Long userId, Long articleId);
}
