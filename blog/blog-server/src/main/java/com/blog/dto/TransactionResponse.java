package com.blog.dto;

import com.blog.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 交易记录响应DTO
 * 需求: 18.2, 18.3, 30.5
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
    
    private Long id;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Long relatedUserId;
    private String relatedUserNickname;
    private Long relatedArticleId;
    private String relatedArticleTitle;
    private Transaction.TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
}
