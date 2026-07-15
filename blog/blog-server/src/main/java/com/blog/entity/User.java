package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 用户实体类
 * 对应数据库表: users
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_phone", columnList = "phone"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_role", columnList = "role")
})
public class User extends BaseEntity {

    /**
     * 手机号（唯一）
     */
    @Column(name = "phone", length = 20, unique = true, nullable = false)
    private String phone;

    /**
     * 邮箱（唯一）
     */
    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    /**
     * 密码（加密存储）
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 昵称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;

    /**
     * 头像URL
     */
    @Column(name = "avatar")
    private String avatar;

    /**
     * 个人简介
     */
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    /**
     * 用户角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    /**
     * 账号状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * 用户角色枚举
     */
    public enum UserRole {
        USER,      // 普通用户
        BLOGGER,   // 博主
        ADMIN      // 管理员
    }

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE,    // 正常
        DISABLED   // 禁用
    }
}
