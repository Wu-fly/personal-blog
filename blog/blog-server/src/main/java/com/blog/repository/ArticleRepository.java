package com.blog.repository;

import com.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Article Repository
 * Handles database operations for Article entity with custom query methods
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    /**
     * Find articles by user ID
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Article> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find articles by category ID
     */
    Page<Article> findByCategoryId(Long categoryId, Pageable pageable);
    
    /**
     * Find articles by review status
     */
    @EntityGraph(attributePaths = {"user", "category"})
    Page<Article> findByReviewStatus(Article.ReviewStatus reviewStatus, Pageable pageable);
    
    /**
     * Find approved articles
     */
    @Query("SELECT a FROM Article a WHERE a.reviewStatus = 'APPROVED'")
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Article> findApprovedArticles(Pageable pageable);
    
    /**
     * Find approved articles by category
     */
    @Query("SELECT a FROM Article a WHERE a.reviewStatus = 'APPROVED' AND a.categoryId = :categoryId")
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Article> findApprovedArticlesByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Find approved articles by user (for personal space)
     */
    @Query("SELECT a FROM Article a WHERE a.reviewStatus = 'APPROVED' AND a.userId = :userId")
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Article> findApprovedArticlesByUser(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Full-text search in title, content, and author nickname
     */
    @Query("SELECT a FROM Article a LEFT JOIN User u ON a.userId = u.id WHERE a.reviewStatus = 'APPROVED' AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Article> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Find pinned articles by user
     */
    @Query("SELECT a FROM Article a WHERE a.userId = :userId AND a.isPinned = true ORDER BY a.pinnedAt DESC")
    List<Article> findPinnedArticlesByUser(@Param("userId") Long userId);
    
    /**
     * Increment view count
     */
    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + 1 WHERE a.id = :id")
    void incrementViewCount(@Param("id") Long id);
    
    /**
     * Increment like count
     */
    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount + 1 WHERE a.id = :id")
    void incrementLikeCount(@Param("id") Long id);
    
    /**
     * Decrement like count
     */
    @Modifying
    @Query("UPDATE Article a SET a.likeCount = a.likeCount - 1 WHERE a.id = :id AND a.likeCount > 0")
    void decrementLikeCount(@Param("id") Long id);
    
    /**
     * Increment favorite count
     */
    @Modifying
    @Query("UPDATE Article a SET a.favoriteCount = a.favoriteCount + 1 WHERE a.id = :id")
    void incrementFavoriteCount(@Param("id") Long id);
    
    /**
     * Decrement favorite count
     */
    @Modifying
    @Query("UPDATE Article a SET a.favoriteCount = a.favoriteCount - 1 WHERE a.id = :id AND a.favoriteCount > 0")
    void decrementFavoriteCount(@Param("id") Long id);
    
    /**
     * Increment purchase count
     */
    @Modifying
    @Query("UPDATE Article a SET a.purchaseCount = a.purchaseCount + 1 WHERE a.id = :id")
    void incrementPurchaseCount(@Param("id") Long id);
    
    /**
     * Find top articles by view count for carousel
     */
    @Query("SELECT a FROM Article a WHERE a.reviewStatus = 'APPROVED' ORDER BY a.viewCount DESC")
    List<Article> findTopArticlesByViewCount(Pageable pageable);
    
    /**
     * Find approved articles by tag
     */
    @Query("SELECT a FROM Article a JOIN ArticleTag at ON a.id = at.articleId WHERE a.reviewStatus = 'APPROVED' AND at.tagId = :tagId")
    Page<Article> findApprovedArticlesByTag(@Param("tagId") Long tagId, Pageable pageable);
    
    /**
     * Search articles with keyword and category filter
     */
    @Query("SELECT a FROM Article a LEFT JOIN User u ON a.userId = u.id WHERE a.reviewStatus = 'APPROVED' AND a.categoryId = :categoryId AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Article> searchByKeywordAndCategory(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Search articles with keyword and tag filter
     */
    @Query("SELECT a FROM Article a LEFT JOIN User u ON a.userId = u.id JOIN ArticleTag at ON a.id = at.articleId WHERE a.reviewStatus = 'APPROVED' AND at.tagId = :tagId AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Article> searchByKeywordAndTag(@Param("keyword") String keyword, @Param("tagId") Long tagId, Pageable pageable);
    
    /**
     * Search articles with keyword, category and tag filter
     */
    @Query("SELECT a FROM Article a LEFT JOIN User u ON a.userId = u.id JOIN ArticleTag at ON a.id = at.articleId WHERE a.reviewStatus = 'APPROVED' AND a.categoryId = :categoryId AND at.tagId = :tagId AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword% OR u.nickname LIKE %:keyword%)")
    @EntityGraph(attributePaths = {"category", "user"})
    Page<Article> searchByKeywordAndCategoryAndTag(@Param("keyword") String keyword, @Param("categoryId") Long categoryId, @Param("tagId") Long tagId, Pageable pageable);
    
    /**
     * Find approved articles by category and tag
     */
    @Query("SELECT a FROM Article a JOIN ArticleTag at ON a.id = at.articleId WHERE a.reviewStatus = 'APPROVED' AND a.categoryId = :categoryId AND at.tagId = :tagId")
    Page<Article> findApprovedArticlesByCategoryAndTag(@Param("categoryId") Long categoryId, @Param("tagId") Long tagId, Pageable pageable);
    
    /**
     * Count articles by user ID and review status
     */
    long countByUserIdAndReviewStatus(Long userId, Article.ReviewStatus reviewStatus);

    /**
     * Count articles by review status
     */
    Long countByReviewStatus(Article.ReviewStatus reviewStatus);

    /**
     * Count articles created between dates
     */
    long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    /**
     * Count articles by category
     */
    @Query("SELECT c.name, COUNT(a) FROM Article a JOIN a.category c GROUP BY c.name")
    List<Object[]> countArticlesByCategory();
    
    /**
     * Count articles by category ID and review status
     */
    long countByCategoryIdAndReviewStatus(Long categoryId, Article.ReviewStatus reviewStatus);
}
