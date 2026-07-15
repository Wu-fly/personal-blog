package com.blog.dto;

import com.blog.entity.User;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 更新用户状态请求DTO
 */
@Data
public class UpdateUserStatusRequest {

    /**
     * 用户状态
     */
    @NotNull(message = "用户状态不能为空")
    private User.UserStatus status;
}
