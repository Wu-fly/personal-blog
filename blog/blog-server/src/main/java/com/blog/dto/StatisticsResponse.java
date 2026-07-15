package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Platform Statistics Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private Long userCount;
    private Long articleCount;
    private Long pendingArticleCount;
    private BigDecimal totalRevenue;
}
