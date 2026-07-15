package com.blog.dto;

import com.blog.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论响应DTO
 * 用于返回评论信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickname;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 回复数量
     */
    private Integer replyCount;

    /**
     * 从Comment实体转换为CommentResponse
     */
    public static CommentResponse fromEntity(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(comment.getId());
        response.setArticleId(comment.getArticleId());
        response.setUserId(comment.getUserId());
        response.setParentId(comment.getParentId());
        response.setContent(comment.getContent());
        response.setStatus(comment.getStatus().name());
        response.setCreatedAt(comment.getCreatedAt());
        
        // 设置用户信息
        if (comment.getUser() != null) {
            response.setUserNickname(comment.getUser().getNickname());
            response.setUserAvatar(comment.getUser().getAvatar());
        }
        
        response.setReplyCount(0); // 默认值，需要单独查询
        
        return response;
    }

    /**
     * 批量转换
     */
    public static List<CommentResponse> fromEntities(List<Comment> comments) {
        return comments.stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
