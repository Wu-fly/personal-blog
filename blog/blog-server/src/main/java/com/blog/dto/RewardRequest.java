package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 打赏请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RewardRequest {
    
    @NotNull(message = "博主ID不能为空")
    private Long bloggerId;
    
    @NotNull(message = "打赏金额不能为空")
    @DecimalMin(value = "0.01", message = "打赏金额必须大于0")
    private BigDecimal amount;
    
    private Long articleId; // 可选，关联的文章ID
}
