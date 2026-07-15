package com.blog.dto;

import com.blog.entity.Article;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章响应DTO
 * 需求: 3.1-3.4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private List<TagResponse> tags;
    private Boolean isPaid;
    private BigDecimal price;
    private Boolean isPinned;
    private String reviewStatus;
    private String reviewComment;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer purchaseCount;
    private AuthorInfo author;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;

    /**
     * 作者信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthorInfo {
        private Long id;
        private String nickname;
        private String avatar;
    }

    /**
     * 标签信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagResponse {
        private Long id;
        private String name;
    }

    /**
     * 从Article实体转换为ArticleResponse
     */
    public static ArticleResponse fromEntity(Article article) {
        return fromEntity(article, false);
    }

    /**
     * 从Article实体转换为ArticleResponse，支持内容截断
     * @param article 文章实体
     * @param shouldTruncate 是否需要截断付费内容
     */
    public static ArticleResponse fromEntity(Article article, boolean shouldTruncate) {
        ArticleResponse response = new ArticleResponse();
        response.setId(article.getId());
        response.setTitle(article.getTitle());
        
        // 处理内容截断逻辑
        String content = article.getContent();
        if (shouldTruncate && content != null && content.length() > 200) {
            content = content.substring(0, 200) + "...\n\n🔒 以下为付费内容，购买后可查看完整文章";
        }
        response.setContent(content);
        
        response.setSummary(article.getSummary());
        response.setCoverImage(article.getCoverImage());
        response.setCategoryId(article.getCategoryId());
        response.setIsPaid(article.getIsPaid());
        response.setPrice(article.getPrice());
        response.setIsPinned(article.getIsPinned());
        response.setReviewStatus(article.getReviewStatus().name());
        response.setReviewComment(article.getReviewComment());
        response.setViewCount(article.getViewCount());
        response.setLikeCount(article.getLikeCount());
        response.setFavoriteCount(article.getFavoriteCount());
        response.setPurchaseCount(article.getPurchaseCount());
        response.setCreatedAt(article.getCreatedAt());
        response.setUpdatedAt(article.getUpdatedAt());
        response.setPublishedAt(article.getPublishedAt());

        // 设置分类名称
        if (article.getCategory() != null) {
            response.setCategoryName(article.getCategory().getName());
        }

        // 设置作者信息
        if (article.getUser() != null) {
            AuthorInfo authorInfo = new AuthorInfo();
            authorInfo.setId(article.getUser().getId());
            authorInfo.setNickname(article.getUser().getNickname());
            authorInfo.setAvatar(article.getUser().getAvatar());
            response.setAuthor(authorInfo);
        }
        
        // 注意：标签信息需要在Controller层单独加载并设置

        return response;
    }
}
