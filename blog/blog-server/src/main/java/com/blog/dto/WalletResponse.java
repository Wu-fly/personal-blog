package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 钱包响应DTO
 * 需求: 18.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {
    
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private BigDecimal totalIncome;
    private BigDecimal totalWithdraw;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
