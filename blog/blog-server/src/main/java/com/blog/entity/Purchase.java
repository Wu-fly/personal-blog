package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 购买记录实体类
 * 对应数据库表: purchases
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "purchases", 
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_article", columnNames = {"user_id", "article_id"})
    },
    indexes = {
        @Index(name = "idx_article_id", columnList = "article_id")
    }
)
public class Purchase extends BaseEntity {

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

    /**
     * 购买金额
     */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
}
