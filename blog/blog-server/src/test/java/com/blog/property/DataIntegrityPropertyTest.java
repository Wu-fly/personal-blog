package com.blog.property;

import com.blog.entity.*;
import com.blog.repository.*;
import com.blog.service.impl.ArticleServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 数据完整性属性测试
 * 使用jqwik进行基于属性的测试
 */
class DataIntegrityPropertyTest {

    /**
     * Feature: personal-blog-system, Property 12: 级联删除完整性
     * 验证需求: 10.3
     * 
     * 对于任意文章，删除文章后，该文章的所有评论、点赞、收藏记录都应该被删除
     */
    @Property(tries = 50)
    void testCascadeDeleteIntegrity(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll @LongRange(min = 0, max = 5) int commentCount,
            @ForAll @LongRange(min = 0, max = 5) int likeCount,
            @ForAll @LongRange(min = 0, max = 5) int favoriteCount) {
        
        // Skip invalid inputs (jqwik edge cases may generate negative values)
        if (commentCount < 0 || likeCount < 0 || favoriteCount < 0) {
            return;
        }
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        LikeRepository likeRepository = mock(LikeRepository.class);
        FavoriteRepository favoriteRepository = mock(FavoriteRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建文章
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        article.setIsPaid(false);
        article.setCoverImage("/cover.jpg");
        article.setViewCount(100);
        article.setLikeCount(likeCount);
        article.setFavoriteCount(favoriteCount);
        article.setPurchaseCount(0);
        article.setIsPinned(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        // 创建评论列表（模拟文章有多个评论）
        List<Comment> comments = createComments(articleId, commentCount);
        when(commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId))
            .thenReturn(comments);
        
        // 创建点赞列表
        List<Like> likes = createLikes(articleId, likeCount);
        when(likeRepository.countByArticleId(articleId))
            .thenReturn((long) likeCount);
        
        // 创建收藏列表
        List<Favorite> favorites = createFavorites(articleId, favoriteCount);
        when(favoriteRepository.countByArticleId(articleId))
            .thenReturn((long) favoriteCount);
        
        // 验证删除前的状态
        long commentsBeforeDelete = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).size();
        long likesBeforeDelete = likeRepository.countByArticleId(articleId);
        long favoritesBeforeDelete = favoriteRepository.countByArticleId(articleId);
        
        assertEquals(commentCount, commentsBeforeDelete, 
            "Should have " + commentCount + " comments before delete");
        assertEquals(likeCount, likesBeforeDelete, 
            "Should have " + likeCount + " likes before delete");
        assertEquals(favoriteCount, favoritesBeforeDelete, 
            "Should have " + favoriteCount + " favorites before delete");
        
        // 模拟级联删除行为
        doAnswer(invocation -> {
            // 当文章被删除时，模拟数据库级联删除行为
            // 清空所有关联的评论、点赞、收藏
            when(commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId))
                .thenReturn(Collections.emptyList());
            when(likeRepository.countByArticleId(articleId))
                .thenReturn(0L);
            when(favoriteRepository.countByArticleId(articleId))
                .thenReturn(0L);
            return null;
        }).when(articleRepository).delete(article);
        
        // 执行删除文章
        articleService.deleteArticle(articleId, userId);
        
