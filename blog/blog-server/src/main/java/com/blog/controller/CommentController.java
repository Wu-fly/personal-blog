package com.blog.controller;

import com.blog.dto.ApiResponse;
import com.blog.dto.ApproveCommentRequest;
import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.entity.Comment;
import com.blog.security.CustomUserDetails;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论管理控制器
 * 需求 6.1-6.5, 7.1-7.4
 */
@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 发表评论
     * POST /api/comments
     * 需求 6.1
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Creating comment for article: {}", request.getArticleId());
        
        // 创建评论实体
        Comment comment = new Comment();
        comment.setArticleId(request.getArticleId());
        comment.setUserId(userDetails.getId());
        comment.setContent(request.getContent());
        comment.setStatus(Comment.CommentStatus.APPROVED); // 默认审核通过
        
        // 保存评论
        Comment createdComment = commentService.createComment(comment);
        
        // 转换为响应DTO
        CommentResponse response = CommentResponse.fromEntity(createdComment);
        
        log.info("Comment created successfully with id: {}", createdComment.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("评论发表成功", response));
    }

    /**
     * 获取文章评论列表
     * GET /api/comments/article/{articleId}
     * 需求 6.4
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getArticleComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching comments for article: {}, page: {}, size: {}", articleId, page, size);
        
        // 创建分页参数，按创建时间倒序
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 查询评论列表
        Page<Comment> commentPage = commentService.getCommentsByArticle(articleId, pageable);
        
        // 转换为响应DTO
        List<CommentResponse> comments = CommentResponse.fromEntities(commentPage.getContent());
        
        // 构建分页响应
        Map<String, Object> response = new HashMap<>();
        response.put("comments", comments);
        response.put("currentPage", commentPage.getNumber());
        response.put("totalPages", commentPage.getTotalPages());
        response.put("totalElements", commentPage.getTotalElements());
        response.put("hasNext", commentPage.hasNext());
        
        log.info("Found {} comments for article: {}", commentPage.getTotalElements(), articleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 回复评论
     * POST /api/comments/{id}/reply
     * 需求 6.2
     */
    @PostMapping("/{id}/reply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CommentResponse>> replyComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Replying to comment: {}", id);
        
        // 创建回复评论实体
        Comment reply = new Comment();
        reply.setArticleId(request.getArticleId());
        reply.setUserId(userDetails.getId());
        reply.setParentId(id); // 设置父评论ID
        reply.setContent(request.getContent());
        reply.setStatus(Comment.CommentStatus.APPROVED); // 默认审核通过
        
        // 保存回复
        Comment createdReply = commentService.replyComment(reply);
        
        // 转换为响应DTO
        CommentResponse response = CommentResponse.fromEntity(createdReply);
        
        log.info("Reply created successfully with id: {}", createdReply.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("回复成功", response));
    }

    /**
     * 删除评论
     * DELETE /api/comments/{id}
     * 需求 7.3
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Deleting comment: {}", id);
        
        // 删除评论（服务层会验证权限）
        commentService.deleteComment(id, userDetails.getId());
        
        log.info("Comment deleted successfully: {}", id);
        return ResponseEntity.ok(ApiResponse.success("评论删除成功", null));
    }

    /**
     * 审核评论
     * PUT /api/comments/{id}/approve
     * 需求 7.2
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('BLOGGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommentResponse>> approveComment(
            @PathVariable Long id,
            @Valid @RequestBody ApproveCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("Approving comment: {} with status: {}", id, request.getStatus());
        
        // 解析审核状态
        Comment.CommentStatus status;
        try {
            status = Comment.CommentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid comment status: {}", request.getStatus());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_STATUS", "无效的审核状态"));
        }
        
        // 审核评论
        Comment approvedComment = commentService.approveComment(id, userDetails.getId(), status);
        
        // 转换为响应DTO
        CommentResponse response = CommentResponse.fromEntity(approvedComment);
        
        log.info("Comment approved successfully: {}", id);
        return ResponseEntity.ok(ApiResponse.success("评论审核成功", response));
    }

    /**
     * 获取待审核评论列表（博主/管理员）
     * GET /api/comments/pending
     * 需求 7.1
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('BLOGGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPendingComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching pending comments, page: {}, size: {}", page, size);
        
        // 创建分页参数
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // 查询待审核评论
        Page<Comment> commentPage = commentService.getPendingComments(pageable);
        
        // 转换为响应DTO
        List<CommentResponse> comments = CommentResponse.fromEntities(commentPage.getContent());
        
        // 构建分页响应
        Map<String, Object> response = new HashMap<>();
        response.put("comments", comments);
        response.put("currentPage", commentPage.getNumber());
        response.put("totalPages", commentPage.getTotalPages());
        response.put("totalElements", commentPage.getTotalElements());
        response.put("hasNext", commentPage.hasNext());
        
        log.info("Found {} pending comments", commentPage.getTotalElements());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取评论的回复列表
     * GET /api/comments/{id}/replies
     * 需求 6.2
     */
    @GetMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getCommentReplies(
            @PathVariable Long id) {
        
        log.info("Fetching replies for comment: {}", id);
        
        // 查询回复列表
        List<Comment> replies = commentService.getRepliesByComment(id);
        
        // 转换为响应DTO
        List<CommentResponse> response = CommentResponse.fromEntities(replies);
        
        log.info("Found {} replies for comment: {}", replies.size(), id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
