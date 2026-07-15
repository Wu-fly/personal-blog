package com.blog.repository;

import com.blog.entity.CarouselConfig;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * CarouselConfig Repository
 * Handles database operations for CarouselConfig entity
 */
@Repository
public interface CarouselConfigRepository extends JpaRepository<CarouselConfig, Long> {
    
    /**
     * Find all carousel items ordered by display order (with article eagerly loaded)
     */
    @EntityGraph(attributePaths = {"article", "article.user", "article.category"})
    List<CarouselConfig> findAllByOrderByDisplayOrderAsc();
    
    /**
     * Find carousel config by article ID
     */
    Optional<CarouselConfig> findByArticleId(Long articleId);
    
    /**
     * Check if article is in carousel
     */
    boolean existsByArticleId(Long articleId);
    
    /**
     * Delete carousel config by article ID
     */
    void deleteByArticleId(Long articleId);
    
    /**
     * Get maximum display order
     */
    @Query("SELECT MAX(c.displayOrder) FROM CarouselConfig c")
    Integer findMaxDisplayOrder();
    
    /**
     * Count carousel items
     */
    long count();
}
