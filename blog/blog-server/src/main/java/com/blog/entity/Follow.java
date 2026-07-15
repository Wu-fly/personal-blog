package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 关注实体类
 * 对应数据库表: follows
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "follows", uniqueConstraints = {
    @UniqueConstraint(name = "uk_follower_following", columnNames = {"follower_id", "following_id"})
})
public class Follow extends BaseEntity {

    /**
     * 关注者ID
     */
    @Column(name = "follower_id", nullable = false)
    private Long followerId;

    /**
     * 关注者（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", insertable = false, updatable = false)
    private User follower;

    /**
     * 被关注者ID
     */
    @Column(name = "following_id", nullable = false)
    private Long followingId;

    /**
     * 被关注者（多对一关系）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", insertable = false, updatable = false)
    private User following;
}
