package com.blog.service;

import com.blog.entity.*;
import com.blog.exception.BusinessException;
import com.blog.repository.*;
import com.blog.service.impl.AdminServiceImpl;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AdminService单元测试
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private BloggerApplicationRepository bloggerApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarouselConfigRepository carouselConfigRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AdminServiceImpl adminService;

    private Article testArticle;
    private User testUser;
    private BloggerApplication testApplication;
    private CarouselConfig testCarouselConfig;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setNickname("测试用户");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);

        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setUserId(1L);
        testArticle.setTitle("测试文章");
        testArticle.setContent("测试内容");
        testArticle.setReviewStatus(Article.ReviewStatus.PENDING);

        testApplication = new BloggerApplication();
        testApplication.setId(1L);
        testApplication.setUserId(1L);
        testApplication.setNickname("测试博主");
        testApplication.setBio("测试简介");
        testApplication.setStatus(BloggerApplication.ApplicationStatus.PENDING);

        testCarouselConfig = new CarouselConfig();
        testCarouselConfig.setId(1L);
        testCarouselConfig.setArticleId(1L);
        testCarouselConfig.setDisplayOrder(1);

        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setUserId(1L);
        testWallet.setBalance(new BigDecimal("1000.00"));
        testWallet.setTotalIncome(new BigDecimal("1000.00"));
        testWallet.setTotalWithdraw(BigDecimal.ZERO);
    }

    @Test
    void testReviewArticle_Approved() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);

        // When
        Article result = adminService.reviewArticle(1L, true, "审核通过");

        // Then
        assertEquals(Article.ReviewStatus.APPROVED, result.getReviewStatus());
        assertEquals("审核通过", result.getReviewComment());
        assertNotNull(result.getPublishedAt());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void testReviewArticle_Rejected() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(articleRepository.save(any(Article.class))).thenReturn(testArticle);

        // When
        Article result = adminService.reviewArticle(1L, false, "内容不符合规范");

        // Then
        assertEquals(Article.ReviewStatus.REJECTED, result.getReviewStatus());
        assertEquals("内容不符合规范", result.getReviewComment());
        assertNull(result.getPublishedAt());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void testReviewArticle_ArticleNotFound() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.reviewArticle(1L, true, "审核通过"));
        assertEquals("ARTICLE_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void testReviewArticle_NotPending() {
        // Given
        testArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.reviewArticle(1L, true, "审核通过"));
        assertEquals("ARTICLE_NOT_PENDING", exception.getErrorCode());
    }

    @Test
    void testGetPendingArticles() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(Arrays.asList(testArticle));
        when(articleRepository.findByReviewStatus(Article.ReviewStatus.PENDING, pageable))
            .thenReturn(expectedPage);

        // When
        Page<Article> result = adminService.getPendingArticles(pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(testArticle, result.getContent().get(0));
    }

    @Test
    void testReviewBloggerApplication_Approved() {
        // Given
        when(bloggerApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.existsByUserId(1L)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(bloggerApplicationRepository.save(any(BloggerApplication.class))).thenReturn(testApplication);

        // When
        BloggerApplication result = adminService.reviewBloggerApplication(1L, true, "审核通过");

        // Then
        assertEquals(BloggerApplication.ApplicationStatus.APPROVED, result.getStatus());
        assertEquals("审核通过", result.getReviewComment());
        assertNotNull(result.getReviewedAt());
        verify(userRepository).save(argThat(user -> user.getRole() == User.UserRole.BLOGGER));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testReviewBloggerApplication_Rejected() {
        // Given
        when(bloggerApplicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bloggerApplicationRepository.save(any(BloggerApplication.class))).thenReturn(testApplication);

        // When
        BloggerApplication result = adminService.reviewBloggerApplication(1L, false, "资料不完整");

        // Then
        assertEquals(BloggerApplication.ApplicationStatus.REJECTED, result.getStatus());
        assertEquals("资料不完整", result.getReviewComment());
        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testUpdateUserStatus_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = adminService.updateUserStatus(1L, User.UserStatus.DISABLED);

        // Then
        assertEquals(User.UserStatus.DISABLED, result.getStatus());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testUpdateUserStatus_CannotDisableAdmin() {
        // Given
        testUser.setRole(User.UserRole.ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.updateUserStatus(1L, User.UserStatus.DISABLED));
        assertEquals("CANNOT_DISABLE_ADMIN", exception.getErrorCode());
    }

    @Test
    void testGetAllUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findAll(pageable)).thenReturn(expectedPage);

        // When
        Page<User> result = adminService.getAllUsers(pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser, result.getContent().get(0));
    }

    @Test
    void testSearchUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.searchByKeyword("test", pageable)).thenReturn(expectedPage);

        // When
        Page<User> result = adminService.searchUsers("test", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        verify(userRepository).searchByKeyword("test", pageable);
    }

    @Test
    void testGetCarouselConfigs() {
        // Given
        List<CarouselConfig> configs = Arrays.asList(testCarouselConfig);
        when(carouselConfigRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(configs);

        // When
        List<CarouselConfig> result = adminService.getCarouselConfigs();

        // Then
        assertEquals(1, result.size());
        assertEquals(testCarouselConfig, result.get(0));
    }

    @Test
    void testAddToCarousel_Success() {
        // Given
        testArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(carouselConfigRepository.existsByArticleId(1L)).thenReturn(false);
        when(carouselConfigRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(Arrays.asList());
        when(carouselConfigRepository.save(any(CarouselConfig.class))).thenReturn(testCarouselConfig);

        // When
        CarouselConfig result = adminService.addToCarousel(1L, 1);

        // Then
        assertNotNull(result);
        verify(carouselConfigRepository).save(any(CarouselConfig.class));
    }

    @Test
    void testAddToCarousel_ArticleNotApproved() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.addToCarousel(1L, 1));
        assertEquals("ARTICLE_NOT_APPROVED", exception.getErrorCode());
    }

    @Test
    void testAddToCarousel_AlreadyInCarousel() {
        // Given
        testArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(carouselConfigRepository.existsByArticleId(1L)).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.addToCarousel(1L, 1));
        assertEquals("ARTICLE_ALREADY_IN_CAROUSEL", exception.getErrorCode());
    }

    @Test
    void testAddToCarousel_CarouselFull() {
        // Given
        testArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle));
        when(carouselConfigRepository.existsByArticleId(1L)).thenReturn(false);
        
        // 创建5个轮播图配置
        List<CarouselConfig> configs = Arrays.asList(
            new CarouselConfig(), new CarouselConfig(), new CarouselConfig(),
            new CarouselConfig(), new CarouselConfig()
        );
        when(carouselConfigRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(configs);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.addToCarousel(1L, 1));
        assertEquals("CAROUSEL_FULL", exception.getErrorCode());
    }

    @Test
    void testRemoveFromCarousel_Success() {
        // Given
        when(carouselConfigRepository.existsByArticleId(1L)).thenReturn(true);

        // When
        adminService.removeFromCarousel(1L);

        // Then
        verify(carouselConfigRepository).deleteByArticleId(1L);
    }

    @Test
    void testRemoveFromCarousel_NotInCarousel() {
        // Given
        when(carouselConfigRepository.existsByArticleId(1L)).thenReturn(false);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.removeFromCarousel(1L));
        assertEquals("ARTICLE_NOT_IN_CAROUSEL", exception.getErrorCode());
    }

    @Test
    void testUpdateCarouselOrder_Success() {
        // Given
        when(carouselConfigRepository.findByArticleId(1L)).thenReturn(Optional.of(testCarouselConfig));
        when(carouselConfigRepository.save(any(CarouselConfig.class))).thenReturn(testCarouselConfig);

        // When
        CarouselConfig result = adminService.updateCarouselOrder(1L, 2);

        // Then
        assertEquals(2, result.getDisplayOrder());
        verify(carouselConfigRepository).save(any(CarouselConfig.class));
    }

    @Test
    void testGetPlatformWallet_Exists() {
        // Given
        User admin = new User();
        admin.setId(1L);
        admin.setRole(User.UserRole.ADMIN);
        when(userRepository.findByRole(User.UserRole.ADMIN)).thenReturn(Optional.of(admin));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));

        // When
        Wallet result = adminService.getPlatformWallet();

        // Then
        assertNotNull(result);
        assertEquals(testWallet, result);
    }

    @Test
    void testGetPlatformWallet_CreateIfNotExists() {
        // Given
        User admin = new User();
        admin.setId(1L);
        admin.setRole(User.UserRole.ADMIN);
        when(userRepository.findByRole(User.UserRole.ADMIN)).thenReturn(Optional.of(admin));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);

        // When
        Wallet result = adminService.getPlatformWallet();

        // Then
        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testPlatformWithdraw_Success() {
        // Given
        User admin = new User();
        admin.setId(1L);
        admin.setRole(User.UserRole.ADMIN);
        when(userRepository.findByRole(User.UserRole.ADMIN)).thenReturn(Optional.of(admin));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        Wallet result = adminService.platformWithdraw(new BigDecimal("100.00"));

        // Then
        assertNotNull(result);
        verify(walletRepository).save(any(Wallet.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testPlatformWithdraw_InvalidAmount() {
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.platformWithdraw(BigDecimal.ZERO));
        assertEquals("INVALID_AMOUNT", exception.getErrorCode());
    }

    @Test
    void testPlatformWithdraw_InsufficientBalance() {
        // Given
        User admin = new User();
        admin.setId(1L);
        admin.setRole(User.UserRole.ADMIN);
        when(userRepository.findByRole(User.UserRole.ADMIN)).thenReturn(Optional.of(admin));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(testWallet));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> adminService.platformWithdraw(new BigDecimal("2000.00")));
        assertEquals("INSUFFICIENT_BALANCE", exception.getErrorCode());
    }
}
