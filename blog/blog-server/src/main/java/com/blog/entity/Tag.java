package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 标签实体类
 * 对应数据库表: tags
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_name", columnList = "name")
})
public class Tag extends BaseEntity {

    /**
     * 标签名称（唯一）
     */
    @Column(name = "name", length = 50, unique = true, nullable = false)
    private String name;
}
