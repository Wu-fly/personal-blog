package com.blog.service;

import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentService单元测试
 * 需求: 6.1-6.5, 7.1-7.4
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User user;
    private User blogger;
    private Article article;
    private Comment comment;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        user = new User();
        user.setId(1L);
        user.setPhone("13800138000");
        user.setEmail("user@example.com");
        user.setNickname("测试用户");
        user.setRole(User.UserRole.USER);

        // 创建测试博主
        blogger = new User();
        blogger.setId(2L);
        blogger.setPhone("13800138001");
        blogger.setEmail("blogger@example.com");
        blogger.setNickname("测试博主");
        blogger.setRole(User.UserRole.BLOGGER);

        // 创建测试文章
        article = new Article();
        article.setId(1L);
        article.setUserId(2L);
        article.setTitle("测试文章");
        article.setContent("这是测试文章内容");
        article.setReviewStatus(Article.ReviewStatus.APPROVED);

        // 创建测试评论
        comment = new Comment();
        comment.setId(1L);
        comment.setArticleId(1L);
        comment.setUserId(1L);
        comment.setContent("这是测试评论");
        comment.setStatus(Comment.CommentStatus.APPROVED);
    }

    /**
     * 测试发表评论 - 成功
     * 需求: 6.1
     */
    @Test
    void testCreateComment_Success() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // When
        Comment result = commentService.createComment(comment);

        // Then
        assertNotNull(result);
        assertEquals(Comment.CommentStatus.APPROVED, result.getStatus());
        assertNull(result.getParentId());
        verify(commentRepository).save(any(Comment.class));
    }

    /**
     * 测试发表评论 - 内容为空
     * 需求: 6.3
     */
    @Test
    void testCreateComment_EmptyContent_ThrowsException() {
        // Given
        comment.setContent("");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.createComment(comment);
        });
        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("评论内容不能为空", exception.getMessage());
    }

    /**
     * 测试发表评论 - 文章不存在
     * 需求: 6.1
     */
    @Test
    void testCreateComment_ArticleNotFound_ThrowsException() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.createComment(comment);
        });
        assertEquals("ARTICLE_NOT_FOUND", exception.getErrorCode());
    }

    /**
     * 测试发表评论 - 用户不存在
     * 需求: 6.1
     */
    @Test
    void testCreateComment_UserNotFound_ThrowsException() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.createComment(comment);
        });
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
    }

    /**
     * 测试回复评论 - 成功
     * 需求: 6.2
     */
    @Test
    void testReplyComment_Success() {
        // Given
        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setArticleId(1L);
        parentComment.setUserId(1L);
        parentComment.setContent("父评论");

        Comment replyComment = new Comment();
        replyComment.setArticleId(1L);
        replyComment.setUserId(2L);
        replyComment.setParentId(1L);
        replyComment.setContent("回复评论");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(parentComment));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(2L)).thenReturn(Optional.of(blogger));
        when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);

        // When
        Comment result = commentService.replyComment(replyComment);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getParentId());
        verify(commentRepository).save(any(Comment.class));
    }

    /**
     * 测试回复评论 - 内容为空
     * 需求: 6.3
     */
    @Test
    void testReplyComment_EmptyContent_ThrowsException() {
        // Given
        Comment replyComment = new Comment();
        replyComment.setContent("");
        replyComment.setParentId(1L);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.replyComment(replyComment);
        });
        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("评论内容不能为空", exception.getMessage());
    }

    /**
     * 测试回复评论 - 未指定父评论
     * 需求: 6.2
     */
    @Test
    void testReplyComment_NoParentId_ThrowsException() {
        // Given
        Comment replyComment = new Comment();
        replyComment.setContent("回复评论");
        replyComment.setParentId(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.replyComment(replyComment);
        });
        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("回复评论必须指定父评论ID", exception.getMessage());
    }

    /**
     * 测试回复评论 - 父评论不存在
     * 需求: 6.2
     */
    @Test
    void testReplyComment_ParentNotFound_ThrowsException() {
        // Given
        Comment replyComment = new Comment();
        replyComment.setContent("回复评论");
        replyComment.setParentId(1L);

        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.replyComment(replyComment);
        });
        assertEquals("COMMENT_NOT_FOUND", exception.getErrorCode());
    }

    /**
     * 测试回复评论 - 不属于同一篇文章
     * 需求: 6.2
     */
    @Test
    void testReplyComment_DifferentArticle_ThrowsException() {
        // Given
        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setArticleId(1L);

        Comment replyComment = new Comment();
        replyComment.setArticleId(2L); // Different article
        replyComment.setUserId(2L);
        replyComment.setParentId(1L);
        replyComment.setContent("回复评论");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(parentComment));
        when(articleRepository.findById(2L)).thenReturn(Optional.of(article));
        when(userRepository.findById(2L)).thenReturn(Optional.of(blogger));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.replyComment(replyComment);
        });
        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("回复评论必须属于同一篇文章", exception.getMessage());
    }

    /**
     * 测试删除评论 - 评论作者删除成功
     * 需求: 7.3
     */
    @Test
    void testDeleteComment_ByCommentAuthor_Success() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When
        commentService.deleteComment(1L, 1L); // Comment author

        // Then
        verify(commentRepository).delete(comment);
    }

    /**
     * 测试删除评论 - 博主删除成功
     * 需求: 7.3
     */
    @Test
    void testDeleteComment_ByBlogger_Success() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When
        commentService.deleteComment(1L, 2L); // Blogger (article author)

        // Then
        verify(commentRepository).delete(comment);
    }

    /**
     * 测试删除评论 - 评论不存在
     * 需求: 7.3
     */
    @Test
    void testDeleteComment_NotFound_ThrowsException() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.deleteComment(1L, 1L);
        });
        assertEquals("COMMENT_NOT_FOUND", exception.getErrorCode());
    }

    /**
     * 测试删除评论 - 无权限
     * 需求: 7.3
     */
    @Test
    void testDeleteComment_NoPermission_ThrowsException() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.deleteComment(1L, 3L); // Different user
        });
        assertEquals("PERMISSION_DENIED", exception.getErrorCode());
    }

    /**
     * 测试查询文章评论列表
     * 需求: 6.4
     */
    @Test
    void testGetCommentsByArticle_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Comment> comments = Arrays.asList(comment);
        Page<Comment> page = new PageImpl<>(comments, pageable, 1);

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(commentRepository.findTopLevelCommentsByArticleId(eq(1L), any(Pageable.class))).thenReturn(page);

        // When
        Page<Comment> result = commentService.getCommentsByArticle(1L, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(commentRepository).findTopLevelCommentsByArticleId(eq(1L), any(Pageable.class));
    }

    /**
     * 测试查询文章评论列表 - 文章不存在
     * 需求: 6.4
     */
    @Test
    void testGetCommentsByArticle_ArticleNotFound_ThrowsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.getCommentsByArticle(1L, pageable);
        });
        assertEquals("ARTICLE_NOT_FOUND", exception.getErrorCode());
    }

    /**
     * 测试查询评论回复列表
     * 需求: 6.2
     */
    @Test
    void testGetRepliesByComment_Success() {
        // Given
        Comment reply1 = new Comment();
        reply1.setId(2L);
        reply1.setParentId(1L);
        reply1.setContent("回复1");

        Comment reply2 = new Comment();
        reply2.setId(3L);
        reply2.setParentId(1L);
        reply2.setContent("回复2");

        List<Comment> replies = Arrays.asList(reply1, reply2);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.findByParentIdOrderByCreatedAtAsc(1L)).thenReturn(replies);

        // When
        List<Comment> result = commentService.getRepliesByComment(1L);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(commentRepository).findByParentIdOrderByCreatedAtAsc(1L);
    }

    /**
     * 测试查询评论回复列表 - 父评论不存在
     * 需求: 6.2
     */
    @Test
    void testGetRepliesByComment_ParentNotFound_ThrowsException() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.getRepliesByComment(1L);
        });
        assertEquals("COMMENT_NOT_FOUND", exception.getErrorCode());
    }

    /**
     * 测试审核评论 - 成功
     * 需求: 7.2
     */
    @Test
    void testApproveComment_Success() {
        // Given
        comment.setStatus(Comment.CommentStatus.PENDING);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // When
        Comment result = commentService.approveComment(1L, 2L, Comment.CommentStatus.APPROVED);

        // Then
        assertNotNull(result);
        assertEquals(Comment.CommentStatus.APPROVED, result.getStatus());
        verify(commentRepository).save(any(Comment.class));
    }

    /**
     * 测试审核评论 - 评论不存在
     * 需求: 7.2
     */
    @Test
    void testApproveComment_NotFound_ThrowsException() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.approveComment(1L, 2L, Comment.CommentStatus.APPROVED);
        });
        assertEquals("COMMENT_NOT_FOUND", exception.getErrorCode());
    }

    /**
     * 测试审核评论 - 无权限
     * 需求: 7.2
     */
    @Test
    void testApproveComment_NoPermission_ThrowsException() {
        // Given
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.approveComment(1L, 1L, Comment.CommentStatus.APPROVED); // Not blogger
        });
        assertEquals("PERMISSION_DENIED", exception.getErrorCode());
    }

    /**
     * 测试查询待审核评论列表
     * 需求: 7.1
     */
    @Test
    void testGetPendingComments_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Comment pendingComment = new Comment();
        pendingComment.setId(1L);
        pendingComment.setStatus(Comment.CommentStatus.PENDING);

        List<Comment> comments = Arrays.asList(pendingComment);
        Page<Comment> page = new PageImpl<>(comments, pageable, 1);

        when(commentRepository.findByStatus(Comment.CommentStatus.PENDING, pageable)).thenReturn(page);

        // When
        Page<Comment> result = commentService.getPendingComments(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(commentRepository).findByStatus(Comment.CommentStatus.PENDING, pageable);
    }
}
