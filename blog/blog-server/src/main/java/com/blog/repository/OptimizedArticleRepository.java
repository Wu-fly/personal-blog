package com.blog.repository;

import com.blog.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Optimized Article Repository
 * 
 * This repository provides optimized query methods using cursor-based pagination
 * to avoid deep pagination performance issues.
 * 
 * Cursor-based pagination uses the last record's ID or timestamp as a cursor
 * instead of OFFSET, which significantly improves performance for large datasets.
 */
@Repository
public interface OptimizedArticleRepository extends JpaRepository<Article, Long> {
    
    /**
     * Find approved articles using cursor-based pagination (by created_at)
     * 
     * @param cursor The timestamp of the last article from previous page
     * @param pageable Page size configuration
     * @return List of articles after the cursor
     */
    @Query("SELECT a FROM Article a " +
           "WHERE a.reviewStatus = 'APPROVED' " +
           "AND a.createdAt < :cursor " +
           "ORDER BY a.createdAt DESC")
    List<Article> findApprovedArticlesAfterCursor(
        @Param("cursor") LocalDateTime cursor, 
        Pageable pageable
    );
    
    /**
     * Find approved articles using cursor-based pagination (by view count)
     * 
     * @param cursorViewCount The view count of the last article
     * @param cursorId The ID of the last article (for tie-breaking)
     * @param pageable Page size configuration
     * @return List of articles after the cursor
     */
    @Query("SELECT a FROM Article a " +
           "WHERE a.reviewStatus = 'APPROVED' " +
           "AND (a.viewCount < :cursorViewCount " +
           "     OR (a.viewCount = :cursorViewCount AND a.id < :cursorId)) " +
           "ORDER BY a.viewCount DESC, a.id DESC")
    List<Article> findApprovedArticlesByViewCountAfterCursor(
        @Param("cursorViewCount") Integer cursorViewCount,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );
    
    /**
     * Find approved articles using cursor-based pagination (by favorite count)
     * 
     * @param cursorFavoriteCount The favorite count of the last article
     * @param cursorId The ID of the last article (for tie-breaking)
     * @param pageable Page size configuration
     * @return List of articles after the cursor
     */
    @Query("SELECT a FROM Article a " +
           "WHERE a.reviewStatus = 'APPROVED' " +
           "AND (a.favoriteCount < :cursorFavoriteCount " +
           "     OR (a.favoriteCount = :cursorFavoriteCount AND a.id < :cursorId)) " +
           "ORDER BY a.favoriteCount DESC, a.id DESC")
    List<Article> findApprovedArticlesByFavoriteCountAfterCursor(
        @Param("cursorFavoriteCount") Integer cursorFavoriteCount,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );
    
    /**
     * Find approved articles using cursor-based pagination (by purchase count)
     * 
     * @param cursorPurchaseCount The purchase count of the last article
     * @param cursorId The ID of the last article (for tie-breaking)
     * @param pageable Page size configuration
     * @return List of articles after the cursor
     */
    @Query("SELECT a FROM Article a " +
           "WHERE a.reviewStatus = 'APPROVED' " +
           "AND (a.purchaseCount < :cursorPurchaseCount " +
           "     OR (a.purchaseCount = :cursorPurchaseCount AND a.id < :cursorId)) " +
           "ORDER BY a.purchaseCount DESC, a.id DESC")
    List<Article> findApprovedArticlesByPurchaseCountAfterCursor(
        @Param("cursorPurchaseCount") Integer cursorPurchaseCount,
        @Param("cursorId") Long cursorId,
        Pageable pageable
    );
    
    /**
     * Find approved articles by category using cursor-based pagination
     * 
     * @param categoryId The category ID
     * @param cursor The timestamp of the last article from previous page
     * @param pageable Page size configuration
     * @return List of articles after the cursor
     */
    @Query("SELECT a FROM Article a " +
           "WHERE a.reviewStatus = 'APPROVED' " +
           "AND a.categoryId = :categoryId " +
           "AND a.createdAt < :cursor " +
           "ORDER BY a.createdAt DESC")
    List<Article> findApprovedArticlesByCategoryAfterCursor(
        @Param("categoryId") Long categoryId,
        @Param("cursor") LocalDateTime cursor,
        Pageable pageable
    );
    
    /**
     * Find approved articles by user using cursor-based pagination
     * 
     * @param userId The user ID
     * @param cursor The timestamp of the last article from previous page
     * @param pageable Page size configuration
     * @return List of articles after the cursor
     */
    @Query("SELECT a FROM Article a " +
           "WHERE a.reviewStatus = 'APPROVED' " +
           "AND a.userId = :userId " +
           "AND a.createdAt < :cursor " +
           "ORDER BY a.createdAt DESC")
    List<Article> findApprovedArticlesByUserAfterCursor(
        @Param("userId") Long userId,
        @Param("cursor") LocalDateTime cursor,
        Pageable pageable
    );
    
    /**
     * Optimized query using covering index
     * First fetch IDs using index, then fetch full data
     * 
     * @param pageable Page configuration
     * @return List of articles
     */
    @Query(value = "SELECT a.* FROM articles a " +
                   "INNER JOIN (" +
                   "    SELECT id FROM articles " +
                   "    WHERE review_status = 'APPROVED' " +
                   "    ORDER BY created_at DESC " +
                   "    LIMIT :limit OFFSET :offset" +
                   ") AS t ON a.id = t.id " +
                   "ORDER BY a.created_at DESC",
           nativeQuery = true)
    List<Article> findApprovedArticlesOptimized(
        @Param("limit") int limit,
        @Param("offset") int offset
    );
    
    /**
     * Find articles with DTO projection to reduce data transfer
     * Only fetch necessary fields for list display
     */
    @Query("SELECT new com.blog.dto.ArticleListDTO(" +
           "a.id, a.title, a.summary, a.coverImage, a.userId, " +
           "a.categoryId, a.viewCount, a.likeCount, a.favoriteCount, " +
           "a.createdAt) " +
           "FROM Article a " +
           "WHERE a.reviewStatus = 'APPROVED' " +
           "ORDER BY a.createdAt DESC")
    List<Object[]> findApprovedArticlesAsDTO(Pageable pageable);
    
    /**
     * Batch fetch articles with user and category information
     * Avoids N+1 query problem
     */
    @Query("SELECT a FROM Article a " +
           "LEFT JOIN FETCH a.user " +
           "LEFT JOIN FETCH a.category " +
           "WHERE a.id IN :ids")
    List<Article> findArticlesWithUserAndCategory(@Param("ids") List<Long> ids);
}
