package com.blog.service;

import com.blog.entity.Transaction;
import com.blog.entity.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

/**
 * 钱包管理服务接口
 * 需求: 17.1-17.5, 18.1-18.8, 30.1-30.5, 31.1-31.9
 */
public interface WalletService {

    /**
     * 充值
     * 需求: 30.1-30.5
     * 
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 更新后的钱包
     */
    Wallet recharge(Long userId, BigDecimal amount);

    /**
     * 提现
     * 需求: 18.4-18.8
     * 
     * @param userId 用户ID
     * @param amount 提现金额
     * @return 更新后的钱包
     */
    Wallet withdraw(Long userId, BigDecimal amount);

    /**
     * 打赏博主
     * 需求: 17.1-17.5
     * 
     * @param fromUserId 打赏用户ID
     * @param toUserId 博主ID
     * @param amount 打赏金额
     * @param articleId 文章ID（可选）
     * @return 打赏交易记录
     */
    Transaction reward(Long fromUserId, Long toUserId, BigDecimal amount, Long articleId);

    /**
     * 购买付费文章
     * 需求: 31.1-31.9
     * 
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 购买交易记录
     */
    Transaction purchaseArticle(Long userId, Long articleId);

    /**
     * 查询钱包余额
     * 需求: 18.1
     * 
     * @param userId 用户ID
     * @return 钱包信息
     */
    Wallet getBalance(Long userId);

    /**
     * 查询交易记录
     * 需求: 18.2, 30.5
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 交易记录分页列表
     */
    Page<Transaction> getTransactions(Long userId, Pageable pageable);

    /**
     * 查询收益明细
     * 需求: 18.3
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 收益交易记录分页列表
     */
    Page<Transaction> getRevenue(Long userId, Pageable pageable);

    /**
     * 创建或获取用户钱包
     * 
     * @param userId 用户ID
     * @return 钱包信息
     */
    Wallet getOrCreateWallet(Long userId);
}
