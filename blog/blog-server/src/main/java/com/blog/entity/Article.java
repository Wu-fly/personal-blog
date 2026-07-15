package com.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 文章实体类
 * 对应数据库表: articles
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "articles", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_category_id", columnList = "category_id"),
    @Index(name = "idx_review_status", columnList = "review_status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_view_count", columnList = "view_count"),
    @Index(name = "idx_favorite_count", columnList = "favorite_count"),
    @Index(name = "idx_purchase_count", columnList = "purchase_count")
})
public class Article extends BaseEntity {

    /**
     * 作者ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 作者（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    /**
     * 文章标题
     */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /**
     * 文章内容
     */
    @Column(name = "content", columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    /**
     * 文章摘要
     */
    @Column(name = "summary", length = 500)
    private String summary;

    /**
     * 封面图片URL
     */
    @Column(name = "cover_image", length = 500)
    private String coverImage;

    /**
     * 分类ID
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * 分类（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Category category;

    /**
     * 是否付费文章
     */
    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    /**
     * 文章价格
     */
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    /**
     * 是否置顶
     */
    @Column(name = "is_pinned", nullable = false)
    private Boolean isPinned = false;

    /**
     * 置顶时间
     */
    @Column(name = "pinned_at")
    private LocalDateTime pinnedAt;

    /**
     * 审核状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", nullable = false)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    /**
     * 审核意见
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    /**
     * 浏览量
     */
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    /**
     * 点赞数
     */
    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    /**
     * 收藏数
     */
    @Column(name = "favorite_count", nullable = false)
    private Integer favoriteCount = 0;

    /**
     * 购买次数
     */
    @Column(name = "purchase_count", nullable = false)
    private Integer purchaseCount = 0;

    /**
     * 发布时间
     */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /**
     * 审核状态枚举
     */
    public enum ReviewStatus {
        PENDING,   // 待审核
        APPROVED,  // 审核通过
        REJECTED   // 审核拒绝
    }
}
