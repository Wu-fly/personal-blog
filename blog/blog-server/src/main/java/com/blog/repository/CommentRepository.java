package com.blog.repository;

import com.blog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Comment Repository
 * Handles database operations for Comment entity
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * Find comments by article ID (top-level comments only)
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT c FROM Comment c WHERE c.articleId = :articleId AND c.parentId IS NULL ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByArticleId(@Param("articleId") Long articleId, Pageable pageable);
    
    /**
     * Find all comments by article ID
     */
    List<Comment> findByArticleIdOrderByCreatedAtDesc(Long articleId);
    
    /**
     * Find replies to a comment
     */
    @EntityGraph(attributePaths = {"user"})
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);
    
    /**
     * Find comments by user ID
     */
    Page<Comment> findByUserId(Long userId, Pageable pageable);
    
    /**
     * Find comments by status
     */
    Page<Comment> findByStatus(Comment.CommentStatus status, Pageable pageable);
    
    /**
     * Count comments for an article
     */
    long countByArticleId(Long articleId);
}
