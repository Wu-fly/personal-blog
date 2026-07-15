package com.blog.property;

import com.blog.entity.Article;
import com.blog.entity.BloggerApplication;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.*;
import com.blog.service.WalletService;
import com.blog.service.impl.AdminServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdminService属性测试
 * 使用jqwik进行基于属性的测试
 */
class AdminServicePropertyTest {

    /**
     * Feature: personal-blog-system, Property 23: 文章审核状态更新
     * 验证需求: 33.3
     * 
     * 对于任意待审核文章，管理员审核通过后，文章状态应该更新为"已通过"并在用户端可见
     */
    @Property(tries = 100)
    void testArticleReviewStatusUpdateToApproved(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll("reviewComments") String reviewComment) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CarouselConfigRepository carouselConfigRepository = mock(CarouselConfigRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        WalletService walletService = mock(WalletService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        
        AdminServiceImpl adminService = new AdminServiceImpl(
            articleRepository, bloggerApplicationRepository, userRepository,
            carouselConfigRepository, walletRepository, walletService, transactionRepository
        );
        
        // 创建待审核的文章
        Article pendingArticle = new Article();
        pendingArticle.setId(articleId);
        pendingArticle.setUserId(userId);
        pendingArticle.setTitle(title);
        pendingArticle.setContent(content);
        pendingArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        pendingArticle.setReviewComment(null);
        pendingArticle.setPublishedAt(null);
        pendingArticle.setIsPaid(false);
        pendingArticle.setCoverImage("/cover.jpg");
        pendingArticle.setViewCount(0);
        pendingArticle.setLikeCount(0);
        pendingArticle.setFavoriteCount(0);
        pendingArticle.setPurchaseCount(0);
        pendingArticle.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(pendingArticle));
        
        // 模拟保存操作，返回更新后的文章
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 执行审核通过操作
        Article result = adminService.reviewArticle(articleId, true, reviewComment);
        
        // 属性验证：审核通过后，文章状态必须是APPROVED
        assertNotNull(result, "Reviewed article should not be null");
        assertEquals(Article.ReviewStatus.APPROVED, result.getReviewStatus(), 
            "Article review status must be APPROVED after admin approval");
        
        // 验证审核意见被保存
        assertEquals(reviewComment, result.getReviewComment(), 
            "Review comment should be saved");
        
        // 验证发布时间被设置
        assertNotNull(result.getPublishedAt(), 
            "Published time should be set when article is approved");
        
        // 验证发布时间是最近的时间（在过去1分钟内）
        assertTrue(result.getPublishedAt().isAfter(LocalDateTime.now().minusMinutes(1)), 
            "Published time should be recent (within last minute)");
        assertTrue(result.getPublishedAt().isBefore(LocalDateTime.now().plusSeconds(5)), 
            "Published time should not be in the future");
        
        // 验证文章内容保持不变
        assertEquals(title, result.getTitle(), 
            "Article title should remain unchanged after review");
        assertEquals(content, result.getContent(), 
            "Article content should remain unchanged after review");
        assertEquals(userId, result.getUserId(), 
            "Article author should remain unchanged after review");
        
        // 验证保存方法被调用
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 23: 文章审核状态更新（审核拒绝）
     * 验证需求: 33.3, 33.4
     * 
     * 对于任意待审核文章，管理员审核拒绝后，文章状态应该更新为"已拒绝"且不在用户端显示
     */
    @Property(tries = 100)
    void testArticleReviewStatusUpdateToRejected(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll("reviewComments") String reviewComment) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CarouselConfigRepository carouselConfigRepository = mock(CarouselConfigRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        WalletService walletService = mock(WalletService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        
        AdminServiceImpl adminService = new AdminServiceImpl(
            articleRepository, bloggerApplicationRepository, userRepository,
            carouselConfigRepository, walletRepository, walletService, transactionRepository
        );
        
        // 创建待审核的文章
        Article pendingArticle = new Article();
        pendingArticle.setId(articleId);
        pendingArticle.setUserId(userId);
        pendingArticle.setTitle(title);
        pendingArticle.setContent(content);
        pendingArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        pendingArticle.setReviewComment(null);
        pendingArticle.setPublishedAt(null);
        pendingArticle.setIsPaid(false);
        pendingArticle.setCoverImage("/cover.jpg");
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(pendingArticle));
        
        // 模拟保存操作
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 执行审核拒绝操作
        Article result = adminService.reviewArticle(articleId, false, reviewComment);
        
        // 属性验证：审核拒绝后，文章状态必须是REJECTED
        assertNotNull(result, "Reviewed article should not be null");
        assertEquals(Article.ReviewStatus.REJECTED, result.getReviewStatus(), 
            "Article review status must be REJECTED after admin rejection");
        
        // 验证审核意见被保存
        assertEquals(reviewComment, result.getReviewComment(), 
            "Review comment should be saved for rejected articles");
        
        // 验证发布时间不应该被设置
        assertNull(result.getPublishedAt(), 
            "Published time should not be set when article is rejected");
        
        // 验证文章内容保持不变
        assertEquals(title, result.getTitle(), 
            "Article title should remain unchanged after rejection");
        assertEquals(content, result.getContent(), 
            "Article content should remain unchanged after rejection");
        
        // 验证保存方法被调用
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 23: 文章审核状态更新（只能审核待审核文章）
     * 验证需求: 33.3
     * 
     * 对于任意非待审核状态的文章，管理员尝试审核应该被拒绝
     */
    @Property(tries = 100)
    void testCannotReviewNonPendingArticle(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll("nonPendingStatuses") Article.ReviewStatus nonPendingStatus,
            @ForAll("reviewComments") String reviewComment) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CarouselConfigRepository carouselConfigRepository = mock(CarouselConfigRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        WalletService walletService = mock(WalletService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        
        AdminServiceImpl adminService = new AdminServiceImpl(
            articleRepository, bloggerApplicationRepository, userRepository,
            carouselConfigRepository, walletRepository, walletService, transactionRepository
        );
        
        // 创建非待审核状态的文章
        Article nonPendingArticle = new Article();
        nonPendingArticle.setId(articleId);
        nonPendingArticle.setUserId(userId);
        nonPendingArticle.setTitle(title);
        nonPendingArticle.setContent(content);
        nonPendingArticle.setReviewStatus(nonPendingStatus);
        nonPendingArticle.setReviewComment("Previous review comment");
        nonPendingArticle.setIsPaid(false);
        
        if (nonPendingStatus == Article.ReviewStatus.APPROVED) {
            nonPendingArticle.setPublishedAt(LocalDateTime.now().minusDays(1));
        }
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(nonPendingArticle));
        
        // 属性验证：尝试审核非待审核文章应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminService.reviewArticle(articleId, true, reviewComment);
        }, String.format("Cannot review article with status %s", nonPendingStatus));
        
        assertEquals("ARTICLE_NOT_PENDING", exception.getErrorCode(), 
            "Should throw ARTICLE_NOT_PENDING error");
        assertEquals("文章不是待审核状态", exception.getMessage(), 
            "Error message should indicate article is not pending");
        
        // 验证没有保存文章（状态不应该改变）
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, never()).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 23: 文章审核状态更新（文章不存在）
     * 验证需求: 33.3
     * 
     * 对于任意不存在的文章ID，管理员尝试审核应该被拒绝
     */
    @Property(tries = 100)
    void testCannotReviewNonExistentArticle(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll("reviewComments") String reviewComment) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CarouselConfigRepository carouselConfigRepository = mock(CarouselConfigRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        WalletService walletService = mock(WalletService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        
        AdminServiceImpl adminService = new AdminServiceImpl(
            articleRepository, bloggerApplicationRepository, userRepository,
            carouselConfigRepository, walletRepository, walletService, transactionRepository
        );
        
        // 模拟文章不存在
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());
        
        // 属性验证：尝试审核不存在的文章应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminService.reviewArticle(articleId, true, reviewComment);
        }, "Cannot review non-existent article");
        
        assertEquals("ARTICLE_NOT_FOUND", exception.getErrorCode(), 
            "Should throw ARTICLE_NOT_FOUND error");
        assertEquals("文章不存在", exception.getMessage(), 
            "Error message should indicate article not found");
        
        // 验证没有保存操作
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, never()).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 23: 文章审核状态更新（审核意见可选）
     * 验证需求: 33.3
     * 
     * 对于任意待审核文章，管理员可以不提供审核意见（null或空字符串）
     */
    @Property(tries = 100)
    void testArticleReviewWithOptionalComment(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll("optionalReviewComments") String reviewComment,
            @ForAll boolean approved) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CarouselConfigRepository carouselConfigRepository = mock(CarouselConfigRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        WalletService walletService = mock(WalletService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        
        AdminServiceImpl adminService = new AdminServiceImpl(
            articleRepository, bloggerApplicationRepository, userRepository,
            carouselConfigRepository, walletRepository, walletService, transactionRepository
        );
        
        // 创建待审核的文章
        Article pendingArticle = new Article();
        pendingArticle.setId(articleId);
        pendingArticle.setUserId(userId);
        pendingArticle.setTitle(title);
        pendingArticle.setContent(content);
        pendingArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        pendingArticle.setReviewComment(null);
        pendingArticle.setPublishedAt(null);
        pendingArticle.setIsPaid(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(pendingArticle));
        
        // 模拟保存操作
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 执行审核操作（可能没有审核意见）
        Article result = adminService.reviewArticle(articleId, approved, reviewComment);
        
        // 属性验证：审核应该成功，即使没有审核意见
        assertNotNull(result, "Reviewed article should not be null");
        
        if (approved) {
            assertEquals(Article.ReviewStatus.APPROVED, result.getReviewStatus(), 
                "Article should be approved");
            assertNotNull(result.getPublishedAt(), 
                "Published time should be set for approved articles");
        } else {
            assertEquals(Article.ReviewStatus.REJECTED, result.getReviewStatus(), 
                "Article should be rejected");
            assertNull(result.getPublishedAt(), 
                "Published time should not be set for rejected articles");
        }
        
        // 验证审核意见被保存（即使是null或空字符串）
        assertEquals(reviewComment, result.getReviewComment(), 
            "Review comment should be saved as provided (including null or empty)");
        
        // 验证保存方法被调用
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 23: 文章审核状态更新（幂等性）
     * 验证需求: 33.3
     * 
     * 对于任意待审核文章，多次审核通过应该保持相同的结果（除了时间戳）
     */
    @Property(tries = 100)
    void testArticleReviewIdempotency(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll("reviewComments") String reviewComment) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CarouselConfigRepository carouselConfigRepository = mock(CarouselConfigRepository.class);
        WalletRepository walletRepository = mock(WalletRepository.class);
        WalletService walletService = mock(WalletService.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        
        AdminServiceImpl adminService = new AdminServiceImpl(
            articleRepository, bloggerApplicationRepository, userRepository,
            carouselConfigRepository, walletRepository, walletService, transactionRepository
        );
        
        // 创建待审核的文章
        Article pendingArticle = new Article();
        pendingArticle.setId(articleId);
        pendingArticle.setUserId(userId);
        pendingArticle.setTitle(title);
        pendingArticle.setContent(content);
        pendingArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        pendingArticle.setReviewComment(null);
        pendingArticle.setPublishedAt(null);
        pendingArticle.setIsPaid(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(pendingArticle));
        
        // 模拟保存操作
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 第一次审核
        Article firstResult = adminService.reviewArticle(articleId, true, reviewComment);
        
        // 属性验证：第一次审核应该成功
        assertNotNull(firstResult, "First review result should not be null");
        assertEquals(Article.ReviewStatus.APPROVED, firstResult.getReviewStatus(), 
            "Article should be approved after first review");
        
        // 更新mock以返回已审核的文章
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(firstResult));
        
        // 尝试第二次审核（应该失败，因为文章已经不是PENDING状态）
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            adminService.reviewArticle(articleId, true, reviewComment);
        }, "Cannot review already reviewed article");
        
        assertEquals("ARTICLE_NOT_PENDING", exception.getErrorCode(), 
            "Should throw ARTICLE_NOT_PENDING error on second review attempt");
        
        // 验证第一次审核调用了保存方法，第二次没有
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * 生成非待审核状态（APPROVED或REJECTED）
     */
    @Provide
    Arbitrary<Article.ReviewStatus> nonPendingStatuses() {
        return Arbitraries.of(
            Article.ReviewStatus.APPROVED,
            Article.ReviewStatus.REJECTED
        );
    }

    /**
     * 生成有效的文章标题（非空，1-200字符）
     */
    @Provide
    Arbitrary<String> validTitles() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(1)
                .ofMaxLength(50)
                .map(s -> "Article Title " + s);
    }

    /**
     * 生成有效的文章内容（非空，10-10000字符）
     */
    @Provide
    Arbitrary<String> validContents() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(10)
                .ofMaxLength(200)
                .map(s -> "This is article content: " + s);
    }

    /**
     * 生成审核意见（非空字符串）
     */
    @Provide
    Arbitrary<String> reviewComments() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(100)
                .map(s -> "Review comment: " + s);
    }

    /**
     * 生成可选的审核意见（包括null和空字符串）
     */
    @Provide
    Arbitrary<String> optionalReviewComments() {
        Arbitrary<String> validComments = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(100)
                .map(s -> "Review comment: " + s);
        
        Arbitrary<String> nullOrEmpty = Arbitraries.of(null, "", "   ");
        
        return Arbitraries.oneOf(validComments, nullOrEmpty);
    }
}
