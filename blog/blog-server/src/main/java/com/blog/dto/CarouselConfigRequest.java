package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 轮播图配置请求DTO
 */
@Data
public class CarouselConfigRequest {

    /**
     * 文章ID
     */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /**
     * 显示顺序
     */
    @NotNull(message = "显示顺序不能为空")
    @Min(value = 1, message = "显示顺序必须大于0")
    private Integer displayOrder;
}
