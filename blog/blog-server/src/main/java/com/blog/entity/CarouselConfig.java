package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 轮播图配置实体类
 * 对应数据库表: carousel_config
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "carousel_config", indexes = {
    @Index(name = "idx_display_order", columnList = "display_order")
})
public class CarouselConfig extends BaseEntity {

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
     * 显示顺序
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
