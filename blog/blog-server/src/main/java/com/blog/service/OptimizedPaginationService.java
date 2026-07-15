package com.blog.service;

import com.blog.dto.ArticleListDTO;
import com.blog.entity.Article;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Optimized Pagination Service
 * 
 * Provides optimized pagination methods using cursor-based pagination
 * to avoid deep pagination performance issues.
 */
public interface OptimizedPaginationService {
    
    /**
     * Get articles using cursor-based pagination
     * 
     * @param cursor The timestamp of the last article from previous page (null for first page)
     * @param pageSize Number of articles per page
     * @param sortBy Sort field (created_at, view_count, favorite_count, purchase_count)
     * @return List of articles
     */
    List<Article> getArticlesWithCursor(LocalDateTime cursor, int pageSize, String sortBy);
    
    /**
     * Get articles using cursor-based pagination with numeric cursor
     * 
     * @param cursorValue The value of the sort field (view count, favorite count, etc.)
     * @param cursorId The ID of the last article (for tie-breaking)
     * @param pageSize Number of articles per page
     * @param sortBy Sort field
     * @return List of articles
     */
    List<Article> getArticlesWithNumericCursor(
        Integer cursorValue, 
        Long cursorId, 
        int pageSize, 
        String sortBy
    );
    
    /**
     * Get articles by category using cursor-based pagination
     * 
     * @param categoryId Category ID
     * @param cursor The timestamp of the last article from previous page
     * @param pageSize Number of articles per page
     * @return List of articles
     */
    List<Article> getArticlesByCategoryWithCursor(
        Long categoryId, 
        LocalDateTime cursor, 
        int pageSize
    );
    
    /**
     * Get articles using optimized offset pagination (for limited pages)
     * 
     * @param page Page number (0-based)
     * @param pageSize Number of articles per page
     * @param maxPage Maximum allowed page number
     * @return List of articles
     */
    List<Article> getArticlesWithOptimizedOffset(int page, int pageSize, int maxPage);
    
    /**
     * Get article list as DTO (lightweight)
     * 
     * @param page Page number
     * @param pageSize Number of articles per page
     * @return List of article DTOs
     */
    List<ArticleListDTO> getArticleListAsDTO(int page, int pageSize);
}
