package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 置顶文章请求DTO
 * 需求: 27.1-27.6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinArticleRequest {

    /**
     * 是否置顶
     */
    @NotNull(message = "置顶状态不能为空")
    private Boolean isPinned;
}
