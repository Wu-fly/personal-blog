package com.blog.repository;

import com.blog.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ArticleTag Repository
 * Handles database operations for ArticleTag entity
 */
@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    
    /**
     * Find all tags for an article
     */
    @Query("SELECT at FROM ArticleTag at WHERE at.articleId = :articleId")
    List<ArticleTag> findByArticleId(@Param("articleId") Long articleId);
    
    /**
     * Find all articles with a specific tag
     */
    @Query("SELECT at FROM ArticleTag at WHERE at.tagId = :tagId")
    List<ArticleTag> findByTagId(@Param("tagId") Long tagId);
    
    /**
     * Delete all tags for an article
     */
    void deleteByArticleId(Long articleId);
}
