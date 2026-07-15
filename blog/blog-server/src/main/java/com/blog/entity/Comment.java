package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 评论实体类
 * 对应数据库表: comments
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_article_id", columnList = "article_id"),
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_parent_id", columnList = "parent_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Comment extends BaseEntity {

    /**
     * 文章ID
     */
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    /**
     * 文章（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;

    /**
     * 评论用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 评论用户（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 父评论ID（用于回复评论）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 父评论（自关联）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Comment parentComment;

    /**
     * 评论内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 评论状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CommentStatus status = CommentStatus.APPROVED;

    /**
     * 评论状态枚举
     */
    public enum CommentStatus {
        PENDING,   // 待审核
        APPROVED,  // 审核通过
        REJECTED   // 审核拒绝
    }
}
