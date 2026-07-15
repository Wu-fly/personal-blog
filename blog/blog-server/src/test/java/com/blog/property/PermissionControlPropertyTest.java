package com.blog.property;

import com.blog.entity.*;
import com.blog.exception.BusinessException;
import com.blog.repository.*;
import com.blog.service.impl.*;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 权限控制属性测试
 * 使用jqwik进行基于属性的测试
 * 
 * 注意：本测试验证业务逻辑层面的权限控制，而不是Spring Security的注解
 * Spring Security的@PreAuthorize注解需要在集成测试中验证
 */
class PermissionControlPropertyTest {

    /**
     * Feature: personal-blog-system, Property 24: 访客权限限制
     * 验证需求: 38.2
     * 
     * 对于任意未登录访客，尝试访问需要登录的功能应该在业务逻辑层被拒绝
     * 
     * 本测试验证服务层的业务逻辑验证，确保即使绕过Spring Security，
     * 业务逻辑层也会进行必要的权限检查
     */
    @Property(tries = 100)
    void testVisitorPermissionRestrictionsAtBusinessLogicLayer(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll("validCommentContent") String commentContent) {
        
        // 测试1: 验证评论功能需要有效的用户和文章
        testCommentRequiresValidUserAndArticle(userId, articleId, commentContent);
        
        // 测试2: 验证点赞功能需要有效的文章
        testLikeRequiresValidArticle(userId, articleId);
        
        // 测试3: 验证收藏功能需要有效的文章
        testFavoriteRequiresValidArticle(userId, articleId);
        
        // 测试4: 验证关注功能需要有效的用户
        testFollowRequiresValidUsers(userId, articleId);
    }

    /**
     * 测试评论功能需要有效的用户和文章
     */
    private void testCommentRequiresValidUserAndArticle(Long userId, Long articleId, String content) {
        CommentRepository commentRepository = mock(CommentRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        CommentServiceImpl commentService = new CommentServiceImpl(
                commentRepository, articleRepository, userRepository);
        
        // 场景1: 文章不存在
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());
        
        Comment comment1 = new Comment();
        comment1.setUserId(userId);
        comment1.setArticleId(articleId);
        comment1.setContent(content);
        
        BusinessException exception1 = assertThrows(BusinessException.class, () -> {
            commentService.createComment(comment1);
        }, "Comment should fail when article does not exist");
        
        assertTrue(exception1.getMessage().contains("文章不存在") || 
                   exception1.getMessage().contains("Article not found"),
                "Error message should indicate article not found");
        
        // 场景2: 用户不存在
        Article article = new Article();
        article.setId(articleId);
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        Comment comment2 = new Comment();
        comment2.setUserId(userId);
        comment2.setArticleId(articleId);
        comment2.setContent(content);
        
        BusinessException exception2 = assertThrows(BusinessException.class, () -> {
            commentService.createComment(comment2);
        }, "Comment should fail when user does not exist");
        
        assertTrue(exception2.getMessage().contains("用户不存在") || 
                   exception2.getMessage().contains("User not found"),
                "Error message should indicate user not found");
    }

    /**
     * 测试点赞功能需要有效的文章
     */
    private void testLikeRequiresValidArticle(Long userId, Long articleId) {
        LikeRepository likeRepository = mock(LikeRepository.class);
        FavoriteRepository favoriteRepository = mock(FavoriteRepository.class);
        FollowRepository followRepository = mock(FollowRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        InteractionServiceImpl interactionService = new InteractionServiceImpl(
                likeRepository, favoriteRepository, followRepository, 
                articleRepository, userRepository);
        
        // 用户存在但文章不存在
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(false);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            interactionService.toggleLike(userId, articleId);
        }, "Like should fail when article does not exist");
        
