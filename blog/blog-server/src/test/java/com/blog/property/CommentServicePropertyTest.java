package com.blog.property;

import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.CommentServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CommentService属性测试
 * 使用jqwik进行基于属性的测试
 */
class CommentServicePropertyTest {

    /**
     * Feature: personal-blog-system, Property 11: 评论父子关系
     * 验证需求: 6.2
     * 
     * 对于任意评论回复，系统应该正确建立父子评论关系，子评论的parent_id应该等于父评论的id
     */
    @Property(tries = 100)
    void testCommentParentChildRelationship(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long parentCommentId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validCommentContent") String parentContent,
            @ForAll("validCommentContent") String replyContent) {
        
        // 准备mock对象
        CommentRepository commentRepository = mock(CommentRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        CommentServiceImpl commentService = new CommentServiceImpl(
                commentRepository, articleRepository, userRepository);
        
        // 创建测试数据
        User user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        user.setEmail("user@example.com");
        user.setNickname("TestUser");
        user.setRole(User.UserRole.USER);
        
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId);
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        
        // 创建父评论
        Comment parentComment = new Comment();
        parentComment.setId(parentCommentId);
        parentComment.setArticleId(articleId);
        parentComment.setUserId(userId);
        parentComment.setContent(parentContent);
        parentComment.setParentId(null); // 父评论没有parent_id
        parentComment.setStatus(Comment.CommentStatus.APPROVED);
        
        // 创建回复评论
        Comment replyComment = new Comment();
        replyComment.setArticleId(articleId);
        replyComment.setUserId(userId);
        replyComment.setParentId(parentCommentId); // 设置父评论ID
        replyComment.setContent(replyContent);
        
        // 模拟保存后的回复评论（带有生成的ID）
        Comment savedReplyComment = new Comment();
        savedReplyComment.setId(parentCommentId + 1); // 回复评论的ID
        savedReplyComment.setArticleId(articleId);
        savedReplyComment.setUserId(userId);
        savedReplyComment.setParentId(parentCommentId); // 父评论ID应该被保留
        savedReplyComment.setContent(replyContent);
        savedReplyComment.setStatus(Comment.CommentStatus.APPROVED);
        
        // 配置mock行为
        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedReplyComment);
        
        // 执行回复评论操作
        Comment result = commentService.replyComment(replyComment);
        
        // 属性验证：回复评论的parent_id应该等于父评论的id
        assertNotNull(result, "Reply comment should not be null");
        assertNotNull(result.getParentId(), "Reply comment should have a parent_id");
        assertEquals(parentCommentId, result.getParentId(), 
                "Reply comment's parent_id should equal parent comment's id");
        
        // 验证回复评论的其他属性
        assertEquals(articleId, result.getArticleId(), 
                "Reply comment should belong to the same article");
        assertEquals(userId, result.getUserId(), 
                "Reply comment should have the correct user_id");
        assertEquals(replyContent, result.getContent(), 
                "Reply comment should have the correct content");
        assertEquals(Comment.CommentStatus.APPROVED, result.getStatus(), 
                "Reply comment should have APPROVED status by default");
        
