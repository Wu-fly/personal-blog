package com.blog.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 审核评论请求DTO
 */
@Data
public class ApproveCommentRequest {

    /**
     * 审核状态: APPROVED, REJECTED
     */
    @NotNull(message = "审核状态不能为空")
    private String status;

    /**
     * 审核意见（可选）
     */
    private String comment;
}
