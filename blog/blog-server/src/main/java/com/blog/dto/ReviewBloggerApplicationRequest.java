package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 审核博主申请请求DTO
 */
@Data
public class ReviewBloggerApplicationRequest {

    /**
     * 是否通过审核
     */
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    /**
     * 审核意见
     */
    private String reviewComment;
}
