package com.blog.service.impl;

import com.blog.config.CacheConfig;
import com.blog.dto.CarouselConfigRequest;
import com.blog.entity.*;
import com.blog.exception.BusinessException;
import com.blog.repository.*;
import com.blog.service.AdminService;
import com.blog.service.CacheService;
import com.blog.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理员服务实现类
 * 提供文章审核、用户管理、轮播图管理、博主申请审核、平台钱包管理等功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final ArticleRepository articleRepository;
    private final BloggerApplicationRepository bloggerApplicationRepository;
    private final UserRepository userRepository;
    private final CarouselConfigRepository carouselConfigRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final TransactionRepository transactionRepository;
    private final CacheService cacheService;

    /**
     * 审核文章
     * 需求: 33.1-33.6
     * 权限: 只有管理员可以审核文章
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public Article reviewArticle(Long articleId, boolean approved, String reviewComment) {
        log.info("Reviewing article: articleId={}, approved={}", articleId, approved);
        
        // 查找文章
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 检查文章是否待审核
        if (article.getReviewStatus() != Article.ReviewStatus.PENDING) {
            throw new BusinessException("ARTICLE_NOT_PENDING", "文章不是待审核状态");
        }
        
        // 更新审核状态
        if (approved) {
            article.setReviewStatus(Article.ReviewStatus.APPROVED);
            article.setPublishedAt(LocalDateTime.now());
            log.info("Article approved: articleId={}", articleId);
        } else {
            article.setReviewStatus(Article.ReviewStatus.REJECTED);
            log.info("Article rejected: articleId={}", articleId);
        }
        
        article.setReviewComment(reviewComment);
        
        // 保存文章
        Article savedArticle = articleRepository.save(article);
        
        // 强制加载关联实体，避免LazyInitializationException
        if (savedArticle.getUser() != null) {
            savedArticle.getUser().getNickname(); // 触发User加载
        }
        if (savedArticle.getCategory() != null) {
            savedArticle.getCategory().getName(); // 触发Category加载
        }
        
        // Evict caches after article review
        cacheService.evictArticleDetailCache(articleId);
        cacheService.evictHotArticlesCache();
        
        // TODO: 通知博主审核结果（可以通过消息系统或邮件）
        log.info("Article review completed: articleId={}, status={}", articleId, savedArticle.getReviewStatus());
        
        return savedArticle;
    }

    /**
     * 获取待审核文章列表
     * 需求: 33.1
     * 权限: 只有管理员可以查看待审核文章
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<Article> getPendingArticles(Pageable pageable) {
        log.info("Getting pending articles: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Article> articles = articleRepository.findByReviewStatus(Article.ReviewStatus.PENDING, pageable);
        // Force load user relationship to avoid LazyInitializationException
        articles.forEach(article -> {
            if (article.getUser() != null) {
                article.getUser().getNickname(); // Trigger lazy loading
            }
        });
        return articles;
    }

    /**
     * 审核博主申请
     * 需求: 35.1-35.5
     * 权限: 只有管理员可以审核博主申请
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public BloggerApplication reviewBloggerApplication(Long applicationId, boolean approved, String reviewComment) {
        log.info("Reviewing blogger application: applicationId={}, approved={}", applicationId, approved);
        
        // 查找申请
        BloggerApplication application = bloggerApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException("APPLICATION_NOT_FOUND", "申请不存在"));
        
        // 检查申请是否待审核
        if (application.getStatus() != BloggerApplication.ApplicationStatus.PENDING) {
            throw new BusinessException("APPLICATION_NOT_PENDING", "申请不是待审核状态");
        }
        
        // 查找用户
        User user = userRepository.findById(application.getUserId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 更新申请状态
        if (approved) {
            application.setStatus(BloggerApplication.ApplicationStatus.APPROVED);
            
            // 更新用户角色为博主
            user.setRole(User.UserRole.BLOGGER);
            user.setNickname(application.getNickname());
            user.setBio(application.getBio());
            userRepository.save(user);
            
            // 为博主创建钱包（如果不存在）
            if (!walletRepository.existsByUserId(user.getId())) {
                Wallet wallet = new Wallet();
                wallet.setUserId(user.getId());
                wallet.setBalance(BigDecimal.ZERO);
                wallet.setTotalIncome(BigDecimal.ZERO);
                wallet.setTotalWithdraw(BigDecimal.ZERO);
                walletRepository.save(wallet);
                log.info("Created wallet for new blogger: userId={}", user.getId());
            }
            
            log.info("Blogger application approved: applicationId={}, userId={}", applicationId, user.getId());
        } else {
            application.setStatus(BloggerApplication.ApplicationStatus.REJECTED);
            log.info("Blogger application rejected: applicationId={}", applicationId);
        }
        
        application.setReviewComment(reviewComment);
        application.setReviewedAt(LocalDateTime.now());
        
        // 保存申请
        BloggerApplication savedApplication = bloggerApplicationRepository.save(application);
        
        // TODO: 通知申请人审核结果
        log.info("Blogger application review completed: applicationId={}, status={}", 
                applicationId, savedApplication.getStatus());
        
        return savedApplication;
    }

    /**
     * 获取待审核博主申请列表
     * 需求: 35.1
     * 权限: 只有管理员可以查看博主申请
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<com.blog.dto.BloggerApplicationDTO> getPendingApplications(Pageable pageable) {
        log.info("Getting pending blogger applications: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<BloggerApplication> applications = bloggerApplicationRepository
                .findByStatusOrderByCreatedAtDesc(BloggerApplication.ApplicationStatus.PENDING, pageable);
        return applications.map(com.blog.dto.BloggerApplicationDTO::fromEntity);
    }

    /**
     * 获取所有博主申请列表
     * 需求: 35.1
     * 权限: 只有管理员可以查看博主申请
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<com.blog.dto.BloggerApplicationDTO> getAllApplications(Pageable pageable) {
        log.info("Getting all blogger applications: page={}, size={}", 
                pageable.getPageNumber(), pageable.getPageSize());
        Page<BloggerApplication> applications = bloggerApplicationRepository
                .findAllByOrderByCreatedAtDesc(pageable);
        return applications.map(com.blog.dto.BloggerApplicationDTO::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<com.blog.dto.BloggerApplicationDTO> getApplicationsByStatus(String status, Pageable pageable) {
        log.info("Getting blogger applications by status: status={}, page={}, size={}", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        try {
            BloggerApplication.ApplicationStatus applicationStatus = 
                    BloggerApplication.ApplicationStatus.valueOf(status.toUpperCase());
            Page<BloggerApplication> applications = bloggerApplicationRepository
                    .findByStatusOrderByCreatedAtDesc(applicationStatus, pageable);
            return applications.map(com.blog.dto.BloggerApplicationDTO::fromEntity);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid application status: {}", status);
            return Page.empty(pageable);
        }
    }

    /**
     * 更新用户状态（禁用/启用）
     * 需求: 37.1-37.6
     * 权限: 只有管理员可以更新用户状态
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserStatus(Long userId, User.UserStatus status) {
        log.info("Updating user status: userId={}, status={}", userId, status);
        
        // 查找用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 不允许禁用管理员账号
        if (user.getRole() == User.UserRole.ADMIN) {
            throw new BusinessException("CANNOT_DISABLE_ADMIN", "不能禁用管理员账号");
        }
        
        // 更新状态
        user.setStatus(status);
        User savedUser = userRepository.save(user);
        
        log.info("User status updated: userId={}, status={}", userId, status);
        
        return savedUser;
    }

    /**
     * 获取所有用户列表
     * 需求: 37.1
     * 权限: 只有管理员可以查看用户列表
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> getAllUsers(Pageable pageable) {
        log.info("Getting all users: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable);
    }

    /**
     * 搜索用户
     * 需求: 37.2
     * 权限: 只有管理员可以搜索用户
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        log.info("Searching users: keyword={}, page={}, size={}", 
                keyword, pageable.getPageNumber(), pageable.getPageSize());
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers(pageable);
        }
        
        return userRepository.searchByKeyword(keyword.trim(), pageable);
    }

    /**
     * 获取轮播图配置列表
     * 需求: 34.1
     * 权限: 只有管理员可以查看轮播图配置
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = CacheConfig.CAROUSEL_CACHE, key = "'all'", unless = "#result == null || #result.isEmpty()")
    public List<CarouselConfig> getCarouselConfigs() {
        log.info("Getting carousel configs");
        return carouselConfigRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * 添加文章到轮播图
     * 需求: 34.2
     * 权限: 只有管理员可以管理轮播图
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CarouselConfig addToCarousel(Long articleId, Integer displayOrder) {
        log.info("Adding article to carousel: articleId={}, displayOrder={}", articleId, displayOrder);
        
        // 检查文章是否存在
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 检查文章是否已审核通过
        if (article.getReviewStatus() != Article.ReviewStatus.APPROVED) {
            throw new BusinessException("ARTICLE_NOT_APPROVED", "只能添加已审核通过的文章到轮播图");
        }
        
        // 检查文章是否已在轮播图中
        if (carouselConfigRepository.existsByArticleId(articleId)) {
            throw new BusinessException("ARTICLE_ALREADY_IN_CAROUSEL", "文章已在轮播图中");
        }
        
        // 检查轮播图数量限制（最多5篇）
        List<CarouselConfig> existingConfigs = carouselConfigRepository.findAllByOrderByDisplayOrderAsc();
        if (existingConfigs.size() >= 5) {
            throw new BusinessException("CAROUSEL_FULL", "轮播图已满，最多只能添加5篇文章");
        }
        
        // 如果未指定顺序，添加到末尾
        if (displayOrder == null) {
            displayOrder = existingConfigs.isEmpty() ? 1 : 
                    existingConfigs.get(existingConfigs.size() - 1).getDisplayOrder() + 1;
        }
        
        // 创建轮播图配置
        CarouselConfig config = new CarouselConfig();
        config.setArticleId(articleId);
        config.setDisplayOrder(displayOrder);
        
        CarouselConfig savedConfig = carouselConfigRepository.save(config);
        
        // Evict carousel cache after adding
        cacheService.evictCarouselCache();
        
        log.info("Article added to carousel: articleId={}, displayOrder={}", articleId, displayOrder);
        
        return savedConfig;
    }

    /**
     * 从轮播图移除文章
     * 需求: 34.3
     * 权限: 只有管理员可以管理轮播图
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void removeFromCarousel(Long articleId) {
        log.info("Removing article from carousel: articleId={}", articleId);
        
        // 检查文章是否在轮播图中
        if (!carouselConfigRepository.existsByArticleId(articleId)) {
            throw new BusinessException("ARTICLE_NOT_IN_CAROUSEL", "文章不在轮播图中");
        }
        
        carouselConfigRepository.deleteByArticleId(articleId);
        
        // Evict carousel cache after removing
        cacheService.evictCarouselCache();
        
        log.info("Article removed from carousel: articleId={}", articleId);
    }

    /**
     * 更新轮播图顺序
     * 需求: 34.4
     * 权限: 只有管理员可以管理轮播图
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CarouselConfig updateCarouselOrder(Long articleId, Integer newOrder) {
        log.info("Updating carousel order: articleId={}, newOrder={}", articleId, newOrder);
        
        // 查找轮播图配置
        CarouselConfig config = carouselConfigRepository.findByArticleId(articleId)
                .orElseThrow(() -> new BusinessException("ARTICLE_NOT_IN_CAROUSEL", "文章不在轮播图中"));
        
        // 更新顺序
        config.setDisplayOrder(newOrder);
        CarouselConfig savedConfig = carouselConfigRepository.save(config);
        
        // Evict carousel cache after updating order
        cacheService.evictCarouselCache();
        
        log.info("Carousel order updated: articleId={}, newOrder={}", articleId, newOrder);
        
        return savedConfig;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public List<CarouselConfig> batchUpdateCarousel(List<CarouselConfigRequest> requests) {
        log.info("Batch updating carousel config: {} items", requests.size());
        
        // 删除所有现有的轮播图配置
        carouselConfigRepository.deleteAll();
        carouselConfigRepository.flush(); // 强制刷新删除操作到数据库
        
        // 创建新的轮播图配置
        List<CarouselConfig> configs = new ArrayList<>();
        for (CarouselConfigRequest request : requests) {
            // 验证文章是否存在
            Article article = articleRepository.findById(request.getArticleId())
                    .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
            
            CarouselConfig config = new CarouselConfig();
            config.setArticleId(request.getArticleId()); // 直接设置articleId而不是article对象
            config.setDisplayOrder(request.getDisplayOrder());
            configs.add(config);
        }
        
        // 批量保存
        List<CarouselConfig> savedConfigs = carouselConfigRepository.saveAll(configs);
        
        // 清除缓存
        cacheService.evictCarouselCache();
        
        log.info("Batch carousel update completed: {} items saved", savedConfigs.size());
        
        return savedConfigs;
    }

    /**
     * 获取平台钱包（管理员钱包）
     * 需求: 36.1
     */
    @Override
    @Transactional(readOnly = true)
    public Wallet getPlatformWallet() {
        log.info("Getting platform wallet");
        
        // 查找管理员用户 - 使用email查找特定的管理员账号
        User admin = userRepository.findByEmail("admin@blog.com")
                .orElseThrow(() -> new BusinessException("ADMIN_NOT_FOUND", "管理员账号不存在"));
        
        // 查找管理员钱包
        Wallet wallet = walletRepository.findByUserId(admin.getId())
                .orElseGet(() -> {
                    // 如果管理员钱包不存在，创建一个
                    Wallet newWallet = new Wallet();
                    newWallet.setUserId(admin.getId());
                    newWallet.setBalance(BigDecimal.ZERO);
                    newWallet.setTotalIncome(BigDecimal.ZERO);
                    newWallet.setTotalWithdraw(BigDecimal.ZERO);
                    Wallet saved = walletRepository.save(newWallet);
                    log.info("Created platform wallet: userId={}", admin.getId());
                    return saved;
                });
        
        return wallet;
    }

    /**
     * 平台钱包提现
     * 需求: 36.3-36.6
     */
    @Override
    @Transactional
    public Wallet platformWithdraw(BigDecimal amount) {
        log.info("Platform wallet withdraw: amount={}", amount);
        
        // 验证提现金额
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "提现金额必须大于0");
        }
        
        // 获取平台钱包
        Wallet wallet = getPlatformWallet();
        
        // 验证余额是否充足
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "余额不足");
        }
        
        // 扣除余额
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setTotalWithdraw(wallet.getTotalWithdraw().add(amount));
        
        Wallet savedWallet = walletRepository.save(wallet);
        
        // 创建交易记录
        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setType(Transaction.TransactionType.WITHDRAW);
        transaction.setAmount(amount.negate()); // 负数表示支出
        transaction.setBalanceAfter(savedWallet.getBalance());
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setDescription("平台提现");
        transactionRepository.save(transaction);
        
        log.info("Platform wallet withdraw completed: amount={}, balance={}", amount, savedWallet.getBalance());
        
        return savedWallet;
    }

    /**
     * 获取平台统计数据
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public com.blog.dto.StatisticsResponse getStatistics() {
        log.info("Getting platform statistics");
        
        // 统计用户总数
        Long userCount = userRepository.count();
        
        // 统计文章总数
        Long articleCount = articleRepository.count();
        
        // 统计待审核文章数
        Long pendingArticleCount = articleRepository.countByReviewStatus(Article.ReviewStatus.PENDING);
        
        // 统计平台总收益（管理员钱包余额）
        Wallet platformWallet = getPlatformWallet();
        BigDecimal totalRevenue = platformWallet != null ? platformWallet.getBalance() : BigDecimal.ZERO;
        
        com.blog.dto.StatisticsResponse response = new com.blog.dto.StatisticsResponse(
            userCount,
            articleCount,
            pendingArticleCount,
            totalRevenue
        );
        
        log.info("Platform statistics: users={}, articles={}, pending={}, revenue={}", 
            userCount, articleCount, pendingArticleCount, totalRevenue);
        
        return response;
    }

    /**
     * 获取图表数据
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public com.blog.dto.ChartDataResponse getChartData() {
        log.info("Getting chart data");
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);
        
        // 获取最近7天的日期列表
        List<String> dates = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            dates.add(now.minusDays(i).format(java.time.format.DateTimeFormatter.ofPattern("MM-dd")));
        }
        
        // 获取文章发布趋势（简化版：使用总数模拟）
        List<Long> articleValues = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            // 统计当天发布的文章数
            long count = articleRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            articleValues.add(count);
        }
        
        // 获取用户增长趋势
        List<Long> userValues = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            // 统计当天注册的用户数
            long count = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            userValues.add(count);
        }
        
        // 获取分类分布
        List<com.blog.dto.ChartDataResponse.CategoryData> categoryDistribution = new ArrayList<>();
        List<Object[]> categoryStats = articleRepository.countArticlesByCategory();
        for (Object[] stat : categoryStats) {
            String categoryName = stat[0] != null ? stat[0].toString() : "Uncategorized";
            Long count = ((Number) stat[1]).longValue();
            categoryDistribution.add(new com.blog.dto.ChartDataResponse.CategoryData(categoryName, count));
        }
        
        // 获取平台收益趋势
        Wallet platformWallet = getPlatformWallet();
        List<Double> incomeValues = new ArrayList<>();
        List<Double> expenseValues = new ArrayList<>();
        List<Double> profitValues = new ArrayList<>();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            // 获取当天的所有交易
            List<Transaction> transactions = transactionRepository.findByWalletIdAndCreatedAtBetween(
                platformWallet.getId(), startOfDay, endOfDay);
            
            double dailyIncome = 0.0;
            double dailyExpense = 0.0;
            
            for (Transaction t : transactions) {
                if (t.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    // 正数为收入（INCOME类型）
                    if (t.getType() == Transaction.TransactionType.INCOME) {
                        dailyIncome += t.getAmount().doubleValue();
                    }
                } else {
                    // 负数为支出（WITHDRAW类型）
                    if (t.getType() == Transaction.TransactionType.WITHDRAW) {
                        dailyExpense += Math.abs(t.getAmount().doubleValue());
                    }
                }
            }
            
            incomeValues.add(dailyIncome);
            expenseValues.add(dailyExpense);
            profitValues.add(dailyIncome - dailyExpense);
        }
        
        com.blog.dto.ChartDataResponse.RevenueTrendData revenueTrend = 
            new com.blog.dto.ChartDataResponse.RevenueTrendData(dates, incomeValues, expenseValues, profitValues);
        
        com.blog.dto.ChartDataResponse response = new com.blog.dto.ChartDataResponse();
        response.setArticleTrend(new com.blog.dto.ChartDataResponse.TrendData(dates, articleValues));
        response.setUserGrowth(new com.blog.dto.ChartDataResponse.TrendData(dates, userValues));
        response.setCategoryDistribution(categoryDistribution);
        response.setRevenueTrend(revenueTrend);
        
        log.info("Chart data retrieved successfully");
        
        return response;
    }
    
    /**
     * 获取平台收益明细
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<com.blog.dto.TransactionDTO> getPlatformRevenue(Pageable pageable) {
        log.info("Getting platform revenue details: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        
        // 获取平台钱包
        Wallet platformWallet = getPlatformWallet();
        
        // 查询平台钱包的交易记录
        Page<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(
            platformWallet.getId(), pageable);
        
        // 转换为DTO并填充关联信息
        Page<com.blog.dto.TransactionDTO> dtoPage = transactions.map(transaction -> {
            com.blog.dto.TransactionDTO dto = com.blog.dto.TransactionDTO.fromEntity(transaction);
            
            // 填充关联用户昵称
            if (transaction.getRelatedUserId() != null) {
                userRepository.findById(transaction.getRelatedUserId()).ifPresent(user -> {
                    dto.setRelatedUserNickname(user.getNickname());
                });
            }
            
            // 填充关联文章标题
            if (transaction.getRelatedArticleId() != null) {
                articleRepository.findById(transaction.getRelatedArticleId()).ifPresent(article -> {
                    dto.setRelatedArticleTitle(article.getTitle());
                });
            }
            
            return dto;
        });
        
        log.info("Platform revenue details retrieved: total={}", dtoPage.getTotalElements());
        
        return dtoPage;
    }
}
