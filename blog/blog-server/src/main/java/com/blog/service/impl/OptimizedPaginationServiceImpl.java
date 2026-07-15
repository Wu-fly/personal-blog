package com.blog.service.impl;

import com.blog.dto.ArticleListDTO;
import com.blog.entity.Article;
import com.blog.exception.BusinessException;
import com.blog.repository.OptimizedArticleRepository;
import com.blog.service.OptimizedPaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Optimized Pagination Service Implementation
 * 
 * Implements various pagination optimization strategies:
 * 1. Cursor-based pagination for infinite scroll
 * 2. Limited offset pagination with covering index
 * 3. DTO projection to reduce data transfer
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OptimizedPaginationServiceImpl implements OptimizedPaginationService {
    
    private final OptimizedArticleRepository optimizedArticleRepository;
    
    /**
     * Maximum allowed page number for offset pagination
     * Prevents deep pagination performance issues
     */
    private static final int MAX_PAGE_NUMBER = 100;
    
    /**
     * Default page size
     */
    private static final int DEFAULT_PAGE_SIZE = 20;
    
    @Override
    public List<Article> getArticlesWithCursor(LocalDateTime cursor, int pageSize, String sortBy) {
        log.debug("Getting articles with cursor: {}, pageSize: {}, sortBy: {}", cursor, pageSize, sortBy);
        
        // Validate page size
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        
        Pageable pageable = PageRequest.of(0, pageSize);
        
        // If cursor is null, this is the first page
        if (cursor == null) {
            cursor = LocalDateTime.now();
        }
        
        // Use cursor-based pagination based on sort field
        if ("created_at".equals(sortBy) || sortBy == null) {
            return optimizedArticleRepository.findApprovedArticlesAfterCursor(cursor, pageable);
        } else {
            // For other sort fields, use the numeric cursor method
            throw new BusinessException("Use getArticlesWithNumericCursor for non-timestamp sorting");
        }
    }
    
    @Override
    public List<Article> getArticlesWithNumericCursor(
            Integer cursorValue, 
            Long cursorId, 
            int pageSize, 
            String sortBy) {
        
        log.debug("Getting articles with numeric cursor: value={}, id={}, pageSize={}, sortBy={}", 
                  cursorValue, cursorId, pageSize, sortBy);
        
        // Validate page size
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        
        Pageable pageable = PageRequest.of(0, pageSize);
        
        // If cursor is null, this is the first page - use max values
        if (cursorValue == null || cursorId == null) {
            cursorValue = Integer.MAX_VALUE;
            cursorId = Long.MAX_VALUE;
        }
        
        // Use appropriate cursor-based query based on sort field
        switch (sortBy) {
            case "view_count":
                return optimizedArticleRepository.findApprovedArticlesByViewCountAfterCursor(
                    cursorValue, cursorId, pageable);
            
            case "favorite_count":
                return optimizedArticleRepository.findApprovedArticlesByFavoriteCountAfterCursor(
                    cursorValue, cursorId, pageable);
            
            case "purchase_count":
                return optimizedArticleRepository.findApprovedArticlesByPurchaseCountAfterCursor(
                    cursorValue, cursorId, pageable);
            
            default:
                throw new BusinessException("Invalid sort field: " + sortBy);
        }
    }
    
    @Override
    public List<Article> getArticlesByCategoryWithCursor(
            Long categoryId, 
            LocalDateTime cursor, 
            int pageSize) {
        
        log.debug("Getting articles by category with cursor: categoryId={}, cursor={}, pageSize={}", 
                  categoryId, cursor, pageSize);
        
        // Validate page size
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        
        Pageable pageable = PageRequest.of(0, pageSize);
        
        // If cursor is null, this is the first page
        if (cursor == null) {
            cursor = LocalDateTime.now();
        }
        
        return optimizedArticleRepository.findApprovedArticlesByCategoryAfterCursor(
            categoryId, cursor, pageable);
    }
    
    @Override
    public List<Article> getArticlesWithOptimizedOffset(int page, int pageSize, int maxPage) {
        log.debug("Getting articles with optimized offset: page={}, pageSize={}, maxPage={}", 
                  page, pageSize, maxPage);
        
        // Validate page number
        if (page < 0) {
            page = 0;
        }
        
        // Enforce maximum page limit
        if (maxPage <= 0) {
            maxPage = MAX_PAGE_NUMBER;
        }
        
        if (page > maxPage) {
            throw new BusinessException("Page number exceeds maximum allowed: " + maxPage);
        }
        
        // Validate page size
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        
        int offset = page * pageSize;
        
        // Use optimized query with covering index
        return optimizedArticleRepository.findApprovedArticlesOptimized(pageSize, offset);
    }
    
    @Override
    public List<ArticleListDTO> getArticleListAsDTO(int page, int pageSize) {
        log.debug("Getting article list as DTO: page={}, pageSize={}", page, pageSize);
        
        // Validate page number
        if (page < 0) {
            page = 0;
        }
        
        // Enforce maximum page limit
        if (page > MAX_PAGE_NUMBER) {
            throw new BusinessException("Page number exceeds maximum allowed: " + MAX_PAGE_NUMBER);
        }
        
        // Validate page size
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        
        Pageable pageable = PageRequest.of(page, pageSize);
        
        // Use DTO projection to reduce data transfer
        List<Object[]> results = optimizedArticleRepository.findApprovedArticlesAsDTO(pageable);
        
        // Convert Object[] to ArticleListDTO
        // Note: This is a placeholder - actual implementation depends on query result structure
        return Collections.emptyList(); // TODO: Implement conversion
    }
}
