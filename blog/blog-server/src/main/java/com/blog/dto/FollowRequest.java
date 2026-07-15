package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 关注请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
