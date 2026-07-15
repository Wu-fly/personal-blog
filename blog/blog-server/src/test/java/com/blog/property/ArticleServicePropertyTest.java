package com.blog.property;

import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.ArticleServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ArticleService属性测试
 * 使用jqwik进行基于属性的测试
 */
class ArticleServicePropertyTest {

    /**
     * Feature: personal-blog-system, Property 4: 文章创建审核状态
     * 验证需求: 2.1
     * 
     * 对于任意新创建的文章，其审核状态应该被设置为"待审核"
     */
    @Property(tries = 100)
    void testArticleCreationReviewStatus(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll @LongRange(min = 1, max = 100) Long categoryId,
            @ForAll boolean isPaid,
            @ForAll("validPrices") BigDecimal price,
            @ForAll("coverImages") String coverImage) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 模拟博主用户存在
        User blogger = new User();
        blogger.setId(userId);
        blogger.setPhone("13800138000");
        blogger.setEmail("blogger@example.com");
        blogger.setRole(User.UserRole.BLOGGER);
        blogger.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(blogger));
        
        // 创建文章对象
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setCategoryId(categoryId);
        article.setIsPaid(isPaid);
        
        // 如果是付费文章，设置价格
        if (isPaid && price.compareTo(BigDecimal.ZERO) > 0) {
            article.setPrice(price);
        } else if (isPaid) {
            // 如果是付费文章但价格无效，应该抛出异常
            article.setPrice(price);
            
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                articleService.createArticle(article);
            });
            
            assertEquals("付费文章价格必须大于0", exception.getMessage());
            return; // 结束此次测试
        }
        
        // 设置封面图片
        if (coverImage != null && !coverImage.trim().isEmpty()) {
            article.setCoverImage(coverImage);
        }
        
        // 模拟保存文章
        Article savedArticle = new Article();
        savedArticle.setId(1L);
        savedArticle.setUserId(userId);
        savedArticle.setTitle(article.getTitle());
        savedArticle.setContent(article.getContent());
        savedArticle.setCategoryId(categoryId);
        savedArticle.setIsPaid(isPaid);
        savedArticle.setPrice(article.getPrice());
        savedArticle.setCoverImage(article.getCoverImage() != null && !article.getCoverImage().trim().isEmpty() 
            ? article.getCoverImage() : "/default-cover.jpg");
        savedArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        savedArticle.setViewCount(0);
        savedArticle.setLikeCount(0);
        savedArticle.setFavoriteCount(0);
        savedArticle.setPurchaseCount(0);
        savedArticle.setIsPinned(false);
        
        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        
        // 执行创建文章
        Article result = articleService.createArticle(article);
        
        // 属性验证：新创建的文章审核状态必须是PENDING
        assertNotNull(result, "Created article should not be null");
        assertEquals(Article.ReviewStatus.PENDING, result.getReviewStatus(), 
            "Newly created article must have PENDING review status");
        
        // 验证其他初始化字段
        assertEquals(0, result.getViewCount(), 
            "View count should be initialized to 0");
        assertEquals(0, result.getLikeCount(), 
            "Like count should be initialized to 0");
        assertEquals(0, result.getFavoriteCount(), 
            "Favorite count should be initialized to 0");
        assertEquals(0, result.getPurchaseCount(), 
            "Purchase count should be initialized to 0");
        assertEquals(false, result.getIsPinned(), 
            "Article should not be pinned by default");
        
        // 验证封面图片设置
        if (coverImage == null || coverImage.trim().isEmpty()) {
            assertEquals("/default-cover.jpg", result.getCoverImage(), 
                "Default cover image should be set when not provided");
        } else {
            assertEquals(coverImage, result.getCoverImage(), 
                "Cover image should be preserved when provided");
        }
        
        // 验证保存方法被调用
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(userRepository, times(1)).findById(userId);
    }

    /**
     * Feature: personal-blog-system, Property 4: 文章创建审核状态（管理员场景）
     * 验证需求: 2.1
     * 
     * 管理员创建的文章也应该设置为待审核状态
     */
    @Property(tries = 100)
    void testArticleCreationReviewStatusForAdmin(
            @ForAll @LongRange(min = 1, max = 1000000) Long adminId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 模拟管理员用户存在
        User admin = new User();
        admin.setId(adminId);
        admin.setPhone("13900139000");
        admin.setEmail("admin@example.com");
        admin.setRole(User.UserRole.ADMIN);
        admin.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        
        // 创建文章对象
        Article article = new Article();
        article.setUserId(adminId);
        article.setTitle(title);
        article.setContent(content);
        article.setIsPaid(false);
        
        // 模拟保存文章
        Article savedArticle = new Article();
        savedArticle.setId(1L);
        savedArticle.setUserId(adminId);
        savedArticle.setTitle(article.getTitle());
        savedArticle.setContent(article.getContent());
        savedArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        savedArticle.setCoverImage("/default-cover.jpg");
        savedArticle.setViewCount(0);
        savedArticle.setLikeCount(0);
        savedArticle.setFavoriteCount(0);
        savedArticle.setPurchaseCount(0);
        savedArticle.setIsPinned(false);
        
        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        
        // 执行创建文章
        Article result = articleService.createArticle(article);
        
        // 属性验证：即使是管理员创建的文章，审核状态也必须是PENDING
        assertNotNull(result, "Created article should not be null");
        assertEquals(Article.ReviewStatus.PENDING, result.getReviewStatus(), 
            "Even admin's article must have PENDING review status");
        
        // 验证保存方法被调用
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 4: 文章创建审核状态（非博主用户拒绝）
     * 验证需求: 2.1, 39.2
     * 
     * 非博主用户尝试创建文章应该被拒绝
     */
    @Property(tries = 100)
    void testArticleCreationRejectedForNonBlogger(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 模拟普通用户（非博主）
        User regularUser = new User();
        regularUser.setId(userId);
        regularUser.setPhone("13700137000");
        regularUser.setEmail("user@example.com");
        regularUser.setRole(User.UserRole.USER);
        regularUser.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(regularUser));
        
        // 创建文章对象
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setIsPaid(false);
        
        // 属性验证：非博主用户创建文章应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.createArticle(article);
        });
        
        assertEquals("只有博主才能发布文章", exception.getMessage());
        
        // 验证没有保存文章
        verify(articleRepository, never()).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 5: 文章编辑重置审核
     * 验证需求: 2.2
     * 
     * 对于任意已审核通过的文章，编辑后其审核状态应该被重置为"待审核"
     */
    @Property(tries = 100)
    void testArticleEditResetsReviewStatus(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String originalTitle,
            @ForAll("validContents") String originalContent,
            @ForAll("validTitles") String newTitle,
            @ForAll("validContents") String newContent,
            @ForAll("reviewStatuses") Article.ReviewStatus originalStatus) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建已存在的文章（具有原始审核状态）
        Article existingArticle = new Article();
        existingArticle.setId(articleId);
        existingArticle.setUserId(userId);
        existingArticle.setTitle(originalTitle);
        existingArticle.setContent(originalContent);
        existingArticle.setReviewStatus(originalStatus);
        existingArticle.setReviewComment("Original review comment");
        existingArticle.setIsPaid(false);
        existingArticle.setCoverImage("/original-cover.jpg");
        existingArticle.setViewCount(100);
        existingArticle.setLikeCount(50);
        existingArticle.setFavoriteCount(30);
        existingArticle.setPurchaseCount(0);
        existingArticle.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        
        // 创建更新后的文章对象
        Article updatedArticle = new Article();
        updatedArticle.setUserId(userId);
        updatedArticle.setTitle(newTitle);
        updatedArticle.setContent(newContent);
        updatedArticle.setIsPaid(false);
        
        // 模拟保存操作
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 执行更新文章
        Article result = articleService.updateArticle(articleId, updatedArticle);
        
        // 属性验证：编辑后的文章审核状态必须被重置为PENDING
        assertNotNull(result, "Updated article should not be null");
        assertEquals(Article.ReviewStatus.PENDING, result.getReviewStatus(), 
            String.format("Article review status must be reset to PENDING after edit (was %s)", originalStatus));
        
        // 验证审核意见被清空
        assertNull(result.getReviewComment(), 
            "Review comment should be cleared after edit");
        
        // 验证文章内容已更新
        assertEquals(newTitle, result.getTitle(), 
            "Article title should be updated");
        assertEquals(newContent, result.getContent(), 
            "Article content should be updated");
        
        // 验证其他字段保持不变（如浏览量、点赞数等）
        assertEquals(100, result.getViewCount(), 
            "View count should remain unchanged after edit");
        assertEquals(50, result.getLikeCount(), 
            "Like count should remain unchanged after edit");
        assertEquals(30, result.getFavoriteCount(), 
            "Favorite count should remain unchanged after edit");
        
        // 验证保存方法被调用
        verify(articleRepository, times(1)).save(any(Article.class));
        verify(articleRepository, times(1)).findById(articleId);
    }

    /**
     * Feature: personal-blog-system, Property 5: 文章编辑重置审核（付费文章场景）
     * 验证需求: 2.2
     * 
     * 付费文章编辑后也应该重置审核状态
     */
    @Property(tries = 100)
    void testPaidArticleEditResetsReviewStatus(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String originalTitle,
            @ForAll("validContents") String originalContent,
            @ForAll("validTitles") String newTitle,
            @ForAll("validContents") String newContent,
            @ForAll("validPrices") BigDecimal originalPrice,
            @ForAll("validPrices") BigDecimal newPrice) {
        
        // 只测试有效的付费价格
        if (originalPrice.compareTo(BigDecimal.ZERO) <= 0 || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建已审核通过的付费文章
        Article existingArticle = new Article();
        existingArticle.setId(articleId);
        existingArticle.setUserId(userId);
        existingArticle.setTitle(originalTitle);
        existingArticle.setContent(originalContent);
        existingArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
        existingArticle.setReviewComment("Approved by admin");
        existingArticle.setIsPaid(true);
        existingArticle.setPrice(originalPrice);
        existingArticle.setCoverImage("/paid-article-cover.jpg");
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        
        // 创建更新后的文章对象
        Article updatedArticle = new Article();
        updatedArticle.setUserId(userId);
        updatedArticle.setTitle(newTitle);
        updatedArticle.setContent(newContent);
        updatedArticle.setIsPaid(true);
        updatedArticle.setPrice(newPrice);
        
        // 模拟保存操作
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 执行更新文章
        Article result = articleService.updateArticle(articleId, updatedArticle);
        
        // 属性验证：付费文章编辑后审核状态也必须被重置为PENDING
        assertNotNull(result, "Updated paid article should not be null");
        assertEquals(Article.ReviewStatus.PENDING, result.getReviewStatus(), 
            "Paid article review status must be reset to PENDING after edit");
        
        // 验证审核意见被清空
        assertNull(result.getReviewComment(), 
            "Review comment should be cleared after edit");
        
        // 验证价格已更新
        assertEquals(newPrice, result.getPrice(), 
            "Article price should be updated");
        
        // 验证保存方法被调用
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 5: 文章编辑重置审核（权限验证）
     * 验证需求: 2.2
     * 
     * 只有文章作者可以编辑文章
     */
    @Property(tries = 100)
    void testArticleEditPermissionCheck(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long originalUserId,
            @ForAll @LongRange(min = 1, max = 1000000) Long differentUserId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content) {
        
        // 确保是不同的用户
        if (originalUserId.equals(differentUserId)) {
            return;
        }
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建已存在的文章（属于originalUserId）
        Article existingArticle = new Article();
        existingArticle.setId(articleId);
        existingArticle.setUserId(originalUserId);
        existingArticle.setTitle(title);
        existingArticle.setContent(content);
        existingArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
        existingArticle.setIsPaid(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(existingArticle));
        
        // 尝试用不同的用户编辑文章
        Article updatedArticle = new Article();
        updatedArticle.setUserId(differentUserId);
        updatedArticle.setTitle("New Title");
        updatedArticle.setContent("New Content");
        updatedArticle.setIsPaid(false);
        
        // 属性验证：非作者编辑文章应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.updateArticle(articleId, updatedArticle);
        });
        
        assertEquals("只能编辑自己的文章", exception.getMessage());
        
        // 验证没有保存文章
        verify(articleRepository, never()).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 6: 未审核文章不可见
     * 验证需求: 2.8
     * 
     * 对于任意审核状态为"待审核"或"审核拒绝"的文章，用户端查询应该不返回该文章
     */
    @Property(tries = 100)
    void testUnapprovedArticlesNotVisible(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll("unapprovedStatuses") Article.ReviewStatus unapprovedStatus) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建未审核或审核拒绝的文章
        Article unapprovedArticle = new Article();
        unapprovedArticle.setId(articleId);
        unapprovedArticle.setUserId(userId);
        unapprovedArticle.setTitle(title);
        unapprovedArticle.setContent(content);
        unapprovedArticle.setReviewStatus(unapprovedStatus);
        unapprovedArticle.setIsPaid(false);
        unapprovedArticle.setCoverImage("/cover.jpg");
        unapprovedArticle.setViewCount(0);
        unapprovedArticle.setLikeCount(0);
        unapprovedArticle.setFavoriteCount(0);
        unapprovedArticle.setPurchaseCount(0);
        unapprovedArticle.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(unapprovedArticle));
        
        // 属性验证：查询未审核或审核拒绝的文章应该返回空（对用户端不可见）
        // 注意：这里我们测试的是getArticleById方法在用户端的行为
        // 如果文章未审核，应该抛出异常或返回null
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.getArticleByIdForUser(articleId);
        }, String.format("Article with %s status should not be visible to users", unapprovedStatus));
        
        assertEquals("文章不存在或未通过审核", exception.getMessage(),
            String.format("Should reject access to article with %s status", unapprovedStatus));
        
        // 验证查询方法被调用
        verify(articleRepository, times(1)).findById(articleId);
    }

    /**
     * Feature: personal-blog-system, Property 6: 未审核文章不可见（已审核文章可见）
     * 验证需求: 2.8
     * 
     * 对于任意审核状态为"已通过"的文章，用户端查询应该返回该文章
     */
    @Property(tries = 100)
    void testApprovedArticlesVisible(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建已审核通过的文章
        Article approvedArticle = new Article();
        approvedArticle.setId(articleId);
        approvedArticle.setUserId(userId);
        approvedArticle.setTitle(title);
        approvedArticle.setContent(content);
        approvedArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
        approvedArticle.setIsPaid(false);
        approvedArticle.setCoverImage("/cover.jpg");
        approvedArticle.setViewCount(100);
        approvedArticle.setLikeCount(50);
        approvedArticle.setFavoriteCount(30);
        approvedArticle.setPurchaseCount(0);
        approvedArticle.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(approvedArticle));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 属性验证：查询已审核通过的文章应该成功返回
        Article result = articleService.getArticleByIdForUser(articleId);
        
        assertNotNull(result, "Approved article should be visible to users");
        assertEquals(articleId, result.getId(), "Should return the correct article");
        assertEquals(Article.ReviewStatus.APPROVED, result.getReviewStatus(), 
            "Returned article should have APPROVED status");
        assertEquals(title, result.getTitle(), "Article title should match");
        assertEquals(content, result.getContent(), "Article content should match");
        
        // 验证浏览计数增加（从100增加到101）
        assertTrue(result.getViewCount() >= 101, 
            "View count should be incremented when article is viewed (was 100, now " + result.getViewCount() + ")");
        
        // 验证查询方法被调用
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 6: 未审核文章不可见（博主可以查看自己的未审核文章）
     * 验证需求: 2.8
     * 
     * 博主应该能够查看自己的未审核文章（用于预览和编辑）
     */
    @Property(tries = 100)
    void testBloggerCanViewOwnUnapprovedArticles(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long bloggerId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll("unapprovedStatuses") Article.ReviewStatus unapprovedStatus) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建博主的未审核文章
        Article unapprovedArticle = new Article();
        unapprovedArticle.setId(articleId);
        unapprovedArticle.setUserId(bloggerId);
        unapprovedArticle.setTitle(title);
        unapprovedArticle.setContent(content);
        unapprovedArticle.setReviewStatus(unapprovedStatus);
        unapprovedArticle.setIsPaid(false);
        unapprovedArticle.setCoverImage("/cover.jpg");
        unapprovedArticle.setViewCount(0);
        unapprovedArticle.setLikeCount(0);
        unapprovedArticle.setFavoriteCount(0);
        unapprovedArticle.setPurchaseCount(0);
        unapprovedArticle.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(unapprovedArticle));
        
        // 属性验证：博主查看自己的未审核文章应该成功（用于博主端管理）
        Article result = articleService.getArticleByIdForBlogger(articleId, bloggerId);
        
        assertNotNull(result, "Blogger should be able to view their own unapproved articles");
        assertEquals(articleId, result.getId(), "Should return the correct article");
        assertEquals(bloggerId, result.getUserId(), "Article should belong to the blogger");
        assertEquals(unapprovedStatus, result.getReviewStatus(), 
            String.format("Article should have %s status", unapprovedStatus));
        
        // 验证查询方法被调用
        verify(articleRepository, times(1)).findById(articleId);
    }

    /**
     * Feature: personal-blog-system, Property 6: 未审核文章不可见（博主不能查看他人的未审核文章）
     * 验证需求: 2.8
     * 
     * 博主不应该能够查看其他博主的未审核文章
     */
    @Property(tries = 100)
    void testBloggerCannotViewOthersUnapprovedArticles(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleOwnerId,
            @ForAll @LongRange(min = 1, max = 1000000) Long otherBloggerId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll("unapprovedStatuses") Article.ReviewStatus unapprovedStatus) {
        
        // 确保是不同的博主
        if (articleOwnerId.equals(otherBloggerId)) {
            return;
        }
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建其他博主的未审核文章
        Article unapprovedArticle = new Article();
        unapprovedArticle.setId(articleId);
        unapprovedArticle.setUserId(articleOwnerId);
        unapprovedArticle.setTitle(title);
        unapprovedArticle.setContent(content);
        unapprovedArticle.setReviewStatus(unapprovedStatus);
        unapprovedArticle.setIsPaid(false);
        unapprovedArticle.setCoverImage("/cover.jpg");
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(unapprovedArticle));
        
        // 属性验证：博主尝试查看其他博主的未审核文章应该被拒绝
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.getArticleByIdForBlogger(articleId, otherBloggerId);
        }, "Blogger should not be able to view other blogger's unapproved articles");
        
        assertEquals("只能查看自己的文章", exception.getMessage());
        
        // 验证查询方法被调用
        verify(articleRepository, times(1)).findById(articleId);
    }

    /**
     * 生成未审核状态（PENDING或REJECTED）
     */
    @Provide
    Arbitrary<Article.ReviewStatus> unapprovedStatuses() {
        return Arbitraries.of(
            Article.ReviewStatus.PENDING,
            Article.ReviewStatus.REJECTED
        );
    }

    /**
     * 生成有效的价格（0到1000之间，保留2位小数）
     */
    @Provide
    Arbitrary<BigDecimal> validPrices() {
        return Arbitraries.doubles()
                .between(0.0, 1000.0)
                .map(d -> BigDecimal.valueOf(Math.round(d * 100.0) / 100.0));
    }
    
    /**
     * 生成所有可能的审核状态
     */
    @Provide
    Arbitrary<Article.ReviewStatus> reviewStatuses() {
        return Arbitraries.of(
            Article.ReviewStatus.PENDING,
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
     * 生成有效的文章内容（非空，1-10000字符）
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
     * 生成封面图片URL（包括null和空字符串）
     */
    @Provide
    Arbitrary<String> coverImages() {
        Arbitrary<String> validUrls = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(20)
                .map(s -> "/images/" + s + ".jpg");
        
        Arbitrary<String> nullOrEmpty = Arbitraries.of(null, "", "   ");
        
        return Arbitraries.oneOf(validUrls, nullOrEmpty);
    }

    /**
     * Feature: personal-blog-system, Property 8: 文章浏览计数增加
     * 验证需求: 3.3
     * 
     * 对于任意文章，每次查看详情应该使浏览计数增加1
     */
    @Property(tries = 100)
    void testArticleViewCountIncrement(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll @LongRange(min = 0, max = 10000) int initialViewCount) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建已审核通过的文章
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        article.setIsPaid(false);
        article.setCoverImage("/cover.jpg");
        article.setViewCount(initialViewCount);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setPurchaseCount(0);
        article.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        // 模拟保存操作，返回更新后的文章
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 执行查看文章详情
        Article result = articleService.getArticleByIdForUser(articleId);
        
        // 属性验证：浏览计数应该增加1
        assertNotNull(result, "Article should not be null");
        assertEquals(initialViewCount + 1, result.getViewCount(), 
            String.format("View count should increment by 1 (was %d, now %d)", 
                initialViewCount, result.getViewCount()));
        
        // 验证查询和保存方法被调用
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 8: 文章浏览计数增加（多次查看）
     * 验证需求: 3.3
     * 
     * 对于任意文章，多次查看应该使浏览计数累加
     */
    @Property(tries = 100)
    void testArticleViewCountIncrementMultipleTimes(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll @LongRange(min = 0, max = 1000) int initialViewCount,
            @ForAll @LongRange(min = 1, max = 10) int viewTimes) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建已审核通过的文章
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        article.setIsPaid(false);
        article.setCoverImage("/cover.jpg");
        article.setViewCount(initialViewCount);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setPurchaseCount(0);
        article.setIsPinned(false);
        
        // 使用一个容器来保存当前的viewCount状态
        final int[] currentViewCount = {initialViewCount};
        
        // 每次findById都返回一个新的article对象，包含最新的viewCount
        when(articleRepository.findById(articleId)).thenAnswer(invocation -> {
            Article freshArticle = new Article();
            freshArticle.setId(articleId);
            freshArticle.setUserId(userId);
            freshArticle.setTitle(title);
            freshArticle.setContent(content);
            freshArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
            freshArticle.setIsPaid(false);
            freshArticle.setCoverImage("/cover.jpg");
            freshArticle.setViewCount(currentViewCount[0]);
            freshArticle.setLikeCount(0);
            freshArticle.setFavoriteCount(0);
            freshArticle.setPurchaseCount(0);
            freshArticle.setIsPinned(false);
            return Optional.of(freshArticle);
        });
        
        // 模拟保存操作，每次保存都更新viewCount并返回
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            // 更新当前viewCount状态
            currentViewCount[0] = saved.getViewCount();
            return saved;
        });
        
        // 执行多次查看文章详情
        Article result = null;
        for (int i = 0; i < viewTimes; i++) {
            result = articleService.getArticleByIdForUser(articleId);
            assertNotNull(result, "Article should not be null after view " + (i + 1));
        }
        
        // 属性验证：浏览计数应该增加viewTimes次
        assertNotNull(result, "Article should not be null");
        assertEquals(initialViewCount + viewTimes, result.getViewCount(), 
            String.format("View count should increment by %d (was %d, now %d)", 
                viewTimes, initialViewCount, result.getViewCount()));
        
        // 验证查询和保存方法被调用viewTimes次
        verify(articleRepository, times(viewTimes)).findById(articleId);
        verify(articleRepository, times(viewTimes)).save(any(Article.class));
    }

    /**
     * Feature: personal-blog-system, Property 8: 文章浏览计数增加（未审核文章不增加）
     * 验证需求: 3.3, 2.8
     * 
     * 对于任意未审核的文章，用户端查看应该被拒绝，浏览计数不应该增加
     */
    @Property(tries = 100)
    void testUnapprovedArticleViewCountNotIncrement(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll @LongRange(min = 0, max = 10000) int initialViewCount,
            @ForAll("unapprovedStatuses") Article.ReviewStatus unapprovedStatus) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建未审核的文章
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setReviewStatus(unapprovedStatus);
        article.setIsPaid(false);
        article.setCoverImage("/cover.jpg");
        article.setViewCount(initialViewCount);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setPurchaseCount(0);
        article.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        // 属性验证：查看未审核文章应该抛出异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.getArticleByIdForUser(articleId);
        }, String.format("Viewing unapproved article with status %s should throw exception", unapprovedStatus));
        
        assertEquals("文章不存在或未通过审核", exception.getMessage());
        
        // 验证浏览计数没有增加（save方法不应该被调用）
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, never()).save(any(Article.class));
        
        // 验证viewCount保持不变
        assertEquals(initialViewCount, article.getViewCount(), 
            "View count should not change for unapproved articles");
    }

    /**
     * Feature: personal-blog-system, Property 8: 文章浏览计数增加（付费文章）
     * 验证需求: 3.3
     * 
     * 对于任意付费文章，查看详情也应该增加浏览计数
     */
    @Property(tries = 100)
    void testPaidArticleViewCountIncrement(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll @LongRange(min = 0, max = 10000) int initialViewCount,
            @ForAll("validPrices") BigDecimal price) {
        
        // 只测试有效的付费价格
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建已审核通过的付费文章
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        article.setIsPaid(true);
        article.setPrice(price);
        article.setCoverImage("/paid-cover.jpg");
        article.setViewCount(initialViewCount);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setPurchaseCount(0);
        article.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        // 模拟保存操作
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            return saved;
        });
        
        // 执行查看文章详情
        Article result = articleService.getArticleByIdForUser(articleId);
        
        // 属性验证：付费文章的浏览计数也应该增加1
        assertNotNull(result, "Paid article should not be null");
        assertEquals(initialViewCount + 1, result.getViewCount(), 
            String.format("Paid article view count should increment by 1 (was %d, now %d)", 
                initialViewCount, result.getViewCount()));
        assertEquals(true, result.getIsPaid(), "Article should be marked as paid");
        assertEquals(price, result.getPrice(), "Article price should be preserved");
        
        // 验证查询和保存方法被调用
        verify(articleRepository, times(1)).findById(articleId);
        verify(articleRepository, times(1)).save(any(Article.class));
    }
}
