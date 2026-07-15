package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.UserRepository;
import com.blog.service.CommentService;
import com.blog.service.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论管理服务实现
 * 需求: 6.1-6.5, 7.1-7.4
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    /**
     * 发表评论
     * 需求: 6.1
     * - 保存评论到数据库
     * - 关联到对应文章
     * - 验证评论内容不为空
     * 
     * 权限: 需求 38.3 - 需要登录才能评论
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Comment createComment(Comment comment) {
        log.info("Creating comment for article: {}", comment.getArticleId());
        
        // 验证评论内容不为空 (需求: 6.3)
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new BusinessException("INVALID_INPUT", "评论内容不能为空");
        }
        
        // 检测违规词汇
        if (securityService.containsSensitiveWord(comment.getContent())) {
            throw new BusinessException("SENSITIVE_WORD_DETECTED", "评论内容包含违规词汇，请修改后重试");
        }
        
        // 验证文章是否存在
        Article article = articleRepository.findById(comment.getArticleId())
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 验证用户是否存在
        User user = userRepository.findById(comment.getUserId())
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 设置默认审核状态为已通过 (需求: 6.1)
        if (comment.getStatus() == null) {
            comment.setStatus(Comment.CommentStatus.APPROVED);
        }
        
        // 确保这是顶级评论（没有父评论）
        comment.setParentId(null);
        
        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with id: {}", savedComment.getId());
        
        return savedComment;
    }

    /**
     * 回复评论
     * 需求: 6.2
     * - 保存回复评论
     * - 建立父子评论关系
     * - 验证父评论是否存在
     * 
     * 权限: 需求 38.3 - 需要登录才能回复评论
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Comment replyComment(Comment comment) {
        log.info("Replying to comment: {}", comment.getParentId());
        
        // 验证评论内容不为空 (需求: 6.3)
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            throw new BusinessException("INVALID_INPUT", "评论内容不能为空");
        }
        
        // 检测违规词汇
        if (securityService.containsSensitiveWord(comment.getContent())) {
            throw new BusinessException("SENSITIVE_WORD_DETECTED", "评论内容包含违规词汇，请修改后重试");
        }
        
        // 验证父评论是否存在 (需求: 6.2)
        if (comment.getParentId() == null) {
            throw new BusinessException("INVALID_INPUT", "回复评论必须指定父评论ID");
        }
        
        Comment parentComment = commentRepository.findById(comment.getParentId())
            .orElseThrow(() -> new BusinessException("COMMENT_NOT_FOUND", "父评论不存在"));
        
        // 验证文章是否存在
        Article article = articleRepository.findById(comment.getArticleId())
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 验证用户是否存在
        User user = userRepository.findById(comment.getUserId())
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 验证父评论和回复评论属于同一篇文章
        if (!parentComment.getArticleId().equals(comment.getArticleId())) {
            throw new BusinessException("INVALID_INPUT", "回复评论必须属于同一篇文章");
        }
        
        // 设置默认审核状态为已通过
        if (comment.getStatus() == null) {
            comment.setStatus(Comment.CommentStatus.APPROVED);
        }
        
        Comment savedComment = commentRepository.save(comment);
        log.info("Reply comment created successfully with id: {}", savedComment.getId());
        
        return savedComment;
    }

    /**
     * 删除评论
     * 需求: 7.3
     * - 删除评论
     * - 级联删除所有子回复
     * - 验证权限（博主或评论作者）
     */
    @Override
    @Transactional
    public void deleteComment(Long id, Long userId) {
        log.info("Deleting comment with id: {}", id);
        
        // 查找评论
        Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new BusinessException("COMMENT_NOT_FOUND", "评论不存在"));
        
        // 查找文章
        Article article = articleRepository.findById(comment.getArticleId())
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 验证权限：评论作者或文章作者（博主）可以删除 (需求: 7.3)
        if (!comment.getUserId().equals(userId) && !article.getUserId().equals(userId)) {
            throw new BusinessException("PERMISSION_DENIED", "只有评论作者或博主可以删除评论");
        }
        
        // 删除评论（级联删除子回复通过数据库外键约束实现）(需求: 7.3)
        commentRepository.delete(comment);
        log.info("Comment deleted successfully with id: {}", id);
    }

    /**
     * 查询文章的评论列表
     * 需求: 6.4
     * - 返回文章的所有评论
     * - 按时间倒序排列
     * - 支持分页
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getCommentsByArticle(Long articleId, Pageable pageable) {
        log.info("Getting comments for article: {}", articleId);
        
        // 验证文章是否存在
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 查询顶级评论（没有父评论的评论），按时间倒序排列 (需求: 6.4)
        Page<Comment> comments = commentRepository.findTopLevelCommentsByArticleId(articleId, pageable);
        
        log.info("Found {} comments for article: {}", comments.getTotalElements(), articleId);
        return comments;
    }

    /**
     * 查询评论的所有回复
     * 需求: 6.2
     * - 返回评论的所有子回复
     * - 按时间正序排列
     */
    @Override
    @Transactional(readOnly = true)
    public List<Comment> getRepliesByComment(Long parentId) {
        log.info("Getting replies for comment: {}", parentId);
        
        // 验证父评论是否存在
        Comment parentComment = commentRepository.findById(parentId)
            .orElseThrow(() -> new BusinessException("COMMENT_NOT_FOUND", "父评论不存在"));
        
        // 查询子回复，按时间正序排列
        List<Comment> replies = commentRepository.findByParentIdOrderByCreatedAtAsc(parentId);
        
        log.info("Found {} replies for comment: {}", replies.size(), parentId);
        return replies;
    }

    /**
     * 审核评论
     * 需求: 7.2
     * - 更新评论审核状态
     * - 验证权限（博主）
     */
    @Override
    @Transactional
    public Comment approveComment(Long id, Long userId, Comment.CommentStatus status) {
        log.info("Approving comment with id: {}, status: {}", id, status);
        
        // 查找评论
        Comment comment = commentRepository.findById(id)
            .orElseThrow(() -> new BusinessException("COMMENT_NOT_FOUND", "评论不存在"));
        
        // 查找文章
        Article article = articleRepository.findById(comment.getArticleId())
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 验证权限：只有文章作者（博主）可以审核评论 (需求: 7.2)
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("PERMISSION_DENIED", "只有博主可以审核评论");
        }
        
        // 更新审核状态 (需求: 7.2)
        comment.setStatus(status);
        
        Comment updatedComment = commentRepository.save(comment);
        log.info("Comment approved successfully with id: {}", id);
        
        return updatedComment;
    }

    /**
     * 查询待审核评论列表
     * 需求: 7.1
     * - 返回所有待审核的评论
     * - 支持分页
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Comment> getPendingComments(Pageable pageable) {
        log.info("Getting pending comments");
        
        // 查询待审核评论 (需求: 7.1)
        Page<Comment> comments = commentRepository.findByStatus(Comment.CommentStatus.PENDING, pageable);
        
        log.info("Found {} pending comments", comments.getTotalElements());
        return comments;
    }
}
