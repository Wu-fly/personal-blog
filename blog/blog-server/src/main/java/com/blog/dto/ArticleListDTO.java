package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Article List DTO
 * 
 * Lightweight DTO for article list display.
 * Only contains necessary fields to reduce data transfer and improve performance.
 * 
 * Used in optimized queries to avoid fetching full article content.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleListDTO {
    
    /**
     * Article ID
     */
    private Long id;
    
    /**
     * Article title
     */
    private String title;
    
    /**
     * Article summary
     */
    private String summary;
    
    /**
     * Cover image URL
     */
    private String coverImage;
    
    /**
     * Author user ID
     */
    private Long userId;
    
    /**
     * Category ID
     */
    private Long categoryId;
    
    /**
     * View count
     */
    private Integer viewCount;
    
    /**
     * Like count
     */
    private Integer likeCount;
    
    /**
     * Favorite count
     */
    private Integer favoriteCount;
    
    /**
     * Created timestamp
     */
    private LocalDateTime createdAt;
}
