package com.blog.repository;

import com.blog.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Wallet Repository
 * Handles database operations for Wallet entity
 */
@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    /**
     * Find wallet by user ID
     */
    Optional<Wallet> findByUserId(Long userId);
    
    /**
     * Check if wallet exists for user
     */
    boolean existsByUserId(Long userId);
    
    /**
     * Update wallet balance
     */
    @Modifying
    @Query("UPDATE Wallet w SET w.balance = w.balance + :amount WHERE w.userId = :userId")
    void updateBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * Update total income
     */
    @Modifying
    @Query("UPDATE Wallet w SET w.totalIncome = w.totalIncome + :amount WHERE w.userId = :userId")
    void updateTotalIncome(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    
    /**
     * Update total withdraw
     */
    @Modifying
    @Query("UPDATE Wallet w SET w.totalWithdraw = w.totalWithdraw + :amount WHERE w.userId = :userId")
    void updateTotalWithdraw(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
