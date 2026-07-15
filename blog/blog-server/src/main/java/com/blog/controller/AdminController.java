package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.Article;
import com.blog.entity.BloggerApplication;
import com.blog.entity.CarouselConfig;
import com.blog.entity.User;
import com.blog.entity.Wallet;
import com.blog.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员控制器
 * 提供文章审核、用户管理、轮播图管理、博主申请审核、平台钱包管理等功能
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取待审核文章列表
     * GET /admin/articles/pending
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 待审核文章分页列表
     */
    @GetMapping("/articles/pending")
    public ResponseEntity<ApiResponse<Page<Article>>> getPendingArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Article> articles = adminService.getPendingArticles(pageable);
        return ResponseEntity.ok(ApiResponse.success(articles));
    }

    /**
     * 审核文章
     * PUT /admin/articles/{id}/review
     *
     * @param id 文章ID
     * @param request 审核请求
     * @return 审核后的文章
     */
    @PutMapping("/articles/{id}/review")
    public ResponseEntity<ApiResponse<ArticleResponse>> reviewArticle(
            @PathVariable Long id,
            @Valid @RequestBody ReviewArticleRequest request) {
        Article article = adminService.reviewArticle(
                id,
                request.getApproved(),
                request.getReviewComment()
        );
        ArticleResponse response = ArticleResponse.fromEntity(article);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取用户列表
     * GET /admin/users
     *
     * @param keyword 搜索关键词（可选）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 用户分页列表
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<User>>> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = adminService.searchUsers(keyword, pageable);
        } else {
            users = adminService.getAllUsers(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * 更新用户状态
     * PUT /admin/users/{id}/status
     *
     * @param id 用户ID
     * @param request 更新状态请求
     * @return 更新后的用户
     */
    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<User>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        User user = adminService.updateUserStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 获取轮播图配置
     * GET /admin/carousel
     *
     * @return 轮播图配置列表
     */
    @GetMapping("/carousel")
    public ResponseEntity<ApiResponse<List<CarouselConfig>>> getCarouselConfigs() {
        List<CarouselConfig> configs = adminService.getCarouselConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    /**
     * 添加文章到轮播图
     * POST /admin/carousel
     *
     * @param request 轮播图配置请求
     * @return 轮播图配置
     */
    @PostMapping("/carousel")
    public ResponseEntity<ApiResponse<CarouselConfig>> addToCarousel(
            @Valid @RequestBody CarouselConfigRequest request) {
        CarouselConfig config = adminService.addToCarousel(
                request.getArticleId(),
                request.getDisplayOrder()
        );
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    /**
     * 批量更新轮播图配置
     * PUT /admin/carousel
     *
     * @param requests 轮播图配置请求列表
     * @return 更新后的轮播图配置列表
     */
    @PutMapping("/carousel")
    public ResponseEntity<ApiResponse<List<CarouselConfig>>> updateCarousel(
            @Valid @RequestBody List<CarouselConfigRequest> requests) {
        List<CarouselConfig> configs = adminService.batchUpdateCarousel(requests);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    /**
     * 从轮播图移除文章
     * DELETE /admin/carousel/{articleId}
     *
     * @param articleId 文章ID
     * @return 成功响应
     */
    @DeleteMapping("/carousel/{articleId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCarousel(@PathVariable Long articleId) {
        adminService.removeFromCarousel(articleId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 获取博主申请列表
     * GET /admin/blogger-applications
     *
     * @param status 申请状态（可选：pending, approved, rejected）
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 博主申请分页列表
     */
    @GetMapping("/blogger-applications")
    public ResponseEntity<ApiResponse<Page<com.blog.dto.BloggerApplicationDTO>>> getBloggerApplications(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.blog.dto.BloggerApplicationDTO> applications;
        
        if (status != null && "PENDING".equalsIgnoreCase(status)) {
            applications = adminService.getPendingApplications(pageable);
        } else if (status != null) {
            // 如果指定了其他状态，按状态过滤
            applications = adminService.getApplicationsByStatus(status, pageable);
        } else {
            applications = adminService.getAllApplications(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    /**
     * 审核博主申请
     * PUT /admin/blogger-applications/{id}
     *
     * @param id 申请ID
     * @param request 审核请求
     * @return 审核结果
     */
    @PutMapping("/blogger-applications/{id}")
    public ResponseEntity<ApiResponse<String>> reviewBloggerApplication(
            @PathVariable Long id,
            @Valid @RequestBody ReviewBloggerApplicationRequest request) {
        adminService.reviewBloggerApplication(
                id,
                request.getApproved(),
                request.getReviewComment()
        );
        String message = request.getApproved() ? "博主申请已通过" : "博主申请已拒绝";
        return ResponseEntity.ok(ApiResponse.success(message, "SUCCESS"));
    }

    /**
     * 获取平台钱包信息
     * GET /admin/wallet
     *
     * @return 平台钱包信息
     */
    @GetMapping("/wallet")
    public ResponseEntity<ApiResponse<Wallet>> getPlatformWallet() {
        Wallet wallet = adminService.getPlatformWallet();
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    /**
     * 平台钱包提现
     * POST /admin/wallet/withdraw
     *
     * @param request 提现请求
     * @return 提现后的钱包信息
     */
    @PostMapping("/wallet/withdraw")
    public ResponseEntity<ApiResponse<Wallet>> platformWithdraw(
            @Valid @RequestBody WithdrawRequest request) {
        Wallet wallet = adminService.platformWithdraw(request.getAmount());
        return ResponseEntity.ok(ApiResponse.success(wallet));
    }

    /**
     * 获取平台统计数据
     * GET /admin/statistics
     *
     * @return 平台统计数据
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<com.blog.dto.StatisticsResponse>> getStatistics() {
        com.blog.dto.StatisticsResponse statistics = adminService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    /**
     * 获取图表数据
     * GET /admin/charts
     *
     * @return 图表数据
     */
    @GetMapping("/charts")
    public ResponseEntity<ApiResponse<com.blog.dto.ChartDataResponse>> getChartData() {
        com.blog.dto.ChartDataResponse chartData = adminService.getChartData();
        return ResponseEntity.ok(ApiResponse.success(chartData));
    }
    
    /**
     * 获取平台收益明细
     * GET /admin/wallet/revenue
     *
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 收益明细分页列表
     */
    @GetMapping("/wallet/revenue")
    public ResponseEntity<ApiResponse<Page<com.blog.dto.TransactionDTO>>> getPlatformRevenue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.blog.dto.TransactionDTO> revenue = adminService.getPlatformRevenue(pageable);
        return ResponseEntity.ok(ApiResponse.success(revenue));
    }
}
