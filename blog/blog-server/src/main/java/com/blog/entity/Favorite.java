package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 收藏实体类
 * 对应数据库表: favorites
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "favorites", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_article", columnNames = {"user_id", "article_id"})
    },
    indexes = {
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
public class Favorite extends BaseEntity {

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
