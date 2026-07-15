package com.blog.repository;

import com.blog.entity.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Purchase Repository
 * Handles database operations for Purchase entity
 */
@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    
    /**
     * Find purchase by user and article
     */
    Optional<Purchase> findByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * Check if user purchased an article
     */
    boolean existsByUserIdAndArticleId(Long userId, Long articleId);
    
    /**
     * Find all purchases by user
     */
    @EntityGraph(attributePaths = {"user", "article", "article.user", "article.category"})
    Page<Purchase> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find all purchases for an article
     */
    Page<Purchase> findByArticleId(Long articleId, Pageable pageable);
    
    /**
     * Count purchases for an article
     */
    long countByArticleId(Long articleId);
}
