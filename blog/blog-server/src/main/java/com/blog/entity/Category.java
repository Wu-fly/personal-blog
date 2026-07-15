package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 分类实体类
 * 对应数据库表: categories
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_name", columnList = "name")
})
public class Category extends BaseEntity {

    /**
     * 分类名称（唯一）
     */
    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name;

    /**
     * 分类描述
     */
    @Column(name = "description", length = 200)
    private String description;
}
