package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.*;
import com.blog.service.AdminService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AdminController单元测试
 */
@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    private Article testArticle;
    private User testUser;
    private BloggerApplication testApplication;
    private CarouselConfig testCarouselConfig;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        // 初始化测试文章
        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setTitle("Test Article");
        testArticle.setContent("Test Content");
        testArticle.setUserId(1L);
        testArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        testArticle.setCreatedAt(LocalDateTime.now());

        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setNickname("Test User");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);

        // 初始化测试博主申请
        testApplication = new BloggerApplication();
        testApplication.setId(1L);
        testApplication.setUserId(1L);
        testApplication.setNickname("Test Blogger");
        testApplication.setBio("Test Bio");
        testApplication.setStatus(BloggerApplication.ApplicationStatus.PENDING);
        testApplication.setCreatedAt(LocalDateTime.now());

        // 初始化测试轮播图配置
        testCarouselConfig = new CarouselConfig();
        testCarouselConfig.setId(1L);
        testCarouselConfig.setArticleId(1L);
        testCarouselConfig.setDisplayOrder(1);
        testCarouselConfig.setCreatedAt(LocalDateTime.now());

        // 初始化测试钱包
        testWallet = new Wallet();
        testWallet.setId(1L);
        testWallet.setUserId(1L);
        testWallet.setBalance(new BigDecimal("1000.00"));
        testWallet.setTotalIncome(new BigDecimal("5000.00"));
        testWallet.setTotalWithdraw(new BigDecimal("4000.00"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPendingArticles() throws Exception {
        // 准备测试数据
        Page<Article> articlePage = new PageImpl<>(Arrays.asList(testArticle));
        when(adminService.getPendingArticles(any(PageRequest.class))).thenReturn(articlePage);

        // 执行请求并验证
        mockMvc.perform(get("/api/admin/articles/pending")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].title").value("Test Article"));

        verify(adminService).getPendingArticles(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetPendingArticles_Forbidden() throws Exception {
        // 非管理员用户应该被拒绝访问
        mockMvc.perform(get("/api/admin/articles/pending"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReviewArticle_Approved() throws Exception {
        // 准备测试数据
        testArticle.setReviewStatus(Article.ReviewStatus.APPROVED);
        testArticle.setReviewComment("Good article");
        when(adminService.reviewArticle(eq(1L), eq(true), anyString())).thenReturn(testArticle);

        ReviewArticleRequest request = new ReviewArticleRequest();
        request.setApproved(true);
        request.setReviewComment("Good article");

        // 执行请求并验证
        mockMvc.perform(put("/api/admin/articles/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.reviewStatus").value("APPROVED"));

        verify(adminService).reviewArticle(1L, true, "Good article");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReviewArticle_Rejected() throws Exception {
        // 准备测试数据
        testArticle.setReviewStatus(Article.ReviewStatus.REJECTED);
        testArticle.setReviewComment("Inappropriate content");
        when(adminService.reviewArticle(eq(1L), eq(false), anyString())).thenReturn(testArticle);

        ReviewArticleRequest request = new ReviewArticleRequest();
        request.setApproved(false);
        request.setReviewComment("Inappropriate content");

        // 执行请求并验证
        mockMvc.perform(put("/api/admin/articles/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reviewStatus").value("REJECTED"));

        verify(adminService).reviewArticle(1L, false, "Inappropriate content");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUsers_All() throws Exception {
        // 准备测试数据
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        when(adminService.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

        // 执行请求并验证
        mockMvc.perform(get("/api/admin/users")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].nickname").value("Test User"));

        verify(adminService).getAllUsers(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetUsers_WithKeyword() throws Exception {
        // 准备测试数据
        Page<User> userPage = new PageImpl<>(Arrays.asList(testUser));
        when(adminService.searchUsers(eq("test"), any(PageRequest.class))).thenReturn(userPage);

        // 执行请求并验证
        mockMvc.perform(get("/api/admin/users")
                        .param("keyword", "test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1));

        verify(adminService).searchUsers(eq("test"), any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserStatus_Disable() throws Exception {
        // 准备测试数据
        testUser.setStatus(User.UserStatus.DISABLED);
        when(adminService.updateUserStatus(eq(1L), eq(User.UserStatus.DISABLED))).thenReturn(testUser);

        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(User.UserStatus.DISABLED);

        // 执行请求并验证
        mockMvc.perform(put("/api/admin/users/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DISABLED"));

        verify(adminService).updateUserStatus(1L, User.UserStatus.DISABLED);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUserStatus_Enable() throws Exception {
        // 准备测试数据
        when(adminService.updateUserStatus(eq(1L), eq(User.UserStatus.ACTIVE))).thenReturn(testUser);

        UpdateUserStatusRequest request = new UpdateUserStatusRequest();
        request.setStatus(User.UserStatus.ACTIVE);

        // 执行请求并验证
        mockMvc.perform(put("/api/admin/users/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));

        verify(adminService).updateUserStatus(1L, User.UserStatus.ACTIVE);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCarouselConfigs() throws Exception {
        // 准备测试数据
        List<CarouselConfig> configs = Arrays.asList(testCarouselConfig);
        when(adminService.getCarouselConfigs()).thenReturn(configs);

        // 执行请求并验证
        mockMvc.perform(get("/api/admin/carousel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].articleId").value(1))
                .andExpect(jsonPath("$.data[0].displayOrder").value(1));

        verify(adminService).getCarouselConfigs();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddToCarousel() throws Exception {
        // 准备测试数据
        when(adminService.addToCarousel(eq(1L), eq(1))).thenReturn(testCarouselConfig);

        CarouselConfigRequest request = new CarouselConfigRequest();
        request.setArticleId(1L);
        request.setDisplayOrder(1);

        // 执行请求并验证
        mockMvc.perform(post("/api/admin/carousel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.articleId").value(1))
                .andExpect(jsonPath("$.data.displayOrder").value(1));

        verify(adminService).addToCarousel(1L, 1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateCarousel() throws Exception {
        // 准备测试数据
        testCarouselConfig.setDisplayOrder(2);
        when(adminService.updateCarouselOrder(eq(1L), eq(2))).thenReturn(testCarouselConfig);

        CarouselConfigRequest request = new CarouselConfigRequest();
        request.setArticleId(1L);
        request.setDisplayOrder(2);

        // 执行请求并验证
        mockMvc.perform(put("/api/admin/carousel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayOrder").value(2));

        verify(adminService).updateCarouselOrder(1L, 2);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRemoveFromCarousel() throws Exception {
        // 执行请求并验证
        mockMvc.perform(delete("/api/admin/carousel/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(adminService).removeFromCarousel(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBloggerApplications_All() throws Exception {
        // 准备测试数据
        Page<BloggerApplication> applicationPage = new PageImpl<>(Arrays.asList(testApplication));
        when(adminService.getAllApplications(any(PageRequest.class))).thenReturn(applicationPage);

        // 执行请求并验证
        mockMvc.perform(get("/api/admin/blogger-applications")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].nickname").value("Test Blogger"));

        verify(adminService).getAllApplications(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetBloggerApplications_Pending() throws Exception {
        // 准备测试数据
        Page<BloggerApplication> applicationPage = new PageImpl<>(Arrays.asList(testApplication));
        when(adminService.getPendingApplications(any(PageRequest.class))).thenReturn(applicationPage);

        // 执行请求并验证
        mockMvc.perform(get("/api/admin/blogger-applications")
                        .param("status", "pending")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("PENDING"));

        verify(adminService).getPendingApplications(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReviewBloggerApplication_Approved() throws Exception {
        // 准备测试数据
        testApplication.setStatus(BloggerApplication.ApplicationStatus.APPROVED);
        testApplication.setReviewComment("Approved");
        when(adminService.reviewBloggerApplication(eq(1L), eq(true), anyString()))
                .thenReturn(testApplication);

        ReviewBloggerApplicationRequest request = new ReviewBloggerApplicationRequest();
        request.setApproved(true);
        request.setReviewComment("Approved");

        // 执行请求并验证
        mockMvc.perform(put("/api/admin/blogger-applications/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        verify(adminService).reviewBloggerApplication(1L, true, "Approved");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReviewBloggerApplication_Rejected() throws Exception {
        // 准备测试数据
        testApplication.setStatus(BloggerApplication.ApplicationStatus.REJECTED);
        testApplication.setReviewComment("Not qualified");
        when(adminService.reviewBloggerApplication(eq(1L), eq(false), anyString()))
                .thenReturn(testApplication);

        ReviewBloggerApplicationRequest request = new ReviewBloggerApplicationRequest();
        request.setApproved(false);
        request.setReviewComment("Not qualified");

        // 执行请求并验证
        mockMvc.perform(put("/api/admin/blogger-applications/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("REJECTED"));

        verify(adminService).reviewBloggerApplication(1L, false, "Not qualified");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetPlatformWallet() throws Exception {
        // 准备测试数据
        when(adminService.getPlatformWallet()).thenReturn(testWallet);

        // 执行请求并验证
        mockMvc.perform(get("/api/admin/wallet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(1000.00))
                .andExpect(jsonPath("$.data.totalIncome").value(5000.00));

        verify(adminService).getPlatformWallet();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPlatformWithdraw() throws Exception {
        // 准备测试数据
        testWallet.setBalance(new BigDecimal("500.00"));
        when(adminService.platformWithdraw(any(BigDecimal.class))).thenReturn(testWallet);

        WithdrawRequest request = new WithdrawRequest();
        request.setAmount(new BigDecimal("500.00"));

        // 执行请求并验证
        mockMvc.perform(post("/api/admin/wallet/withdraw")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.balance").value(500.00));

        verify(adminService).platformWithdraw(any(BigDecimal.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testReviewArticle_InvalidRequest() throws Exception {
        // 测试无效请求（缺少必填字段）
        ReviewArticleRequest request = new ReviewArticleRequest();
        // approved字段为null

        mockMvc.perform(put("/api/admin/articles/1/review")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAddToCarousel_InvalidOrder() throws Exception {
        // 测试无效的显示顺序（小于1）
        CarouselConfigRequest request = new CarouselConfigRequest();
        request.setArticleId(1L);
        request.setDisplayOrder(0);

        mockMvc.perform(post("/api/admin/carousel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