        // 验证调用了正确的repository方法
        verify(commentRepository).findById(parentCommentId);
        verify(articleRepository).findById(articleId);
        verify(userRepository).findById(userId);
        verify(commentRepository).save(any(Comment.class));
    }

    /**
     * Feature: personal-blog-system, Property 11: 评论父子关系（多层回复）
     * 验证需求: 6.2
     * 
     * 测试多层评论回复的父子关系建立
     * 
     * NOTE: This is an extended test that verifies multi-level comment hierarchies.
     * The basic parent-child relationship is already covered by testCommentParentChildRelationship.
     */
    @Property(tries = 100)
    void testMultiLevelCommentParentChildRelationship(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId1,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId2,
            @ForAll("validCommentContent") String content1,
            @ForAll("validCommentContent") String content2,
            @ForAll("validCommentContent") String content3) {
        
        // 确保用户ID不同
        Assume.that(!userId1.equals(userId2));
        
        // 准备mock对象
        CommentRepository commentRepository = mock(CommentRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        CommentServiceImpl commentService = new CommentServiceImpl(
                commentRepository, articleRepository, userRepository);
        
        // 创建测试用户
        User user1 = new User();
        user1.setId(userId1);
        user1.setPhone("13800138000");
        user1.setEmail("user1@example.com");
        user1.setNickname("User1");
        user1.setRole(User.UserRole.USER);
        
        User user2 = new User();
        user2.setId(userId2);
        user2.setPhone("13800138001");
        user2.setEmail("user2@example.com");
        user2.setNickname("User2");
        user2.setRole(User.UserRole.USER);
        
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId1);
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        
        // 第一层：顶级评论
        Comment topLevelComment = new Comment();
        topLevelComment.setId(1L);
        topLevelComment.setArticleId(articleId);
        topLevelComment.setUserId(userId1);
        topLevelComment.setContent(content1);
        topLevelComment.setParentId(null);
        topLevelComment.setStatus(Comment.CommentStatus.APPROVED);
        
        // 第二层：回复顶级评论
        Comment secondLevelComment = new Comment();
        secondLevelComment.setArticleId(articleId);
        secondLevelComment.setUserId(userId2);
        secondLevelComment.setParentId(1L);
        secondLevelComment.setContent(content2);
        
        Comment savedSecondLevelComment = new Comment();
        savedSecondLevelComment.setId(2L);
        savedSecondLevelComment.setArticleId(articleId);
        savedSecondLevelComment.setUserId(userId2);
        savedSecondLevelComment.setParentId(1L); // 父评论是顶级评论
        savedSecondLevelComment.setContent(content2);
        savedSecondLevelComment.setStatus(Comment.CommentStatus.APPROVED);
        
        // 第三层：回复第二层评论
        Comment thirdLevelComment = new Comment();
        thirdLevelComment.setArticleId(articleId);
        thirdLevelComment.setUserId(userId1);
        thirdLevelComment.setParentId(2L);
        thirdLevelComment.setContent(content3);
        
        Comment savedThirdLevelComment = new Comment();
        savedThirdLevelComment.setId(3L);
        savedThirdLevelComment.setArticleId(articleId);
        savedThirdLevelComment.setUserId(userId1);
        savedThirdLevelComment.setParentId(2L); // 父评论是第二层评论
        savedThirdLevelComment.setContent(content3);
        savedThirdLevelComment.setStatus(Comment.CommentStatus.APPROVED);
        
        // 配置mock行为 - 第二层评论
        when(commentRepository.findById(1L)).thenReturn(Optional.of(topLevelComment));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId2)).thenReturn(Optional.of(user2));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment c = invocation.getArgument(0);
                    if (c.getParentId() != null && c.getParentId().equals(1L)) {
                        return savedSecondLevelComment;
                    }
                    return c;
                });
        
        // 执行第二层回复
        Comment result2 = commentService.replyComment(secondLevelComment);
        
        // 验证第二层评论的父子关系
        assertNotNull(result2);
        assertEquals(1L, result2.getParentId(), 
                "Second level comment's parent_id should be 1 (top level comment)");
        
        // 配置mock行为 - 第三层评论
        when(commentRepository.findById(2L)).thenReturn(Optional.of(savedSecondLevelComment));
        when(userRepository.findById(userId1)).thenReturn(Optional.of(user1));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(invocation -> {
                    Comment c = invocation.getArgument(0);
                    if (c.getParentId() != null && c.getParentId().equals(2L)) {
                        return savedThirdLevelComment;
                    }
                    return savedSecondLevelComment; // Return previous for other cases
                });
        
        // 执行第三层回复
        Comment result3 = commentService.replyComment(thirdLevelComment);
        
        // 验证第三层评论的父子关系
        assertNotNull(result3);
        assertEquals(2L, result3.getParentId(), 
                "Third level comment's parent_id should be 2 (second level comment)");
        
        // 验证所有评论都属于同一篇文章
        assertEquals(articleId, result2.getArticleId());
        assertEquals(articleId, result3.getArticleId());
    }

    /**
     * Feature: personal-blog-system, Property 11: 评论父子关系（错误场景）
     * 验证需求: 6.2
     * 
     * 测试父评论不存在时，回复评论应该失败
     */
    @Property(tries = 100)
    void testCommentParentChildRelationship_ParentNotFound(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long nonExistentParentId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validCommentContent") String replyContent) {
        
        // 准备mock对象
        CommentRepository commentRepository = mock(CommentRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        CommentServiceImpl commentService = new CommentServiceImpl(
                commentRepository, articleRepository, userRepository);
        
        // 创建回复评论（父评论不存在）
        Comment replyComment = new Comment();
        replyComment.setArticleId(articleId);
        replyComment.setUserId(userId);
        replyComment.setParentId(nonExistentParentId);
        replyComment.setContent(replyContent);
        
        // 配置mock行为：父评论不存在
        when(commentRepository.findById(nonExistentParentId)).thenReturn(Optional.empty());
        
        // 属性验证：当父评论不存在时，回复评论应该失败
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.replyComment(replyComment);
        });
        
        // 验证错误消息
        assertEquals("COMMENT_NOT_FOUND", exception.getErrorCode());
        assertEquals("父评论不存在", exception.getMessage());
        
        // 验证没有保存评论
        verify(commentRepository, never()).save(any(Comment.class));
    }

    /**
     * Feature: personal-blog-system, Property 11: 评论父子关系（跨文章错误）
     * 验证需求: 6.2
     * 
     * 测试回复评论和父评论不属于同一篇文章时应该失败
     */
    @Property(tries = 100)
    void testCommentParentChildRelationship_DifferentArticles(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId1,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId2,
            @ForAll @LongRange(min = 1, max = 1000000) Long parentCommentId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validCommentContent") String parentContent,
            @ForAll("validCommentContent") String replyContent) {
        
        // 确保两篇文章ID不同
        Assume.that(!articleId1.equals(articleId2));
        
        // 准备mock对象
        CommentRepository commentRepository = mock(CommentRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        CommentServiceImpl commentService = new CommentServiceImpl(
                commentRepository, articleRepository, userRepository);
        
        // 创建测试数据
        User user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        user.setEmail("user@example.com");
        user.setNickname("TestUser");
        user.setRole(User.UserRole.USER);
        
        Article article1 = new Article();
        article1.setId(articleId1);
        article1.setUserId(userId);
        article1.setTitle("Article 1");
        article1.setContent("Content 1");
        article1.setReviewStatus(Article.ReviewStatus.APPROVED);
        
        Article article2 = new Article();
        article2.setId(articleId2);
        article2.setUserId(userId);
        article2.setTitle("Article 2");
        article2.setContent("Content 2");
        article2.setReviewStatus(Article.ReviewStatus.APPROVED);
        
        // 父评论属于文章1
        Comment parentComment = new Comment();
        parentComment.setId(parentCommentId);
        parentComment.setArticleId(articleId1);
        parentComment.setUserId(userId);
        parentComment.setContent(parentContent);
        parentComment.setParentId(null);
        parentComment.setStatus(Comment.CommentStatus.APPROVED);
        
        // 回复评论尝试属于文章2（不同的文章）
        Comment replyComment = new Comment();
        replyComment.setArticleId(articleId2);
        replyComment.setUserId(userId);
        replyComment.setParentId(parentCommentId);
        replyComment.setContent(replyContent);
        
        // 配置mock行为
        when(commentRepository.findById(parentCommentId)).thenReturn(Optional.of(parentComment));
        when(articleRepository.findById(articleId2)).thenReturn(Optional.of(article2));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        // 属性验证：回复评论和父评论不属于同一篇文章时应该失败
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            commentService.replyComment(replyComment);
        });
        
        // 验证错误消息
        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("回复评论必须属于同一篇文章", exception.getMessage());
        
        // 验证没有保存评论
        verify(commentRepository, never()).save(any(Comment.class));
    }

    /**
     * Feature: personal-blog-system, Property 11: 评论父子关系（顶级评论验证）
     * 验证需求: 6.1
     * 
     * 测试顶级评论（非回复）不应该有parent_id
     */
    @Property(tries = 100)
    void testTopLevelComment_NoParentId(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validCommentContent") String content) {
        
        // 准备mock对象
        CommentRepository commentRepository = mock(CommentRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        CommentServiceImpl commentService = new CommentServiceImpl(
                commentRepository, articleRepository, userRepository);
        
        // 创建测试数据
        User user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        user.setEmail("user@example.com");
        user.setNickname("TestUser");
        user.setRole(User.UserRole.USER);
        
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId);
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        
        // 创建顶级评论
        Comment topLevelComment = new Comment();
        topLevelComment.setArticleId(articleId);
        topLevelComment.setUserId(userId);
        topLevelComment.setContent(content);
        topLevelComment.setParentId(null); // 顶级评论没有parent_id
        
        // 模拟保存后的评论
        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setArticleId(articleId);
        savedComment.setUserId(userId);
        savedComment.setContent(content);
        savedComment.setParentId(null); // parent_id应该保持为null
        savedComment.setStatus(Comment.CommentStatus.APPROVED);
        
        // 配置mock行为
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        
        // 执行创建评论操作
        Comment result = commentService.createComment(topLevelComment);
        
        // 属性验证：顶级评论不应该有parent_id
        assertNotNull(result);
        assertNull(result.getParentId(), 
                "Top level comment should not have a parent_id");
        
        // 验证其他属性
        assertEquals(articleId, result.getArticleId());
        assertEquals(userId, result.getUserId());
        assertEquals(content, result.getContent());
        assertEquals(Comment.CommentStatus.APPROVED, result.getStatus());
        
        // 验证调用了正确的repository方法
        verify(commentRepository).save(argThat(c -> c.getParentId() == null));
    }

    /**
     * 生成有效的评论内容（非空且非纯空格）
     */
    @Provide
    Arbitrary<String> validCommentContent() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(100)
                .filter(s -> !s.trim().isEmpty());
    }
}

