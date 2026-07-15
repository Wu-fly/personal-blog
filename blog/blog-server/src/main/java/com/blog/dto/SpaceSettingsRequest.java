package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * 个人空间设置请求DTO
 * 需求: 40.1-40.7
 */
@Data
public class SpaceSettingsRequest {

    /**
     * 主题颜色（十六进制颜色代码）
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "主题颜色格式不正确，应为十六进制颜色代码")
    private String themeColor;

    /**
     * 背景图片URL
     */
    @Size(max = 255, message = "背景图片URL长度不能超过255个字符")
    private String backgroundImage;

    /**
     * 布局样式（SINGLE, DOUBLE, CARD）
     */
    @Pattern(regexp = "^(SINGLE|DOUBLE|CARD)$", message = "布局样式必须是SINGLE、DOUBLE或CARD")
    private String layoutStyle;
}
