package com.blog.dto;

import com.blog.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    
    private Long id;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    
    // 关联信息
    private Long relatedUserId;
    private String relatedUserNickname;
    private Long relatedArticleId;
    private String relatedArticleTitle;
    
    /**
     * 从Transaction实体转换为DTO
     */
    public static TransactionDTO fromEntity(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType().name());
        dto.setAmount(transaction.getAmount());
        dto.setBalanceAfter(transaction.getBalanceAfter());
        dto.setDescription(transaction.getDescription());
        dto.setStatus(transaction.getStatus().name());
        dto.setCreatedAt(transaction.getCreatedAt());
        dto.setRelatedUserId(transaction.getRelatedUserId());
        dto.setRelatedArticleId(transaction.getRelatedArticleId());
        return dto;
    }
}
