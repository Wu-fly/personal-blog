package com.blog.repository;

import com.blog.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Transaction Repository
 * Handles database operations for Transaction entity
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    /**
     * Find transactions by wallet ID
     */
    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);
    
    /**
     * Find transactions by wallet ID and type
     */
    Page<Transaction> findByWalletIdAndTypeOrderByCreatedAtDesc(Long walletId, Transaction.TransactionType type, Pageable pageable);
    
    /**
     * Find transactions by type
     */
    Page<Transaction> findByType(Transaction.TransactionType type, Pageable pageable);
    
    /**
     * Find income transactions for a user (rewards and article purchases)
     */
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId AND t.type IN ('REWARD', 'INCOME') ORDER BY t.createdAt DESC")
    Page<Transaction> findIncomeTransactions(@Param("walletId") Long walletId, Pageable pageable);
    
    /**
     * Find all transactions for a user
     */
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId ORDER BY t.createdAt DESC")
    List<Transaction> findAllByWalletId(@Param("walletId") Long walletId);
    
    /**
     * Find transactions by wallet ID and created date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.walletId = :walletId AND t.createdAt >= :startDate AND t.createdAt < :endDate")
    List<Transaction> findByWalletIdAndCreatedAtBetween(@Param("walletId") Long walletId, 
                                                         @Param("startDate") java.time.LocalDateTime startDate,
                                                         @Param("endDate") java.time.LocalDateTime endDate);
}
