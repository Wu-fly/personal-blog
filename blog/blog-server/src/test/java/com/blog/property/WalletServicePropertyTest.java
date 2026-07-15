package com.blog.property;

import com.blog.entity.*;
import com.blog.exception.BusinessException;
import com.blog.repository.*;
import com.blog.service.impl.WalletServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.BigRange;
import net.jqwik.api.constraints.LongRange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * WalletService属性测试
 * 使用jqwik进行基于属性的测试
 */
class WalletServicePropertyTest {

    /**
     * Feature: personal-blog-system, Property 21: 钱包余额变化正确性（充值场景）
     * 验证需求: 30.2
     * 
     * 对于任意充值操作，钱包余额的变化应该等于充值金额
     */
    @Property(tries = 100)
    void testWalletBalanceChangeForRecharge(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @BigRange(min = "1.00", max = "10000.00") BigDecimal initialBalance,
            @ForAll @BigRange(min = "1.00", max = "5000.00") BigDecimal rechargeAmount) {
        
        // 准备mock对象
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        WalletServiceImpl walletService = new WalletServiceImpl(
                walletRepository, transactionRepository, purchaseRepository, 
                articleRepository, userRepository);
        
        // 创建初始钱包
        Wallet initialWallet = new Wallet();
        initialWallet.setId(1L);
        initialWallet.setUserId(userId);
        initialWallet.setBalance(initialBalance);
        initialWallet.setTotalIncome(BigDecimal.ZERO);
        initialWallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 模拟钱包查询
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        
        // 模拟钱包保存
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet wallet = invocation.getArgument(0);
            return wallet;
        });
        
        // 模拟交易记录保存
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction;
        });
        
        // 执行充值操作
        Wallet resultWallet = walletService.recharge(userId, rechargeAmount);
        
        // 属性验证：余额变化应该等于充值金额
        BigDecimal expectedBalance = initialBalance.add(rechargeAmount);
        assertEquals(0, expectedBalance.compareTo(resultWallet.getBalance()),
                String.format("Balance change should equal recharge amount. Expected: %s, Actual: %s",
                        expectedBalance, resultWallet.getBalance()));
        
        // 验证余额变化的精确性
        BigDecimal balanceChange = resultWallet.getBalance().subtract(initialBalance);
        assertEquals(0, balanceChange.compareTo(rechargeAmount),
                String.format("Balance change (%s) should exactly equal recharge amount (%s)",
                        balanceChange, rechargeAmount));
        
        // 验证总收入也增加了相应金额
        assertEquals(0, rechargeAmount.compareTo(resultWallet.getTotalIncome()),
                "Total income should increase by recharge amount");
        
        // 验证保存了钱包
        verify(walletRepository, times(1)).save(any(Wallet.class));
        
        // 验证创建了交易记录
        verify(transactionRepository, times(1)).save(argThat(transaction ->
                transaction.getType() == Transaction.TransactionType.RECHARGE &&
                transaction.getAmount().compareTo(rechargeAmount) == 0 &&
                transaction.getBalanceAfter().compareTo(expectedBalance) == 0
        ));
    }

    /**
     * Feature: personal-blog-system, Property 21: 钱包余额变化正确性（提现场景）
     * 验证需求: 30.2
     * 
     * 对于任意提现操作，钱包余额的变化应该等于提现金额（减少）
     */
    @Property(tries = 100)
    void testWalletBalanceChangeForWithdraw(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @BigRange(min = "100.00", max = "10000.00") BigDecimal initialBalance,
            @ForAll @BigRange(min = "10.00", max = "100.00") BigDecimal withdrawAmount) {
        
        // 确保初始余额大于提现金额
        Assume.that(initialBalance.compareTo(withdrawAmount) >= 0);
        
        // 准备mock对象
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        WalletServiceImpl walletService = new WalletServiceImpl(
                walletRepository, transactionRepository, purchaseRepository, 
                articleRepository, userRepository);
        
        // 创建初始钱包
        Wallet initialWallet = new Wallet();
        initialWallet.setId(1L);
        initialWallet.setUserId(userId);
        initialWallet.setBalance(initialBalance);
        initialWallet.setTotalIncome(BigDecimal.ZERO);
        initialWallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 模拟钱包查询
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        
        // 模拟钱包保存
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet wallet = invocation.getArgument(0);
            return wallet;
        });
        
        // 模拟交易记录保存
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction;
        });
        
        // 执行提现操作
        Wallet resultWallet = walletService.withdraw(userId, withdrawAmount);
        
        // 属性验证：余额变化应该等于提现金额（减少）
        BigDecimal expectedBalance = initialBalance.subtract(withdrawAmount);
        assertEquals(0, expectedBalance.compareTo(resultWallet.getBalance()),
                String.format("Balance change should equal withdraw amount. Expected: %s, Actual: %s",
                        expectedBalance, resultWallet.getBalance()));
        
        // 验证余额变化的精确性
        BigDecimal balanceChange = initialBalance.subtract(resultWallet.getBalance());
        assertEquals(0, balanceChange.compareTo(withdrawAmount),
                String.format("Balance change (%s) should exactly equal withdraw amount (%s)",
                        balanceChange, withdrawAmount));
        
        // 验证总提现金额增加了相应金额
        assertEquals(0, withdrawAmount.compareTo(resultWallet.getTotalWithdraw()),
                "Total withdraw should increase by withdraw amount");
        
        // 验证保存了钱包
        verify(walletRepository, times(1)).save(any(Wallet.class));
        
        // 验证创建了交易记录
        verify(transactionRepository, times(1)).save(argThat(transaction ->
                transaction.getType() == Transaction.TransactionType.WITHDRAW &&
                transaction.getAmount().compareTo(withdrawAmount) == 0 &&
                transaction.getBalanceAfter().compareTo(expectedBalance) == 0
        ));
    }

    /**
     * Feature: personal-blog-system, Property 21: 钱包余额变化正确性（打赏场景）
     * 验证需求: 17.2
     * 
     * 对于任意打赏操作，打赏用户余额减少，博主余额增加，变化金额应该相等
     */
    @Property(tries = 100)
    void testWalletBalanceChangeForReward(
            @ForAll @LongRange(min = 1, max = 1000000) Long fromUserId,
            @ForAll @LongRange(min = 1, max = 1000000) Long toUserId,
            @ForAll @BigRange(min = "100.00", max = "10000.00") BigDecimal fromInitialBalance,
            @ForAll @BigRange(min = "0.00", max = "5000.00") BigDecimal toInitialBalance,
            @ForAll @BigRange(min = "1.00", max = "100.00") BigDecimal rewardAmount,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId) {
        
        // 确保打赏用户和接收用户不同
        Assume.that(!fromUserId.equals(toUserId));
        
        // 确保打赏用户余额充足
        Assume.that(fromInitialBalance.compareTo(rewardAmount) >= 0);
        
        // 准备mock对象
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        WalletServiceImpl walletService = new WalletServiceImpl(
                walletRepository, transactionRepository, purchaseRepository, 
                articleRepository, userRepository);
        
        // 创建打赏用户钱包
        Wallet fromWallet = new Wallet();
        fromWallet.setId(1L);
        fromWallet.setUserId(fromUserId);
        fromWallet.setBalance(fromInitialBalance);
        fromWallet.setTotalIncome(BigDecimal.ZERO);
        fromWallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 创建博主钱包
        Wallet toWallet = new Wallet();
        toWallet.setId(2L);
        toWallet.setUserId(toUserId);
        toWallet.setBalance(toInitialBalance);
        toWallet.setTotalIncome(BigDecimal.ZERO);
        toWallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 创建博主用户
        User toUser = new User();
        toUser.setId(toUserId);
        toUser.setRole(User.UserRole.BLOGGER);
        toUser.setStatus(User.UserStatus.ACTIVE);
        
        // 模拟查询
        when(walletRepository.findByUserId(fromUserId)).thenReturn(Optional.of(fromWallet));
        when(walletRepository.findByUserId(toUserId)).thenReturn(Optional.of(toWallet));
        when(userRepository.findById(toUserId)).thenReturn(Optional.of(toUser));
        
        // 模拟钱包保存
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet wallet = invocation.getArgument(0);
            return wallet;
        });
        
        // 模拟交易记录保存
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction;
        });
        
        // 执行打赏操作
        Transaction resultTransaction = walletService.reward(fromUserId, toUserId, rewardAmount, articleId);
        
        // 属性验证：打赏用户余额减少应该等于打赏金额
        BigDecimal expectedFromBalance = fromInitialBalance.subtract(rewardAmount);
        assertEquals(0, expectedFromBalance.compareTo(fromWallet.getBalance()),
                String.format("From user balance should decrease by reward amount. Expected: %s, Actual: %s",
                        expectedFromBalance, fromWallet.getBalance()));
        
        // 属性验证：博主余额增加应该等于打赏金额
        BigDecimal expectedToBalance = toInitialBalance.add(rewardAmount);
        assertEquals(0, expectedToBalance.compareTo(toWallet.getBalance()),
                String.format("To user balance should increase by reward amount. Expected: %s, Actual: %s",
                        expectedToBalance, toWallet.getBalance()));
        
        // 验证余额变化的守恒性：打赏用户减少的金额 = 博主增加的金额
        BigDecimal fromBalanceChange = fromInitialBalance.subtract(fromWallet.getBalance());
        BigDecimal toBalanceChange = toWallet.getBalance().subtract(toInitialBalance);
        assertEquals(0, fromBalanceChange.compareTo(toBalanceChange),
                String.format("Balance change should be conserved. From decrease: %s, To increase: %s",
                        fromBalanceChange, toBalanceChange));
        assertEquals(0, fromBalanceChange.compareTo(rewardAmount),
                "Balance change should equal reward amount");
        
        // 验证博主总收入增加
        BigDecimal expectedToTotalIncome = toWallet.getTotalIncome();
        assertEquals(0, rewardAmount.compareTo(expectedToTotalIncome),
                "Blogger total income should increase by reward amount");
        
        // 验证保存了两个钱包
        verify(walletRepository, times(2)).save(any(Wallet.class));
        
        // 验证创建了两条交易记录
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        
        // 验证返回的交易记录是博主的收入记录
        assertNotNull(resultTransaction);
        assertEquals(Transaction.TransactionType.REWARD, resultTransaction.getType());
        assertEquals(0, rewardAmount.compareTo(resultTransaction.getAmount()));
    }

    /**
     * Feature: personal-blog-system, Property 21: 钱包余额变化正确性（余额不足场景）
     * 验证需求: 30.2
     * 
     * 对于任意余额不足的提现或打赏操作，应该拒绝并且余额不变
     */
    @Property(tries = 100)
    void testWalletBalanceUnchangedWhenInsufficientBalance(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @BigRange(min = "0.00", max = "50.00") BigDecimal initialBalance,
            @ForAll @BigRange(min = "100.00", max = "500.00") BigDecimal requestAmount) {
        
        // 确保余额不足
        Assume.that(initialBalance.compareTo(requestAmount) < 0);
        
        // 准备mock对象
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        WalletServiceImpl walletService = new WalletServiceImpl(
                walletRepository, transactionRepository, purchaseRepository, 
                articleRepository, userRepository);
        
        // 创建初始钱包
        Wallet initialWallet = new Wallet();
        initialWallet.setId(1L);
        initialWallet.setUserId(userId);
        initialWallet.setBalance(initialBalance);
        initialWallet.setTotalIncome(BigDecimal.ZERO);
        initialWallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 模拟钱包查询
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        
        // 测试提现场景
        if (requestAmount.compareTo(new BigDecimal("10.00")) >= 0) {
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                walletService.withdraw(userId, requestAmount);
            });
            
            // 验证错误消息
            assertEquals("余额不足", exception.getMessage());
            
            // 验证余额没有变化
            assertEquals(0, initialBalance.compareTo(initialWallet.getBalance()),
                    "Balance should remain unchanged when insufficient balance");
            
            // 验证没有保存钱包（因为操作失败）
            verify(walletRepository, never()).save(any(Wallet.class));
            
            // 验证没有创建交易记录
            verify(transactionRepository, never()).save(any(Transaction.class));
        }
    }

    /**
     * Feature: personal-blog-system, Property 21: 钱包余额变化正确性（多次操作累积）
     * 验证需求: 30.2
     * 
     * 对于任意多次充值操作，最终余额应该等于初始余额加上所有充值金额之和
     */
    @Property(tries = 100)
    void testWalletBalanceChangeForMultipleRecharges(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @BigRange(min = "0.00", max = "1000.00") BigDecimal initialBalance,
            @ForAll("rechargeAmountList") List<BigDecimal> rechargeAmounts) {
        
        // 准备mock对象
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        WalletServiceImpl walletService = new WalletServiceImpl(
                walletRepository, transactionRepository, purchaseRepository, 
                articleRepository, userRepository);
        
        // 创建初始钱包
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUserId(userId);
        wallet.setBalance(initialBalance);
        wallet.setTotalIncome(BigDecimal.ZERO);
        wallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 模拟钱包查询
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        
        // 模拟钱包保存
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet savedWallet = invocation.getArgument(0);
            return savedWallet;
        });
        
        // 模拟交易记录保存
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction;
        });
        
        // 计算预期的总充值金额
        BigDecimal totalRechargeAmount = rechargeAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 执行多次充值操作
        for (BigDecimal amount : rechargeAmounts) {
            wallet = walletService.recharge(userId, amount);
        }
        
        // 属性验证：最终余额应该等于初始余额加上所有充值金额之和
        BigDecimal expectedFinalBalance = initialBalance.add(totalRechargeAmount);
        assertEquals(0, expectedFinalBalance.compareTo(wallet.getBalance()),
                String.format("Final balance should equal initial balance plus total recharge amount. " +
                        "Initial: %s, Total recharge: %s, Expected: %s, Actual: %s",
                        initialBalance, totalRechargeAmount, expectedFinalBalance, wallet.getBalance()));
        
        // 验证余额变化的精确性
        BigDecimal totalBalanceChange = wallet.getBalance().subtract(initialBalance);
        assertEquals(0, totalBalanceChange.compareTo(totalRechargeAmount),
                String.format("Total balance change (%s) should equal total recharge amount (%s)",
                        totalBalanceChange, totalRechargeAmount));
        
        // 验证充值次数
        verify(walletRepository, times(rechargeAmounts.size())).save(any(Wallet.class));
        verify(transactionRepository, times(rechargeAmounts.size())).save(any(Transaction.class));
    }

    /**
     * Feature: personal-blog-system, Property 21: 钱包余额变化正确性（混合操作）
     * 验证需求: 30.2, 17.2
     * 
     * 对于任意充值和提现的混合操作，最终余额应该等于初始余额加上充值减去提现
     */
    @Property(tries = 100)
    void testWalletBalanceChangeForMixedOperations(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @BigRange(min = "1000.00", max = "5000.00") BigDecimal initialBalance,
            @ForAll @BigRange(min = "100.00", max = "500.00") BigDecimal rechargeAmount,
            @ForAll @BigRange(min = "10.00", max = "100.00") BigDecimal withdrawAmount) {
        
        // 确保操作后余额仍然为正
        BigDecimal finalBalance = initialBalance.add(rechargeAmount).subtract(withdrawAmount);
        Assume.that(finalBalance.compareTo(BigDecimal.ZERO) > 0);
        
        // 准备mock对象
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        WalletServiceImpl walletService = new WalletServiceImpl(
                walletRepository, transactionRepository, purchaseRepository, 
                articleRepository, userRepository);
        
        // 创建初始钱包
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUserId(userId);
        wallet.setBalance(initialBalance);
        wallet.setTotalIncome(BigDecimal.ZERO);
        wallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 模拟钱包查询
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        
        // 模拟钱包保存
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet savedWallet = invocation.getArgument(0);
            return savedWallet;
        });
        
        // 模拟交易记录保存
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction;
        });
        
        // 执行充值操作
        wallet = walletService.recharge(userId, rechargeAmount);
        BigDecimal balanceAfterRecharge = wallet.getBalance();
        
        // 执行提现操作
        wallet = walletService.withdraw(userId, withdrawAmount);
        BigDecimal balanceAfterWithdraw = wallet.getBalance();
        
        // 属性验证：最终余额应该等于初始余额 + 充值 - 提现
        BigDecimal expectedFinalBalance = initialBalance.add(rechargeAmount).subtract(withdrawAmount);
        assertEquals(0, expectedFinalBalance.compareTo(balanceAfterWithdraw),
                String.format("Final balance should equal initial + recharge - withdraw. " +
                        "Initial: %s, Recharge: %s, Withdraw: %s, Expected: %s, Actual: %s",
                        initialBalance, rechargeAmount, withdrawAmount, expectedFinalBalance, balanceAfterWithdraw));
        
        // 验证中间状态：充值后余额
        BigDecimal expectedBalanceAfterRecharge = initialBalance.add(rechargeAmount);
        assertEquals(0, expectedBalanceAfterRecharge.compareTo(balanceAfterRecharge),
                "Balance after recharge should equal initial balance plus recharge amount");
        
        // 验证总余额变化
        BigDecimal totalBalanceChange = balanceAfterWithdraw.subtract(initialBalance);
        BigDecimal expectedTotalChange = rechargeAmount.subtract(withdrawAmount);
        assertEquals(0, expectedTotalChange.compareTo(totalBalanceChange),
                String.format("Total balance change (%s) should equal recharge minus withdraw (%s)",
                        totalBalanceChange, expectedTotalChange));
        
        // 验证操作次数
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }

    /**
     * 生成充值金额列表（2-5个金额）
     */
    @Provide
    Arbitrary<List<BigDecimal>> rechargeAmountList() {
        Arbitrary<BigDecimal> amount = Arbitraries.bigDecimals()
                .between(new BigDecimal("1.00"), new BigDecimal("100.00"))
                .ofScale(2);
        
        return amount.list().ofMinSize(2).ofMaxSize(5);
    }

    /**
     * Feature: personal-blog-system, Property 22: 付费文章收益分成
     * 验证需求: 31.6
     * 
     * 对于任意付费文章购买，博主应该获得90%收益，管理员应该获得10%收益
     */
    @Property(tries = 100)
    void testPaidArticleRevenueSplit(
            @ForAll @LongRange(min = 2, max = 1000000) Long buyerId,
            @ForAll @LongRange(min = 2, max = 1000000) Long bloggerId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @BigRange(min = "100.00", max = "10000.00") BigDecimal buyerInitialBalance,
            @ForAll @BigRange(min = "0.00", max = "5000.00") BigDecimal bloggerInitialBalance,
            @ForAll @BigRange(min = "0.00", max = "5000.00") BigDecimal adminInitialBalance,
            @ForAll @BigRange(min = "1.00", max = "100.00") BigDecimal articlePrice) {
        
        // 确保购买者、博主和管理员ID不同
        Assume.that(!buyerId.equals(bloggerId));
        Assume.that(!buyerId.equals(1L)); // 管理员ID为1
        Assume.that(!bloggerId.equals(1L));
        
        // 确保购买者余额充足
        Assume.that(buyerInitialBalance.compareTo(articlePrice) >= 0);
        
        // 准备mock对象
        WalletRepository walletRepository = mock(WalletRepository.class);
        TransactionRepository transactionRepository = mock(TransactionRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        
        WalletServiceImpl walletService = new WalletServiceImpl(
                walletRepository, transactionRepository, purchaseRepository, 
                articleRepository, userRepository);
        
        // 创建付费文章
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(bloggerId);
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setIsPaid(true);
        article.setPrice(articlePrice);
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        
        // 创建购买者钱包
        Wallet buyerWallet = new Wallet();
        buyerWallet.setId(2L);
        buyerWallet.setUserId(buyerId);
        buyerWallet.setBalance(buyerInitialBalance);
        buyerWallet.setTotalIncome(BigDecimal.ZERO);
        buyerWallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 创建博主钱包
        Wallet bloggerWallet = new Wallet();
        bloggerWallet.setId(3L);
        bloggerWallet.setUserId(bloggerId);
        bloggerWallet.setBalance(bloggerInitialBalance);
        bloggerWallet.setTotalIncome(BigDecimal.ZERO);
        bloggerWallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 创建管理员钱包（ID为1）
        Wallet adminWallet = new Wallet();
        adminWallet.setId(1L);
        adminWallet.setUserId(1L);
        adminWallet.setBalance(adminInitialBalance);
        adminWallet.setTotalIncome(BigDecimal.ZERO);
        adminWallet.setTotalWithdraw(BigDecimal.ZERO);
        
        // 模拟查询
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(purchaseRepository.existsByUserIdAndArticleId(buyerId, articleId)).thenReturn(false);
        when(walletRepository.findByUserId(buyerId)).thenReturn(Optional.of(buyerWallet));
        when(walletRepository.findByUserId(bloggerId)).thenReturn(Optional.of(bloggerWallet));
        when(walletRepository.findByUserId(1L)).thenReturn(Optional.of(adminWallet));
        
        // 模拟钱包保存
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet wallet = invocation.getArgument(0);
            return wallet;
        });
        
        // 模拟交易记录保存
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            return transaction;
        });
        
        // 模拟购买记录保存
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase purchase = invocation.getArgument(0);
            return purchase;
        });
        
        // 执行购买操作
        Transaction resultTransaction = walletService.purchaseArticle(buyerId, articleId);
        
        // 计算预期的分成金额（使用与实现相同的计算方式）
        BigDecimal expectedBloggerIncome = articlePrice.multiply(new BigDecimal("0.90"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedAdminIncome = articlePrice.multiply(new BigDecimal("0.10"))
                .setScale(2, RoundingMode.HALF_UP);
        
        // 属性验证1：博主应该获得90%收益
        BigDecimal bloggerIncomeActual = bloggerWallet.getBalance().subtract(bloggerInitialBalance);
        assertEquals(0, expectedBloggerIncome.compareTo(bloggerIncomeActual),
                String.format("Blogger should receive 90%% of article price. " +
                        "Article price: %s, Expected blogger income: %s, Actual: %s",
                        articlePrice, expectedBloggerIncome, bloggerIncomeActual));
        
        // 属性验证2：管理员应该获得10%收益
        BigDecimal adminIncomeActual = adminWallet.getBalance().subtract(adminInitialBalance);
        assertEquals(0, expectedAdminIncome.compareTo(adminIncomeActual),
                String.format("Admin should receive 10%% of article price. " +
                        "Article price: %s, Expected admin income: %s, Actual: %s",
                        articlePrice, expectedAdminIncome, adminIncomeActual));
        
        // 属性验证3：博主收益比例应该是90%
        BigDecimal bloggerIncomeRatio = bloggerIncomeActual.divide(articlePrice, 4, RoundingMode.HALF_UP);
        BigDecimal expectedBloggerRatio = new BigDecimal("0.90");
        assertTrue(bloggerIncomeRatio.subtract(expectedBloggerRatio).abs().compareTo(new BigDecimal("0.01")) <= 0,
                String.format("Blogger income ratio should be approximately 90%%. " +
                        "Expected: %s, Actual: %s", expectedBloggerRatio, bloggerIncomeRatio));
        
        // 属性验证4：管理员收益比例应该是10%
        BigDecimal adminIncomeRatio = adminIncomeActual.divide(articlePrice, 4, RoundingMode.HALF_UP);
        BigDecimal expectedAdminRatio = new BigDecimal("0.10");
        assertTrue(adminIncomeRatio.subtract(expectedAdminRatio).abs().compareTo(new BigDecimal("0.01")) <= 0,
                String.format("Admin income ratio should be approximately 10%%. " +
                        "Expected: %s, Actual: %s", expectedAdminRatio, adminIncomeRatio));
        
        // 属性验证5：购买者支付的金额应该等于文章价格
        BigDecimal buyerPayment = buyerInitialBalance.subtract(buyerWallet.getBalance());
        assertEquals(0, articlePrice.compareTo(buyerPayment),
                String.format("Buyer should pay exactly the article price. " +
                        "Expected: %s, Actual: %s", articlePrice, buyerPayment));
        
        // 属性验证6：总收益应该等于文章价格（博主收益 + 管理员收益）
        BigDecimal totalIncome = bloggerIncomeActual.add(adminIncomeActual);
        // 由于四舍五入，允许1分钱的误差
        assertTrue(totalIncome.subtract(articlePrice).abs().compareTo(new BigDecimal("0.01")) <= 0,
                String.format("Total income (blogger + admin) should equal article price. " +
                        "Article price: %s, Blogger income: %s, Admin income: %s, Total: %s",
                        articlePrice, bloggerIncomeActual, adminIncomeActual, totalIncome));
        
        // 属性验证7：博主的总收入应该增加
        assertEquals(0, expectedBloggerIncome.compareTo(bloggerWallet.getTotalIncome()),
                "Blogger total income should increase by blogger income amount");
        
        // 属性验证8：管理员的总收入应该增加
        assertEquals(0, expectedAdminIncome.compareTo(adminWallet.getTotalIncome()),
                "Admin total income should increase by admin income amount");
        
        // 验证保存了三个钱包（购买者、博主、管理员）
        verify(walletRepository, times(3)).save(any(Wallet.class));
        
        // 验证创建了三条交易记录
        verify(transactionRepository, times(3)).save(any(Transaction.class));
        
        // 验证创建了购买记录
        verify(purchaseRepository, times(1)).save(argThat(purchase ->
                purchase.getUserId().equals(buyerId) &&
                purchase.getArticleId().equals(articleId) &&
                purchase.getAmount().compareTo(articlePrice) == 0
        ));
        
        // 验证增加了文章购买次数
        verify(articleRepository, times(1)).incrementPurchaseCount(articleId);
        
        // 验证返回的交易记录是购买者的支出记录
        assertNotNull(resultTransaction);
        assertEquals(Transaction.TransactionType.PURCHASE, resultTransaction.getType());
        assertEquals(0, articlePrice.negate().compareTo(resultTransaction.getAmount()));
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
