package com.blog.controller;

import com.blog.dto.ApiResponse;
import com.blog.entity.Transaction;
import com.blog.entity.Wallet;
import com.blog.security.JwtUtil;
import com.blog.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 钱包管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final JwtUtil jwtUtil;

    /**
     * 获取钱包余额和统计信息
     */
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBalance(
            @RequestHeader("Authorization") String token) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        
        Wallet wallet = walletService.getBalance(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("balance", wallet.getBalance());
        result.put("totalIncome", wallet.getTotalIncome());
        result.put("totalWithdraw", wallet.getTotalWithdraw());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 充值
     */
    @PostMapping("/recharge")
    public ResponseEntity<ApiResponse<Map<String, Object>>> recharge(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        Wallet wallet = walletService.recharge(userId, amount);
        
        // 返回简化的数据，避免懒加载问题
        Map<String, Object> result = new HashMap<>();
        result.put("balance", wallet.getBalance());
        result.put("totalIncome", wallet.getTotalIncome());
        result.put("totalWithdraw", wallet.getTotalWithdraw());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 提现
     */
    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<Map<String, Object>>> withdraw(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        
        Wallet wallet = walletService.withdraw(userId, amount);
        
        // 返回简化的数据，避免懒加载问题
        Map<String, Object> result = new HashMap<>();
        result.put("balance", wallet.getBalance());
        result.put("totalIncome", wallet.getTotalIncome());
        result.put("totalWithdraw", wallet.getTotalWithdraw());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 获取交易记录
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getTransactions(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = walletService.getTransactions(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * 获取收益明细
     */
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<Page<Transaction>>> getRevenue(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> revenue = walletService.getRevenue(userId, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(revenue));
    }

    /**
     * 购买付费文章
     */
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<Transaction>> purchaseArticle(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Long articleId = Long.valueOf(request.get("articleId").toString());
        
        Transaction transaction = walletService.purchaseArticle(userId, articleId);
        
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }

    /**
     * 打赏博主
     */
    @PostMapping("/reward")
    public ResponseEntity<ApiResponse<Transaction>> reward(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {
        Long userId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
        Long toUserId = Long.valueOf(request.get("toUserId").toString());
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        Long articleId = request.containsKey("articleId") ? 
            Long.valueOf(request.get("articleId").toString()) : null;
        
        Transaction transaction = walletService.reward(userId, toUserId, amount, articleId);
        
        return ResponseEntity.ok(ApiResponse.success(transaction));
    }
}
