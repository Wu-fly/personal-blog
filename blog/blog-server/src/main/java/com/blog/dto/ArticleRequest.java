package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * 文章创建/更新请求DTO
 * 需求: 2.1-2.8
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleRequest {

    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;

    /**
     * 文章摘要
     */
    @Size(max = 500, message = "文章摘要不能超过500个字符")
    private String summary;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 标签名称列表（用于前端传递标签名称）
     */
    private List<String> tagNames;

    /**
     * 是否付费文章
     */
    private Boolean isPaid = false;

    /**
     * 文章价格（仅付费文章需要，验证在Controller中处理）
     */
    private BigDecimal price;
}
