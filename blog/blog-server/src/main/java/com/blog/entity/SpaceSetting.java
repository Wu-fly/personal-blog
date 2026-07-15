package com.blog.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 个人空间设置实体类
 * 对应数据库表: space_settings
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "space_settings")
public class SpaceSetting extends BaseEntity {

    /**
     * 用户ID（唯一）
     */
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    /**
     * 用户（一对一关系）
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 主题颜色
     */
    @Column(name = "theme_color", length = 20)
    private String themeColor = "#409EFF";

    /**
     * 背景图片URL
     */
    @Column(name = "background_image")
    private String backgroundImage;

    /**
     * 布局样式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "layout_style", nullable = false)
    private LayoutStyle layoutStyle = LayoutStyle.CARD;

    /**
     * 布局样式枚举
     */
    public enum LayoutStyle {
        SINGLE,  // 单栏
        DOUBLE,  // 双栏
        CARD     // 卡片式
    }
}
