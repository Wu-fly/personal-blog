package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 敏感词实体类
 * 对应数据库表: sensitive_words
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sensitive_words", indexes = {
    @Index(name = "idx_word", columnList = "word")
})
public class SensitiveWord extends BaseEntity {

    /**
     * 敏感词（唯一）
     */
    @Column(name = "word", length = 50, unique = true, nullable = false)
    private String word;
}
