package com.blog.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 文章标签关联实体类
 * 对应数据库表: article_tags
 */
@Data
@Entity
@Table(name = "article_tags")
@IdClass(ArticleTag.ArticleTagId.class)
public class ArticleTag {

    /**
     * 文章ID
     */
    @Id
    @Column(name = "article_id", nullable = false)
    private Long articleId;

    /**
     * 标签ID
     */
    @Id
    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    /**
     * 文章（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", insertable = false, updatable = false)
    private Article article;

    /**
     * 标签（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", insertable = false, updatable = false)
    private Tag tag;

    /**
     * 复合主键类
     */
    @Data
    public static class ArticleTagId implements Serializable {
        private Long articleId;
        private Long tagId;
    }
}
