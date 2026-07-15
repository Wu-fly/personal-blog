package com.blog.service;

import com.blog.entity.Article;
import com.blog.entity.BloggerApplication;
import com.blog.entity.CarouselConfig;
import com.blog.entity.User;
import com.blog.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理员服务接口
 * 提供文章审核、用户管理、轮播图管理、博主申请审核、平台钱包管理等功能
 */
public interface AdminService {

    /**
     * 审核文章
     * 
     * @param articleId 文章ID
     * @param approved 是否通过审核
     * @param reviewComment 审核意见
     * @return 审核后的文章
     */
    Article reviewArticle(Long articleId, boolean approved, String reviewComment);

    /**
     * 获取待审核文章列表
     * 
     * @param pageable 分页参数
     * @return 待审核文章分页列表
     */
    Page<Article> getPendingArticles(Pageable pageable);

    /**
     * 审核博主申请
     * 
     * @param applicationId 申请ID
     * @param approved 是否通过审核
     * @param reviewComment 审核意见
     * @return 审核后的申请
     */
    BloggerApplication reviewBloggerApplication(Long applicationId, boolean approved, String reviewComment);

    /**
     * 获取待审核博主申请列表
     * 
     * @param pageable 分页参数
     * @return 待审核申请分页列表
     */
    Page<com.blog.dto.BloggerApplicationDTO> getPendingApplications(Pageable pageable);

    /**
     * 获取所有博主申请列表
     * 
     * @param pageable 分页参数
     * @return 所有申请分页列表
     */
    Page<com.blog.dto.BloggerApplicationDTO> getAllApplications(Pageable pageable);

    /**
     * 根据状态获取博主申请列表
     * 
     * @param status 申请状态
     * @param pageable 分页参数
     * @return 申请分页列表
     */
    Page<com.blog.dto.BloggerApplicationDTO> getApplicationsByStatus(String status, Pageable pageable);

    /**
     * 更新用户状态（禁用/启用）
     * 
     * @param userId 用户ID
     * @param status 用户状态
     * @return 更新后的用户
     */
    User updateUserStatus(Long userId, User.UserStatus status);

    /**
     * 获取所有用户列表
     * 
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> getAllUsers(Pageable pageable);

    /**
     * 搜索用户
     * 
     * @param keyword 搜索关键词（手机号、邮箱、昵称）
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    Page<User> searchUsers(String keyword, Pageable pageable);

    /**
     * 获取轮播图配置列表
     * 
     * @return 轮播图配置列表（按显示顺序排序）
     */
    List<CarouselConfig> getCarouselConfigs();

    /**
     * 添加文章到轮播图
     * 
     * @param articleId 文章ID
     * @param displayOrder 显示顺序
     * @return 轮播图配置
     */
    CarouselConfig addToCarousel(Long articleId, Integer displayOrder);

    /**
     * 从轮播图移除文章
     * 
     * @param articleId 文章ID
     */
    void removeFromCarousel(Long articleId);

    /**
     * 更新轮播图顺序
     * 
     * @param articleId 文章ID
     * @param newOrder 新的显示顺序
     * @return 更新后的轮播图配置
     */
    CarouselConfig updateCarouselOrder(Long articleId, Integer newOrder);

    /**
     * 批量更新轮播图配置
     * 
     * @param requests 轮播图配置请求列表
     * @return 更新后的轮播图配置列表
     */
    List<CarouselConfig> batchUpdateCarousel(List<com.blog.dto.CarouselConfigRequest> requests);

    /**
     * 获取平台钱包（管理员钱包）
     * 
     * @return 平台钱包
     */
    Wallet getPlatformWallet();

    /**
     * 平台钱包提现
     * 
     * @param amount 提现金额
     * @return 提现后的钱包
     */
    Wallet platformWithdraw(BigDecimal amount);

    /**
     * 获取平台统计数据
     * 
     * @return 统计数据
     */
    com.blog.dto.StatisticsResponse getStatistics();

    /**
     * 获取图表数据
     * 
     * @return 图表数据
     */
    com.blog.dto.ChartDataResponse getChartData();
    
    /**
     * 获取平台收益明细
     * 
     * @param pageable 分页参数
     * @return 收益明细分页列表
     */
    Page<com.blog.dto.TransactionDTO> getPlatformRevenue(Pageable pageable);
}