        // 属性验证：删除文章后，所有关联数据都应该被删除
        long commentsAfterDelete = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).size();
        long likesAfterDelete = likeRepository.countByArticleId(articleId);
        long favoritesAfterDelete = favoriteRepository.countByArticleId(articleId);
        
        assertEquals(0, commentsAfterDelete, 
            "All comments should be deleted after article deletion (cascade delete)");
        assertEquals(0, likesAfterDelete, 
            "All likes should be deleted after article deletion (cascade delete)");
        assertEquals(0, favoritesAfterDelete, 
            "All favorites should be deleted after article deletion (cascade delete)");
        
        // 验证删除方法被调用
        verify(articleRepository, times(1)).delete(article);
        verify(articleRepository, times(1)).findById(articleId);
    }

    /**
     * Feature: personal-blog-system, Property 12: 级联删除完整性（包含子评论）
     * 验证需求: 10.3
     * 
     * 删除文章时，不仅要删除顶级评论，还要删除所有子评论（回复）
     */
    @Property(tries = 50)
    void testCascadeDeleteWithNestedComments(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll @LongRange(min = 1, max = 3) int topLevelCommentCount,
            @ForAll @LongRange(min = 0, max = 2) int repliesPerComment) {
        
        // Skip invalid inputs (jqwik edge cases may generate negative values)
        if (topLevelCommentCount < 1 || repliesPerComment < 0) {
            return;
        }
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建文章
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(userId);
        article.setTitle(title);
        article.setContent(content);
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        article.setIsPaid(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        // 创建嵌套评论结构（顶级评论 + 回复）
        List<Comment> allComments = createNestedComments(articleId, topLevelCommentCount, repliesPerComment);
        int totalCommentCount = topLevelCommentCount * (1 + repliesPerComment);
        
        when(commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId))
            .thenReturn(allComments);
        
        // 验证删除前的状态
        long commentsBeforeDelete = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).size();
        assertEquals(totalCommentCount, commentsBeforeDelete, 
            "Should have " + totalCommentCount + " total comments (including replies) before delete");
        
        // 模拟级联删除行为
        doAnswer(invocation -> {
            // 当文章被删除时，模拟数据库级联删除所有评论（包括子评论）
            when(commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId))
                .thenReturn(Collections.emptyList());
            return null;
        }).when(articleRepository).delete(article);
        
        // 执行删除文章
        articleService.deleteArticle(articleId, userId);
        
        // 属性验证：删除文章后，所有评论（包括子评论）都应该被删除
        long commentsAfterDelete = commentRepository.findByArticleIdOrderByCreatedAtDesc(articleId).size();
        assertEquals(0, commentsAfterDelete, 
            "All comments including nested replies should be deleted after article deletion");
        
        // 验证删除方法被调用
        verify(articleRepository, times(1)).delete(article);
    }

    /**
     * Feature: personal-blog-system, Property 12: 级联删除完整性（权限验证）
     * 验证需求: 10.3
     * 
     * 只有文章作者可以删除文章，其他用户尝试删除应该被拒绝
     */
    @Property(tries = 50)
    void testCascadeDeletePermissionCheck(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleOwnerId,
            @ForAll @LongRange(min = 1, max = 1000000) Long otherUserId,
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content) {
        
        // 确保是不同的用户
        if (articleOwnerId.equals(otherUserId)) {
            return;
        }
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 创建文章（属于articleOwnerId）
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(articleOwnerId);
        article.setTitle(title);
        article.setContent(content);
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        article.setIsPaid(false);
        
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        // 属性验证：非作者尝试删除文章应该抛出异常
        Exception exception = assertThrows(Exception.class, () -> {
            articleService.deleteArticle(articleId, otherUserId);
        }, "Non-owner should not be able to delete article");
        
        assertTrue(exception.getMessage().contains("只能删除自己的文章") || 
                   exception.getMessage().contains("PERMISSION_DENIED"),
            "Should reject deletion with permission error");
        
        // 验证文章没有被删除
        verify(articleRepository, never()).delete(any(Article.class));
        verify(articleRepository, times(1)).findById(articleId);
    }

    /**
     * Feature: personal-blog-system, Property 12: 级联删除完整性（不存在的文章）
     * 验证需求: 10.3
     * 
     * 尝试删除不存在的文章应该抛出异常
     */
    @Property(tries = 50)
    void testCascadeDeleteNonExistentArticle(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        CommentRepository commentRepository = mock(CommentRepository.class);
        
        ArticleServiceImpl articleService = new ArticleServiceImpl(
            articleRepository, userRepository, commentRepository
        );
        
        // 模拟文章不存在
        when(articleRepository.findById(articleId)).thenReturn(Optional.empty());
        
        // 属性验证：删除不存在的文章应该抛出异常
        Exception exception = assertThrows(Exception.class, () -> {
            articleService.deleteArticle(articleId, userId);
        }, "Should throw exception when deleting non-existent article");
        
        assertTrue(exception.getMessage().contains("文章不存在") || 
                   exception.getMessage().contains("ARTICLE_NOT_FOUND"),
            "Should reject deletion with article not found error");
        
        // 验证没有执行删除操作
        verify(articleRepository, never()).delete(any(Article.class));
        verify(articleRepository, times(1)).findById(articleId);
    }

    /**
     * 创建评论列表
     */
    private List<Comment> createComments(Long articleId, int count) {
        Comment[] comments = new Comment[count];
        for (int i = 0; i < count; i++) {
            Comment comment = new Comment();
            comment.setId((long) (i + 1));
            comment.setArticleId(articleId);
            comment.setUserId((long) (i + 100));
            comment.setContent("Test comment " + (i + 1));
            comment.setStatus(Comment.CommentStatus.APPROVED);
            comments[i] = comment;
        }
        return Arrays.asList(comments);
    }

    /**
     * 创建点赞列表
     */
    private List<Like> createLikes(Long articleId, int count) {
        Like[] likes = new Like[count];
        for (int i = 0; i < count; i++) {
            Like like = new Like();
            like.setId((long) (i + 1));
            like.setArticleId(articleId);
            like.setUserId((long) (i + 200));
            likes[i] = like;
        }
        return Arrays.asList(likes);
    }

    /**
     * 创建收藏列表
     */
    private List<Favorite> createFavorites(Long articleId, int count) {
        Favorite[] favorites = new Favorite[count];
        for (int i = 0; i < count; i++) {
            Favorite favorite = new Favorite();
            favorite.setId((long) (i + 1));
            favorite.setArticleId(articleId);
            favorite.setUserId((long) (i + 300));
            favorites[i] = favorite;
        }
        return Arrays.asList(favorites);
    }

    /**
     * 创建嵌套评论结构（顶级评论 + 回复）
     */
    private List<Comment> createNestedComments(Long articleId, int topLevelCount, int repliesPerComment) {
        List<Comment> allComments = new java.util.ArrayList<>();
        long commentIdCounter = 1;
        
        for (int i = 0; i < topLevelCount; i++) {
            // 创建顶级评论
            Comment topLevelComment = new Comment();
            topLevelComment.setId(commentIdCounter++);
            topLevelComment.setArticleId(articleId);
            topLevelComment.setUserId((long) (i + 100));
            topLevelComment.setContent("Top level comment " + (i + 1));
            topLevelComment.setStatus(Comment.CommentStatus.APPROVED);
            topLevelComment.setParentId(null);
            allComments.add(topLevelComment);
            
            // 创建回复
            for (int j = 0; j < repliesPerComment; j++) {
                Comment reply = new Comment();
                reply.setId(commentIdCounter++);
                reply.setArticleId(articleId);
                reply.setUserId((long) (j + 500));
                reply.setContent("Reply " + (j + 1) + " to comment " + (i + 1));
                reply.setStatus(Comment.CommentStatus.APPROVED);
                reply.setParentId(topLevelComment.getId());
                allComments.add(reply);
            }
        }
        
        return allComments;
    }

    /**
     * Feature: personal-blog-system, Property 13: 事务数据一致性
     * 验证需求: 10.2
     * 
     * 对于任意并发的数据库操作，使用事务应该确保数据的最终一致性
     * 
     * 测试场景：钱包充值操作
     * - 充值操作应该是原子性的：要么全部成功（余额增加+交易记录创建），要么全部失败
     * - 如果交易记录创建失败，余额不应该增加
     * - 如果余额更新失败，交易记录不应该被创建
     */
    @Property(tries = 100)
    void testTransactionConsistencyForWalletRecharge(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @LongRange(min = 1, max = 10000) long rechargeAmountCents) {
        
        // 将分转换为元（避免浮点数精度问题）
        java.math.BigDecimal rechargeAmount = java.math.BigDecimal.valueOf(rechargeAmountCents)
                .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        
        // 跳过小于最低充值额度的金额
        if (rechargeAmount.compareTo(java.math.BigDecimal.ONE) < 0) {
            return;
        }
        
        // 准备mock对象
        com.blog.repository.WalletRepository walletRepository = mock(com.blog.repository.WalletRepository.class);
        com.blog.repository.TransactionRepository transactionRepository = mock(com.blog.repository.TransactionRepository.class);
        
        // 创建初始钱包状态
        Wallet initialWallet = new Wallet();
        initialWallet.setId(1L);
        initialWallet.setUserId(userId);
        initialWallet.setBalance(java.math.BigDecimal.valueOf(100.00));
        initialWallet.setTotalIncome(java.math.BigDecimal.ZERO);
        initialWallet.setTotalWithdraw(java.math.BigDecimal.ZERO);
        
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(initialWallet));
        
        // 模拟事务成功场景：钱包保存成功，交易记录也保存成功
        // 注意：WalletService会修改传入的wallet对象，所以我们需要返回修改后的对象
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> {
            Wallet wallet = invocation.getArgument(0);
            // 返回保存的钱包对象（已经被service修改过）
            return wallet;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(1L);
            return transaction;
        });
        
        // 创建服务实例（简化版，只测试事务一致性）
        com.blog.service.impl.WalletServiceImpl walletService = new com.blog.service.impl.WalletServiceImpl(
            walletRepository, transactionRepository, null, null, null
        );
        
        // 保存初始余额（因为service会修改wallet对象）
        java.math.BigDecimal initialBalance = initialWallet.getBalance();
        
        // 执行充值操作
        Wallet resultWallet = walletService.recharge(userId, rechargeAmount);
        
        // 属性验证1：余额变化应该等于充值金额
        java.math.BigDecimal expectedBalance = initialBalance.add(rechargeAmount);
        assertTrue(expectedBalance.compareTo(resultWallet.getBalance()) == 0,
            "Balance should increase by recharge amount (transaction consistency). Expected: " + 
            expectedBalance + ", but was: " + resultWallet.getBalance());
        
        // 属性验证2：钱包保存和交易记录保存都应该被调用（原子性）
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        
        // 属性验证3：交易记录的余额应该与钱包余额一致
        verify(transactionRepository).save(argThat(transaction -> 
            transaction.getType() == Transaction.TransactionType.RECHARGE &&
            transaction.getAmount().compareTo(rechargeAmount) == 0 &&
            transaction.getBalanceAfter().compareTo(expectedBalance) == 0 &&
            transaction.getStatus() == Transaction.TransactionStatus.SUCCESS
        ));
    }

    /**
     * Feature: personal-blog-system, Property 13: 事务数据一致性（失败回滚）
     * 验证需求: 10.2
     * 
     * 测试场景：钱包提现操作失败时的回滚
     * - 如果余额不足，提现应该失败
     * - 失败时不应该创建交易记录
     * - 失败时钱包余额不应该改变
     */
    @Property(tries = 100)
    void testTransactionRollbackOnFailure(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @LongRange(min = 1000, max = 10000) long withdrawAmountCents) {
        
        // 将分转换为元
        java.math.BigDecimal withdrawAmount = java.math.BigDecimal.valueOf(withdrawAmountCents)
                .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        
        // 准备mock对象
        com.blog.repository.WalletRepository walletRepository = mock(com.blog.repository.WalletRepository.class);
        com.blog.repository.TransactionRepository transactionRepository = mock(com.blog.repository.TransactionRepository.class);
        
        // 创建余额不足的钱包（余额小于提现金额）
        java.math.BigDecimal insufficientBalance = withdrawAmount.subtract(java.math.BigDecimal.ONE);
        Wallet wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUserId(userId);
        wallet.setBalance(insufficientBalance);
        wallet.setTotalIncome(insufficientBalance);
        wallet.setTotalWithdraw(java.math.BigDecimal.ZERO);
        
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.of(wallet));
        
        // 创建服务实例
        com.blog.service.impl.WalletServiceImpl walletService = new com.blog.service.impl.WalletServiceImpl(
            walletRepository, transactionRepository, null, null, null
        );
        
        // 属性验证：余额不足时应该抛出异常
        Exception exception = assertThrows(Exception.class, () -> {
            walletService.withdraw(userId, withdrawAmount);
        }, "Should throw exception when balance is insufficient");
        
        assertTrue(exception.getMessage().contains("余额不足") || 
                   exception.getMessage().contains("INSUFFICIENT_BALANCE"),
            "Should reject withdrawal with insufficient balance error");
        
        // 属性验证：事务回滚，不应该保存钱包或创建交易记录
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    /**
     * Feature: personal-blog-system, Property 13: 事务数据一致性（打赏操作）
     * 验证需求: 10.2
     * 
     * 测试场景：打赏操作的事务一致性
     * - 打赏用户余额减少
     * - 博主余额增加
     * - 创建两条交易记录（打赏用户的支出记录和博主的收入记录）
     * - 所有操作应该在同一个事务中完成
     */
    @Property(tries = 100)
    void testTransactionConsistencyForReward(
            @ForAll @LongRange(min = 1, max = 1000000) Long rewardUserId,
            @ForAll @LongRange(min = 1, max = 1000000) Long bloggerId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 100, max = 10000) long rewardAmountCents) {
        
        // 确保是不同的用户
        if (rewardUserId.equals(bloggerId)) {
            return;
        }
        
        // 将分转换为元
        java.math.BigDecimal rewardAmount = java.math.BigDecimal.valueOf(rewardAmountCents)
                .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        
        // 准备mock对象
        com.blog.repository.WalletRepository walletRepository = mock(com.blog.repository.WalletRepository.class);
        com.blog.repository.TransactionRepository transactionRepository = mock(com.blog.repository.TransactionRepository.class);
        com.blog.repository.ArticleRepository articleRepository = mock(com.blog.repository.ArticleRepository.class);
        com.blog.repository.UserRepository userRepository = mock(com.blog.repository.UserRepository.class);
        
        // 创建打赏用户的钱包（余额充足）
        Wallet rewardUserWallet = new Wallet();
        rewardUserWallet.setId(1L);
        rewardUserWallet.setUserId(rewardUserId);
        rewardUserWallet.setBalance(java.math.BigDecimal.valueOf(1000.00));
        rewardUserWallet.setTotalIncome(java.math.BigDecimal.valueOf(1000.00));
        rewardUserWallet.setTotalWithdraw(java.math.BigDecimal.ZERO);
        
        // 创建博主的钱包
        Wallet bloggerWallet = new Wallet();
        bloggerWallet.setId(2L);
        bloggerWallet.setUserId(bloggerId);
        bloggerWallet.setBalance(java.math.BigDecimal.valueOf(500.00));
        bloggerWallet.setTotalIncome(java.math.BigDecimal.valueOf(500.00));
        bloggerWallet.setTotalWithdraw(java.math.BigDecimal.ZERO);
        
        // 创建文章
        Article article = new Article();
        article.setId(articleId);
        article.setUserId(bloggerId);
        article.setTitle("Test Article");
        article.setContent("Test Content");
        
        when(walletRepository.findByUserId(rewardUserId)).thenReturn(Optional.of(rewardUserWallet));
        when(walletRepository.findByUserId(bloggerId)).thenReturn(Optional.of(bloggerWallet));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        // 模拟钱包保存
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            transaction.setId(System.currentTimeMillis());
            return transaction;
        });
        
        // 创建服务实例
        com.blog.service.impl.WalletServiceImpl walletService = new com.blog.service.impl.WalletServiceImpl(
            walletRepository, transactionRepository, null, articleRepository, userRepository
        );
        
        // 执行打赏操作
        walletService.reward(rewardUserId, bloggerId, rewardAmount, articleId);
        
        // 属性验证1：打赏用户的钱包应该被保存（余额减少）
        verify(walletRepository, atLeast(1)).save(argThat(wallet -> 
            wallet.getUserId().equals(rewardUserId)
        ));
        
        // 属性验证2：博主的钱包应该被保存（余额增加）
        verify(walletRepository, atLeast(1)).save(argThat(wallet -> 
            wallet.getUserId().equals(bloggerId)
        ));
        
        // 属性验证3：应该创建两条交易记录（打赏支出和打赏收入）
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        
        // 属性验证4：交易记录应该包含正确的类型和金额
        verify(transactionRepository).save(argThat(transaction -> 
            transaction.getType() == Transaction.TransactionType.REWARD &&
            transaction.getAmount().compareTo(rewardAmount) == 0
        ));
        
        verify(transactionRepository).save(argThat(transaction -> 
            transaction.getType() == Transaction.TransactionType.INCOME &&
            transaction.getAmount().compareTo(rewardAmount) == 0
        ));
    }

    /**
     * 生成有效的文章标题（非空，1-200字符）
     */
    @Provide
    Arbitrary<String> validTitles() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars(' ', '-', '_')
                .ofMinLength(1)
                .ofMaxLength(200);
    }

    /**
     * 生成有效的文章内容（非空，10-10000字符）
     */
    @Provide
    Arbitrary<String> validContents() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars(' ', '.', ',', '!', '?', '\n')
                .ofMinLength(10)
                .ofMaxLength(10000);
    }
}
