package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 博主申请请求DTO
 * 需求: 19.1-19.6
 */
@Data
public class BloggerApplicationRequest {

    /**
     * 博主昵称
     */
    @NotBlank(message = "博主昵称不能为空")
    @Size(min = 2, max = 50, message = "博主昵称长度必须在2-50个字符之间")
    private String nickname;

    /**
     * 博主简介
     */
    @NotBlank(message = "博主简介不能为空")
    @Size(min = 10, max = 500, message = "博主简介长度必须在10-500个字符之间")
    private String bio;

    /**
     * 擅长领域(多个用逗号分隔)
     */
    private String fields;

    /**
     * 自定义领域
     */
    private String customField;
}
