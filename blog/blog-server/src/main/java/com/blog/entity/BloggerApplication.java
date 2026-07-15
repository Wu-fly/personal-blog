package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 博主申请实体类
 * 对应数据库表: blogger_applications
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "blogger_applications", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_status", columnList = "status")
})
public class BloggerApplication extends BaseEntity {

    /**
     * 申请用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 申请用户（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 博主昵称
     */
    @Column(name = "nickname", length = 50, nullable = false)
    private String nickname;

    /**
     * 博主简介
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * 擅长领域
     */
    @Column(name = "field", length = 100)
    private String field;

    /**
     * 申请状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status = ApplicationStatus.PENDING;

    /**
     * 审核意见
     */
    @Column(name = "review_comment", columnDefinition = "TEXT")
    private String reviewComment;

    /**
     * 审核时间
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    /**
     * 申请状态枚举
     */
    public enum ApplicationStatus {
        PENDING,   // 待审核
        APPROVED,  // 审核通过
        REJECTED   // 审核拒绝
    }
}
