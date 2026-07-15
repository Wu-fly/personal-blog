package com.blog.repository;

import com.blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Tag Repository
 * Handles database operations for Tag entity
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    
    /**
     * Find tag by name
     */
    Optional<Tag> findByName(String name);
    
    /**
     * Check if tag name exists
     */
    boolean existsByName(String name);
    
    /**
     * Get top N most used tags
     * Returns tags ordered by the number of articles they are associated with
     */
    @Query("SELECT t FROM Tag t " +
           "LEFT JOIN ArticleTag at ON t.id = at.tagId " +
           "GROUP BY t.id " +
           "ORDER BY COUNT(at.articleId) DESC")
    List<Tag> findTopUsedTags(org.springframework.data.domain.Pageable pageable);
}
