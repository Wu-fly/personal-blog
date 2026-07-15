package com.blog.service.impl;

import com.blog.entity.*;
import com.blog.exception.BusinessException;
import com.blog.repository.*;
import com.blog.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 钱包管理服务实现
 * 需求: 17.1-17.5, 18.1-18.8, 30.1-30.5, 31.1-31.9
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PurchaseRepository purchaseRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    // 最低充值额度
    private static final BigDecimal MIN_RECHARGE_AMOUNT = new BigDecimal("1.00");
    // 最低提现额度
    private static final BigDecimal MIN_WITHDRAW_AMOUNT = new BigDecimal("10.00");
    // 最低打赏额度
    private static final BigDecimal MIN_REWARD_AMOUNT = new BigDecimal("1.00");
    // 平台抽成比例（10%）
    private static final BigDecimal PLATFORM_COMMISSION_RATE = new BigDecimal("0.10");
    // 博主收益比例（90%）
    private static final BigDecimal BLOGGER_INCOME_RATE = new BigDecimal("0.90");

    /**
     * 充值
     * 需求: 30.1-30.5
     * - 验证充值金额是否满足最低额度
     * - 增加用户钱包余额
     * - 创建充值交易记录
     * 
     * 权限: 需要登录才能充值
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Wallet recharge(Long userId, BigDecimal amount) {
        log.info("User {} recharging amount: {}", userId, amount);

        // 验证充值金额 (需求: 30.3)
        if (amount == null || amount.compareTo(MIN_RECHARGE_AMOUNT) < 0) {
            throw new BusinessException("INVALID_AMOUNT", 
                String.format("充值金额不能小于%.2f元", MIN_RECHARGE_AMOUNT));
        }

        // 获取或创建钱包
        Wallet wallet = getOrCreateWallet(userId);

        // 增加余额 (需求: 30.2)
        BigDecimal newBalance = wallet.getBalance().add(amount);
        wallet.setBalance(newBalance);
        wallet.setTotalIncome(wallet.getTotalIncome().add(amount));

        // 保存钱包
        Wallet savedWallet = walletRepository.save(wallet);

        // 创建充值交易记录 (需求: 30.4)
        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setType(Transaction.TransactionType.RECHARGE);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setDescription("钱包充值");
        transactionRepository.save(transaction);

        log.info("Recharge successful for user {}, new balance: {}", userId, newBalance);
        return savedWallet;
    }

    /**
     * 提现
     * 需求: 18.4-18.8
     * - 验证余额是否充足
     * - 验证提现金额是否满足最低额度
     * - 扣除余额
     * - 创建提现记录
     * 
     * 权限: 需要登录才能提现
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Wallet withdraw(Long userId, BigDecimal amount) {
        log.info("User {} withdrawing amount: {}", userId, amount);

        // 验证提现金额 (需求: 18.6)
        if (amount == null || amount.compareTo(MIN_WITHDRAW_AMOUNT) < 0) {
            throw new BusinessException("INVALID_AMOUNT", 
                String.format("提现金额不能小于%.2f元", MIN_WITHDRAW_AMOUNT));
        }

        // 获取钱包
        Wallet wallet = getOrCreateWallet(userId);

        // 验证余额是否充足 (需求: 18.4, 18.5)
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "余额不足");
        }

        // 扣除余额
        BigDecimal newBalance = wallet.getBalance().subtract(amount);
        wallet.setBalance(newBalance);
        wallet.setTotalWithdraw(wallet.getTotalWithdraw().add(amount));

        // 保存钱包
        Wallet savedWallet = walletRepository.save(wallet);

        // 创建提现交易记录 (需求: 18.7)
        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setType(Transaction.TransactionType.WITHDRAW);
        transaction.setAmount(amount.negate()); // 负数表示支出
        transaction.setBalanceAfter(newBalance);
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setDescription("提现");
        transactionRepository.save(transaction);

        log.info("Withdraw successful for user {}, new balance: {}", userId, newBalance);
        return savedWallet;
    }

    /**
     * 打赏博主
     * 需求: 17.1-17.5
     * - 验证打赏金额是否满足最低限额
     * - 扣除打赏用户余额
     * - 增加博主钱包余额
     * - 创建交易记录
     * - 发送打赏通知（TODO: 需要消息服务）
     * 
     * 权限: 需求 38.6 - 需要登录才能打赏
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Transaction reward(Long fromUserId, Long toUserId, BigDecimal amount, Long articleId) {
        log.info("User {} rewarding user {} with amount: {}", fromUserId, toUserId, amount);

        // 验证打赏金额 (需求: 17.3)
        if (amount == null || amount.compareTo(MIN_REWARD_AMOUNT) < 0) {
            throw new BusinessException("INVALID_AMOUNT", 
                String.format("打赏金额不能小于%.2f元", MIN_REWARD_AMOUNT));
        }

        // 验证不能打赏自己
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException("INVALID_OPERATION", "不能打赏自己");
        }

        // 简化验证：只检查用户是否存在，不检查角色
        // 如果用户不存在，创建钱包时会失败
        if (!userRepository.existsById(toUserId)) {
            throw new BusinessException("USER_NOT_FOUND", "用户不存在");
        }

        // 获取打赏用户钱包
        Wallet fromWallet = getOrCreateWallet(fromUserId);

        // 验证余额是否充足
        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "余额不足");
        }

        // 扣除打赏用户余额
        BigDecimal fromNewBalance = fromWallet.getBalance().subtract(amount);
        fromWallet.setBalance(fromNewBalance);
        walletRepository.save(fromWallet);

        // 获取博主钱包
        Wallet toWallet = getOrCreateWallet(toUserId);

        // 增加博主余额 (需求: 17.2)
        BigDecimal toNewBalance = toWallet.getBalance().add(amount);
        toWallet.setBalance(toNewBalance);
        toWallet.setTotalIncome(toWallet.getTotalIncome().add(amount));
        walletRepository.save(toWallet);

        // 创建打赏用户的交易记录
        Transaction fromTransaction = new Transaction();
        fromTransaction.setWalletId(fromWallet.getId());
        fromTransaction.setType(Transaction.TransactionType.REWARD);
        fromTransaction.setAmount(amount.negate()); // 负数表示支出
        fromTransaction.setBalanceAfter(fromNewBalance);
        fromTransaction.setRelatedUserId(toUserId);
        fromTransaction.setRelatedArticleId(articleId);
        fromTransaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        fromTransaction.setDescription("打赏博主");
        transactionRepository.save(fromTransaction);

        // 创建博主的收入交易记录 (需求: 17.5)
        Transaction toTransaction = new Transaction();
        toTransaction.setWalletId(toWallet.getId());
        toTransaction.setType(Transaction.TransactionType.REWARD);
        toTransaction.setAmount(amount);
        toTransaction.setBalanceAfter(toNewBalance);
        toTransaction.setRelatedUserId(fromUserId);
        toTransaction.setRelatedArticleId(articleId);
        toTransaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        toTransaction.setDescription("收到打赏");
        transactionRepository.save(toTransaction);

        log.info("Reward successful from user {} to user {}, amount: {}", fromUserId, toUserId, amount);
        
        // TODO: 发送打赏通知 (需求: 17.4)
        
        return toTransaction;
    }

    /**
     * 购买付费文章
     * 需求: 31.1-31.9
     * - 验证文章是否为付费文章
     * - 验证用户是否已购买
     * - 验证余额是否充足
     * - 扣除用户余额
     * - 分成给博主（90%）和管理员（10%）
     * - 创建购买记录
     * 
     * 权限: 需要登录才能购买付费文章
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public Transaction purchaseArticle(Long userId, Long articleId) {
        log.info("User {} purchasing article {}", userId, articleId);

        // 查找文章
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));

        // 验证文章是否为付费文章 (需求: 31.1)
        if (!article.getIsPaid()) {
            throw new BusinessException("INVALID_OPERATION", "该文章不是付费文章");
        }

        // 验证文章是否审核通过
        if (article.getReviewStatus() != Article.ReviewStatus.APPROVED) {
            throw new BusinessException("ARTICLE_NOT_APPROVED", "文章未审核通过");
        }

        // 验证用户是否已购买 (需求: 31.7)
        if (purchaseRepository.existsByUserIdAndArticleId(userId, articleId)) {
            throw new BusinessException("ALREADY_PURCHASED", "您已购买过该文章");
        }

        // 验证不能购买自己的文章
        if (userId.equals(article.getUserId())) {
            throw new BusinessException("INVALID_OPERATION", "不能购买自己的文章");
        }

        BigDecimal price = article.getPrice();

        // 获取用户钱包
        Wallet userWallet = getOrCreateWallet(userId);

        // 验证余额是否充足 (需求: 31.4, 31.5)
        if (userWallet.getBalance().compareTo(price) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "余额不足，请先充值");
        }

        // 扣除用户余额
        BigDecimal userNewBalance = userWallet.getBalance().subtract(price);
        userWallet.setBalance(userNewBalance);
        walletRepository.save(userWallet);

        // 计算分成 (需求: 31.6)
        BigDecimal bloggerIncome = price.multiply(BLOGGER_INCOME_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal adminIncome = price.multiply(PLATFORM_COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);

        // 获取博主钱包
        Wallet bloggerWallet = getOrCreateWallet(article.getUserId());
        BigDecimal bloggerNewBalance = bloggerWallet.getBalance().add(bloggerIncome);
        bloggerWallet.setBalance(bloggerNewBalance);
        bloggerWallet.setTotalIncome(bloggerWallet.getTotalIncome().add(bloggerIncome));
        walletRepository.save(bloggerWallet);

        // 获取管理员钱包（管理员ID为2）
        // TODO: 应该从配置或数据库中获取管理员ID
        Long adminId = 2L;
        Wallet adminWallet = getOrCreateWallet(adminId);
        BigDecimal adminNewBalance = adminWallet.getBalance().add(adminIncome);
        adminWallet.setBalance(adminNewBalance);
        adminWallet.setTotalIncome(adminWallet.getTotalIncome().add(adminIncome));
        walletRepository.save(adminWallet);

        // 创建用户的购买交易记录
        Transaction userTransaction = new Transaction();
        userTransaction.setWalletId(userWallet.getId());
        userTransaction.setType(Transaction.TransactionType.PURCHASE);
        userTransaction.setAmount(price.negate()); // 负数表示支出
        userTransaction.setBalanceAfter(userNewBalance);
        userTransaction.setRelatedUserId(article.getUserId());
        userTransaction.setRelatedArticleId(articleId);
        userTransaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        userTransaction.setDescription("购买付费文章");
        transactionRepository.save(userTransaction);

        // 创建博主的收入交易记录
        Transaction bloggerTransaction = new Transaction();
        bloggerTransaction.setWalletId(bloggerWallet.getId());
        bloggerTransaction.setType(Transaction.TransactionType.INCOME);
        bloggerTransaction.setAmount(bloggerIncome);
        bloggerTransaction.setBalanceAfter(bloggerNewBalance);
        bloggerTransaction.setRelatedUserId(userId);
        bloggerTransaction.setRelatedArticleId(articleId);
        bloggerTransaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        bloggerTransaction.setDescription("付费文章收益");
        transactionRepository.save(bloggerTransaction);

        // 创建管理员的收入交易记录
        Transaction adminTransaction = new Transaction();
        adminTransaction.setWalletId(adminWallet.getId());
        adminTransaction.setType(Transaction.TransactionType.INCOME);
        adminTransaction.setAmount(adminIncome);
        adminTransaction.setBalanceAfter(adminNewBalance);
        adminTransaction.setRelatedUserId(userId);
        adminTransaction.setRelatedArticleId(articleId);
        adminTransaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        adminTransaction.setDescription("平台抽成");
        transactionRepository.save(adminTransaction);

        // 创建购买记录 (需求: 31.6)
        Purchase purchase = new Purchase();
        purchase.setUserId(userId);
        purchase.setArticleId(articleId);
        purchase.setAmount(price);
        purchaseRepository.save(purchase);

        // 增加文章购买次数
        articleRepository.incrementPurchaseCount(articleId);

        log.info("Purchase successful for user {}, article {}, price: {}", userId, articleId, price);
        
        return userTransaction;
    }

    /**
     * 查询钱包余额
     * 需求: 18.1
     * - 返回当前余额和总收益
     */
    @Override
    @Transactional
    public Wallet getBalance(Long userId) {
        log.info("Getting balance for user {}", userId);
        return getOrCreateWallet(userId);
    }

    /**
     * 查询交易记录
     * 需求: 18.2, 30.5
     * - 返回所有交易记录
     * - 按时间倒序排列
     * - 支持分页
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getTransactions(Long userId, Pageable pageable) {
        log.info("Getting transactions for user {}", userId);
        
        Wallet wallet = getOrCreateWallet(userId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId(), pageable);
    }

    /**
     * 查询收益明细
     * 需求: 18.3
     * - 返回所有收益来源（打赏、付费文章购买）
     * - 按时间倒序排列
     * - 支持分页
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Transaction> getRevenue(Long userId, Pageable pageable) {
        log.info("Getting revenue for user {}", userId);
        
        Wallet wallet = getOrCreateWallet(userId);
        return transactionRepository.findIncomeTransactions(wallet.getId(), pageable);
    }

    /**
     * 创建或获取用户钱包
     * - 如果用户没有钱包，自动创建
     * - 返回用户钱包信息
     */
    @Override
    @Transactional
    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
            .orElseGet(() -> {
                log.info("Creating new wallet for user {}", userId);
                
                // 不需要验证用户是否存在，数据库外键约束会处理
                // 如果用户不存在，保存时会抛出异常
                Wallet wallet = new Wallet();
                wallet.setUserId(userId);
                wallet.setBalance(BigDecimal.ZERO);
                wallet.setTotalIncome(BigDecimal.ZERO);
                wallet.setTotalWithdraw(BigDecimal.ZERO);
                return walletRepository.save(wallet);
            });
    }
}
