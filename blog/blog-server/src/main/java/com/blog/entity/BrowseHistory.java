package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 浏览历史实体类
 * 对应数据库表: browse_history
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "browse_history", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_updated_at", columnList = "updated_at")
})
public class BrowseHistory extends BaseEntity {

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 用户（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

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
}
