package com.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 交易记录实体类
 * 对应数据库表: transactions
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class Transaction extends BaseEntity {

    /**
     * 钱包ID
     */
    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    /**
     * 钱包（多对一关系）
     * 使用JsonIgnore避免序列化时的懒加载问题
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", insertable = false, updatable = false)
    private Wallet wallet;

    /**
     * 交易类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    /**
     * 交易金额
     */
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    /**
     * 交易后余额
     */
    @Column(name = "balance_after", precision = 10, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    /**
     * 关联用户ID（打赏、购买时记录对方用户）
     */
    @Column(name = "related_user_id")
    private Long relatedUserId;

    /**
     * 关联文章ID（打赏、购买时记录文章）
     */
    @Column(name = "related_article_id")
    private Long relatedArticleId;

    /**
     * 交易状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.SUCCESS;

    /**
     * 交易描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 交易类型枚举
     */
    public enum TransactionType {
        RECHARGE,  // 充值
        WITHDRAW,  // 提现
        REWARD,    // 打赏
        PURCHASE,  // 购买
        INCOME     // 收入
    }

    /**
     * 交易状态枚举
     */
    public enum TransactionStatus {
        PENDING,   // 处理中
        SUCCESS,   // 成功
        FAILED     // 失败
    }
}
