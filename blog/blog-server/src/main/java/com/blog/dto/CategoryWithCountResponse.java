package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类响应DTO（包含文章数量）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithCountResponse {
    
    private Long id;
    private String name;
    private String description;
    private Long count;  // 该分类下审核通过的文章数量
}
