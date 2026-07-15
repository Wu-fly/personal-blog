package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 公告实体类
 * 对应数据库表: announcements
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "announcements", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id")
})
public class Announcement extends BaseEntity {

    /**
     * 博主用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 博主（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 公告内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
}
