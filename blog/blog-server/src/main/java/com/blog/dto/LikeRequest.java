package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 点赞请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeRequest {
    
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}
