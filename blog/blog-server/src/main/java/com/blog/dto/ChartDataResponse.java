package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Chart Data Response DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataResponse {
    
    /**
     * Article trend data (last 7 days)
     */
    private TrendData articleTrend;
    
    /**
     * User growth data (last 7 days)
     */
    private TrendData userGrowth;
    
    /**
     * Category distribution
     */
    private List<CategoryData> categoryDistribution;
    
    /**
     * Revenue trend data (last 7 days)
     */
    private RevenueTrendData revenueTrend;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private List<String> dates;
        private List<Long> values;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevenueTrendData {
        private List<String> dates;
        private List<Double> income;    // 每日收入
        private List<Double> expense;   // 每日支出
        private List<Double> profit;    // 每日净收益
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryData {
        private String name;
        private Long value;
    }
}
