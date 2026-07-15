package com.blog.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * 钱包实体类
 * 对应数据库表: wallets
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "wallets")
public class Wallet extends BaseEntity {

    /**
     * 用户ID（唯一）
     */
    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    /**
     * 用户（一对一关系）
     * 使用JsonIgnore避免序列化时的懒加载问题
     */
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    /**
     * 当前余额
     */
    @Column(name = "balance", precision = 10, scale = 2, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * 总收入
     */
    @Column(name = "total_income", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalIncome = BigDecimal.ZERO;

    /**
     * 总提现
     */
    @Column(name = "total_withdraw", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalWithdraw = BigDecimal.ZERO;
}