        assertTrue(exception.getMessage().contains("文章不存在") || 
                   exception.getMessage().contains("Article not found"),
                "Error message should indicate article not found");
    }

    /**
     * 测试收藏功能需要有效的文章
     */
    private void testFavoriteRequiresValidArticle(Long userId, Long articleId) {
        LikeRepository likeRepository = mock(LikeRepository.class);
        FavoriteRepository favoriteRepository = mock(FavoriteRepository.class);
        FollowRepository followRepository = mock(FollowRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        InteractionServiceImpl interactionService = new InteractionServiceImpl(
                likeRepository, favoriteRepository, followRepository, 
                articleRepository, userRepository);
        
        // 用户存在但文章不存在
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(false);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            interactionService.toggleFavorite(userId, articleId);
        }, "Favorite should fail when article does not exist");
        
        assertTrue(exception.getMessage().contains("文章不存在") || 
                   exception.getMessage().contains("Article not found"),
                "Error message should indicate article not found");
    }

    /**
     * 测试关注功能需要有效的用户
     */
    private void testFollowRequiresValidUsers(Long followerId, Long followingId) {
        LikeRepository likeRepository = mock(LikeRepository.class);
        FavoriteRepository favoriteRepository = mock(FavoriteRepository.class);
        FollowRepository followRepository = mock(FollowRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        InteractionServiceImpl interactionService = new InteractionServiceImpl(
                likeRepository, favoriteRepository, followRepository, 
                articleRepository, userRepository);
        
        // 关注者不存在
        when(userRepository.existsById(followerId)).thenReturn(false);
        
        BusinessException exception1 = assertThrows(BusinessException.class, () -> {
            interactionService.toggleFollow(followerId, followingId);
        }, "Follow should fail when follower does not exist");
        
        assertTrue(exception1.getMessage().contains("不存在") || 
                   exception1.getMessage().contains("not found"),
                "Error message should indicate user not found");
        
        // 被关注者不存在
        when(userRepository.existsById(followerId)).thenReturn(true);
        when(userRepository.existsById(followingId)).thenReturn(false);
        
        BusinessException exception2 = assertThrows(BusinessException.class, () -> {
            interactionService.toggleFollow(followerId, followingId);
        }, "Follow should fail when following user does not exist");
        
        assertTrue(exception2.getMessage().contains("不存在") || 
                   exception2.getMessage().contains("not found"),
                "Error message should indicate user not found");
    }

    /**
     * Feature: personal-blog-system, Property 24: 访客权限限制（正向测试）
     * 验证需求: 38.2
     * 
     * 对于任意有效的用户和文章，业务逻辑应该允许操作
     */
    @Property(tries = 100)
    void testValidUserCanPerformOperations(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll("validCommentContent") String commentContent) {
        
        // 测试1: 有效用户可以发表评论
        testValidUserCanComment(userId, articleId, commentContent);
        
        // 测试2: 有效用户可以点赞
        testValidUserCanLike(userId, articleId);
        
        // 测试3: 有效用户可以收藏
        testValidUserCanFavorite(userId, articleId);
    }

    /**
     * 测试有效用户可以发表评论
     */
    private void testValidUserCanComment(Long userId, Long articleId, String content) {
        CommentRepository commentRepository = mock(CommentRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        CommentServiceImpl commentService = new CommentServiceImpl(
                commentRepository, articleRepository, userRepository);
        
        // 模拟有效的文章和用户
        Article article = new Article();
        article.setId(articleId);
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        Comment savedComment = new Comment();
        savedComment.setId(1L);
        savedComment.setUserId(userId);
        savedComment.setArticleId(articleId);
        savedComment.setContent(content);
        savedComment.setStatus(Comment.CommentStatus.APPROVED);
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setArticleId(articleId);
        comment.setContent(content);
        
        // 有效用户应该能够发表评论
        Comment result = assertDoesNotThrow(() -> {
            return commentService.createComment(comment);
        }, "Valid user should be able to create comment");
        
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(articleId, result.getArticleId());
        assertEquals(content, result.getContent());
    }

    /**
     * 测试有效用户可以点赞
     */
    private void testValidUserCanLike(Long userId, Long articleId) {
        LikeRepository likeRepository = mock(LikeRepository.class);
        FavoriteRepository favoriteRepository = mock(FavoriteRepository.class);
        FollowRepository followRepository = mock(FollowRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        InteractionServiceImpl interactionService = new InteractionServiceImpl(
                likeRepository, favoriteRepository, followRepository, 
                articleRepository, userRepository);
        
        // 模拟有效的用户和文章
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(likeRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(false);
        
        // 有效用户应该能够点赞
        boolean result = assertDoesNotThrow(() -> {
            return interactionService.toggleLike(userId, articleId);
        }, "Valid user should be able to like article");
        
        assertTrue(result, "Like operation should return true for new like");
    }

    /**
     * 测试有效用户可以收藏
     */
    private void testValidUserCanFavorite(Long userId, Long articleId) {
        LikeRepository likeRepository = mock(LikeRepository.class);
        FavoriteRepository favoriteRepository = mock(FavoriteRepository.class);
        FollowRepository followRepository = mock(FollowRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        InteractionServiceImpl interactionService = new InteractionServiceImpl(
                likeRepository, favoriteRepository, followRepository, 
                articleRepository, userRepository);
        
        // 模拟有效的用户和文章
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(false);
        
        // 有效用户应该能够收藏
        boolean result = assertDoesNotThrow(() -> {
            return interactionService.toggleFavorite(userId, articleId);
        }, "Valid user should be able to favorite article");
        
        assertTrue(result, "Favorite operation should return true for new favorite");
    }

    /**
     * Feature: personal-blog-system, Property 25: 博主权限控制
     * 验证需求: 39.2
     * 
     * 对于任意非博主用户，尝试使用博主专属功能（发布文章、设置公告等）应该被拒绝
     * 
     * 注意：本测试验证业务逻辑层面的权限控制
     * Spring Security的@PreAuthorize注解在集成测试中验证
     */
    @Property(tries = 100)
    void testBloggerPermissionControl(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validArticleTitle") String title,
            @ForAll("validArticleContent") String content,
            @ForAll("validAnnouncementContent") String announcement) {
        
        // 测试1: 非博主用户不能发布文章
        testNonBloggerCannotCreateArticle(userId, title, content);
        
        // 测试2: 非博主用户不能设置公告
        testNonBloggerCannotSetAnnouncement(userId, announcement);
        
        // 测试3: 非博主用户不能更新个人空间设置
        testNonBloggerCannotUpdateSpaceSettings(userId);
    }

    /**
     * 测试非博主用户不能发布文章
     */
    private void testNonBloggerCannotCreateArticle(Long userId, String title, String content) {
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
                articleRepository, userRepository, commentRepository);
        
        // 创建非博主用户（普通用户）
        User normalUser = new User();
        normalUser.setId(userId);
        normalUser.setPhone("13800138000");
        normalUser.setEmail("user@example.com");
        normalUser.setRole(User.UserRole.USER);  // 普通用户，不是博主
        normalUser.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));
        
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setCategoryId(1L);
        
        // 非博主用户尝试发布文章应该被拒绝
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.createArticle(article);
        }, "Non-blogger user should not be able to create article");
        
        assertTrue(exception.getMessage().contains("只有博主才能发布文章") || 
                   exception.getMessage().contains("PERMISSION_DENIED") ||
                   exception.getMessage().contains("blogger"),
                "Error message should indicate permission denied for non-blogger");
    }

    /**
     * 测试非博主用户不能设置公告
     */
    private void testNonBloggerCannotSetAnnouncement(Long userId, String content) {
        UserRepository userRepository = mock(UserRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        AnnouncementRepository announcementRepository = mock(AnnouncementRepository.class);
        SpaceSettingRepository spaceSettingRepository = mock(SpaceSettingRepository.class);
        
        BloggerServiceImpl bloggerService = new BloggerServiceImpl(
                userRepository, bloggerApplicationRepository, announcementRepository, spaceSettingRepository);
        
        // 创建非博主用户
        User normalUser = new User();
        normalUser.setId(userId);
        normalUser.setRole(User.UserRole.USER);
        normalUser.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));
        
        // 非博主用户尝试设置公告应该被拒绝
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bloggerService.saveAnnouncement(userId, content);
        }, "Non-blogger user should not be able to set announcement");
        
        assertTrue(exception.getMessage().contains("用户不是博主") || 
                   exception.getMessage().contains("NOT_BLOGGER") ||
                   exception.getMessage().contains("blogger"),
                "Error message should indicate user is not a blogger");
    }

    /**
     * 测试非博主用户不能更新个人空间设置
     */
    private void testNonBloggerCannotUpdateSpaceSettings(Long userId) {
        UserRepository userRepository = mock(UserRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        AnnouncementRepository announcementRepository = mock(AnnouncementRepository.class);
        SpaceSettingRepository spaceSettingRepository = mock(SpaceSettingRepository.class);
        
        BloggerServiceImpl bloggerService = new BloggerServiceImpl(
                userRepository, bloggerApplicationRepository, announcementRepository, spaceSettingRepository);
        
        // 创建非博主用户
        User normalUser = new User();
        normalUser.setId(userId);
        normalUser.setRole(User.UserRole.USER);
        normalUser.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(normalUser));
        
        // 非博主用户尝试更新个人空间设置应该被拒绝
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            bloggerService.saveSpaceSettings(userId, "#409EFF", null, null);
        }, "Non-blogger user should not be able to update space settings");
        
        assertTrue(exception.getMessage().contains("用户不是博主") || 
                   exception.getMessage().contains("NOT_BLOGGER") ||
                   exception.getMessage().contains("blogger"),
                "Error message should indicate user is not a blogger");
    }

    /**
     * Feature: personal-blog-system, Property 25: 博主权限控制（正向测试）
     * 验证需求: 39.2
     * 
     * 对于任意博主用户，应该能够使用博主专属功能
     */
    @Property(tries = 100)
    void testBloggerCanUseExclusiveFeatures(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validArticleTitle") String title,
            @ForAll("validArticleContent") String content,
            @ForAll("validAnnouncementContent") String announcement) {
        
        // 测试1: 博主用户可以发布文章
        testBloggerCanCreateArticle(userId, title, content);
        
        // 测试2: 博主用户可以设置公告
        testBloggerCanSetAnnouncement(userId, announcement);
        
        // 测试3: 博主用户可以更新个人空间设置
        testBloggerCanUpdateSpaceSettings(userId);
    }

    /**
     * 测试博主用户可以发布文章
     */
    private void testBloggerCanCreateArticle(Long userId, String title, String content) {
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
                articleRepository, userRepository, commentRepository);
        
        // 创建博主用户
        User blogger = new User();
        blogger.setId(userId);
        blogger.setPhone("13800138000");
        blogger.setEmail("blogger@example.com");
        blogger.setRole(User.UserRole.BLOGGER);  // 博主角色
        blogger.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(blogger));
        
        Article article = new Article();
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setCategoryId(1L);
        
        Article savedArticle = new Article();
        savedArticle.setId(1L);
        savedArticle.setUserId(userId);
        savedArticle.setTitle(title);
        savedArticle.setContent(content);
        savedArticle.setCategoryId(1L);
        savedArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        
        when(articleRepository.save(any(Article.class))).thenReturn(savedArticle);
        
        // 博主用户应该能够发布文章
        Article result = assertDoesNotThrow(() -> {
            return articleService.createArticle(article);
        }, "Blogger should be able to create article");
        
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(title, result.getTitle());
        assertEquals(Article.ReviewStatus.PENDING, result.getReviewStatus());
    }

    /**
     * 测试博主用户可以设置公告
     */
    private void testBloggerCanSetAnnouncement(Long userId, String content) {
        UserRepository userRepository = mock(UserRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        AnnouncementRepository announcementRepository = mock(AnnouncementRepository.class);
        SpaceSettingRepository spaceSettingRepository = mock(SpaceSettingRepository.class);
        
        BloggerServiceImpl bloggerService = new BloggerServiceImpl(
                userRepository, bloggerApplicationRepository, announcementRepository, spaceSettingRepository);
        
        // 创建博主用户
        User blogger = new User();
        blogger.setId(userId);
        blogger.setRole(User.UserRole.BLOGGER);
        blogger.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(blogger));
        when(announcementRepository.findByUserId(userId)).thenReturn(Optional.empty());
        
        Announcement savedAnnouncement = new Announcement();
        savedAnnouncement.setId(1L);
        savedAnnouncement.setUserId(userId);
        savedAnnouncement.setContent(content);
        
        when(announcementRepository.save(any(Announcement.class))).thenReturn(savedAnnouncement);
        
        // 博主用户应该能够设置公告
        Announcement result = assertDoesNotThrow(() -> {
            return bloggerService.saveAnnouncement(userId, content);
        }, "Blogger should be able to set announcement");
        
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(content, result.getContent());
    }

    /**
     * 测试博主用户可以更新个人空间设置
     */
    private void testBloggerCanUpdateSpaceSettings(Long userId) {
        UserRepository userRepository = mock(UserRepository.class);
        BloggerApplicationRepository bloggerApplicationRepository = mock(BloggerApplicationRepository.class);
        AnnouncementRepository announcementRepository = mock(AnnouncementRepository.class);
        SpaceSettingRepository spaceSettingRepository = mock(SpaceSettingRepository.class);
        
        BloggerServiceImpl bloggerService = new BloggerServiceImpl(
                userRepository, bloggerApplicationRepository, announcementRepository, spaceSettingRepository);
        
        // 创建博主用户
        User blogger = new User();
        blogger.setId(userId);
        blogger.setRole(User.UserRole.BLOGGER);
        blogger.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(blogger));
        when(spaceSettingRepository.findByUserId(userId)).thenReturn(Optional.empty());
        
        SpaceSetting savedSettings = new SpaceSetting();
        savedSettings.setId(1L);
        savedSettings.setUserId(userId);
        savedSettings.setThemeColor("#409EFF");
        
        when(spaceSettingRepository.save(any(SpaceSetting.class))).thenReturn(savedSettings);
        
        // 博主用户应该能够更新个人空间设置
        SpaceSetting result = assertDoesNotThrow(() -> {
            return bloggerService.saveSpaceSettings(userId, "#409EFF", null, null);
        }, "Blogger should be able to update space settings");
        
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("#409EFF", result.getThemeColor());
    }

    /**
     * 生成有效的评论内容
     */
    @Provide
    Arbitrary<String> validCommentContent() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(100)
                .map(s -> "Comment: " + s);
    }

    /**
     * 生成有效的文章标题
     */
    @Provide
    Arbitrary<String> validArticleTitle() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(5)
                .ofMaxLength(50)
                .map(s -> "Article Title: " + s);
    }

    /**
     * 生成有效的文章内容
     */
    @Provide
    Arbitrary<String> validArticleContent() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(20)
                .ofMaxLength(200)
                .map(s -> "Article content: " + s);
    }

    /**
     * 生成有效的公告内容
     */
    @Provide
    Arbitrary<String> validAnnouncementContent() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(10)
                .ofMaxLength(100)
                .map(s -> "Announcement: " + s);
    }
}
