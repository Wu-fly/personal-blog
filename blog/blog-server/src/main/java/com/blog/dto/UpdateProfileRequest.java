package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 更新用户信息请求DTO
 * 需求: 9.1
 */
@Data
public class UpdateProfileRequest {

    /**
     * 昵称
     */
    @Size(max = 50, message = "昵称长度不能超过50个字符")
    private String nickname;

    /**
     * 头像URL
     */
    @Size(max = 255, message = "头像URL长度不能超过255个字符")
    private String avatar;

    /**
     * 个人简介
     */
    @Size(max = 500, message = "个人简介长度不能超过500个字符")
    private String bio;
}
