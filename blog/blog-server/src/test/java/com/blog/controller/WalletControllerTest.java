package com.blog.controller;

import com.blog.dto.RechargeRequest;
import com.blog.dto.WithdrawRequest;
import com.blog.entity.Transaction;
import com.blog.entity.Wallet;
import com.blog.security.CustomUserDetails;
import com.blog.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * WalletController单元测试
 * 需求: 18.1-18.8, 30.1-30.5
 */
@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WalletService walletService;

    private CustomUserDetails userDetails;
    private Wallet wallet;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(1L, "13800138000", "password", "USER", "ACTIVE");
        
        wallet = new Wallet();
        wallet.setId(1L);
        wallet.setUserId(1L);
        wallet.setBalance(new BigDecimal("100.00"));
        wallet.setTotalIncome(new BigDecimal("500.00"));
        wallet.setTotalWithdraw(new BigDecimal("400.00"));
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        
        transaction = new Transaction();
        transaction.setId(1L);
        transaction.setWalletId(1L);
        transaction.setType(Transaction.TransactionType.RECHARGE);
        transaction.setAmount(new BigDecimal("50.00"));
        transaction.setBalanceAfter(new BigDecimal("150.00"));
        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setDescription("充值");
        transaction.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("充值成功")
    @WithMockUser
    void testRecharge_Success() throws Exception {
        // Given
        RechargeRequest request = new RechargeRequest(new BigDecimal("50.00"));
        when(walletService.recharge(eq(1L), any(BigDecimal.class))).thenReturn(wallet);

        // When & Then
        mockMvc.perform(post("/api/wallet/recharge")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("充值成功"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.balance").value(100.00));
    }

    @Test
    @DisplayName("充值失败 - 金额为空")
    @WithMockUser
    void testRecharge_NullAmount() throws Exception {
        // Given
        RechargeRequest request = new RechargeRequest(null);

        // When & Then
        mockMvc.perform(post("/api/wallet/recharge")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("充值失败 - 金额小于最低限额")
    @WithMockUser
    void testRecharge_AmountTooSmall() throws Exception {
        // Given
        RechargeRequest request = new RechargeRequest(new BigDecimal("0.00"));

        // When & Then
        mockMvc.perform(post("/api/wallet/recharge")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("提现成功")
    @WithMockUser
    void testWithdraw_Success() throws Exception {
        // Given
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("30.00"));
        when(walletService.withdraw(eq(1L), any(BigDecimal.class))).thenReturn(wallet);

        // When & Then
        mockMvc.perform(post("/api/wallet/withdraw")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("提现申请已提交"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.balance").value(100.00));
    }

    @Test
    @DisplayName("提现失败 - 金额为空")
    @WithMockUser
    void testWithdraw_NullAmount() throws Exception {
        // Given
        WithdrawRequest request = new WithdrawRequest(null);

        // When & Then
        mockMvc.perform(post("/api/wallet/withdraw")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("提现失败 - 金额小于最低限额")
    @WithMockUser
    void testWithdraw_AmountTooSmall() throws Exception {
        // Given
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("0.00"));

        // When & Then
        mockMvc.perform(post("/api/wallet/withdraw")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取钱包余额成功")
    @WithMockUser
    void testGetBalance_Success() throws Exception {
        // Given
        when(walletService.getBalance(1L)).thenReturn(wallet);

        // When & Then
        mockMvc.perform(get("/api/wallet/balance")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.balance").value(100.00))
                .andExpect(jsonPath("$.data.totalIncome").value(500.00))
                .andExpect(jsonPath("$.data.totalWithdraw").value(400.00));
    }

    @Test
    @DisplayName("获取交易记录成功")
    @WithMockUser
    void testGetTransactions_Success() throws Exception {
        // Given
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> page = new PageImpl<>(transactions);
        when(walletService.getTransactions(eq(1L), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/wallet/transactions")
                        .with(user(userDetails))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].type").value("RECHARGE"))
                .andExpect(jsonPath("$.data.content[0].amount").value(50.00))
                .andExpect(jsonPath("$.data.content[0].balanceAfter").value(150.00));
    }

    @Test
    @DisplayName("获取交易记录 - 使用默认分页参数")
    @WithMockUser
    void testGetTransactions_DefaultPagination() throws Exception {
        // Given
        List<Transaction> transactions = Arrays.asList(transaction);
        Page<Transaction> page = new PageImpl<>(transactions);
        when(walletService.getTransactions(eq(1L), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/wallet/transactions")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("获取收益明细成功 - 博主权限")
    @WithMockUser(roles = "BLOGGER")
    void testGetRevenue_Success() throws Exception {
        // Given
        transaction.setType(Transaction.TransactionType.INCOME);
        transaction.setDescription("付费文章收益");
        List<Transaction> revenue = Arrays.asList(transaction);
        Page<Transaction> page = new PageImpl<>(revenue);
        when(walletService.getRevenue(eq(1L), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/wallet/revenue")
                        .with(user(new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE")))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].type").value("INCOME"))
                .andExpect(jsonPath("$.data.content[0].amount").value(50.00));
    }

    @Test
    @DisplayName("获取收益明细成功 - 管理员权限")
    @WithMockUser(roles = "ADMIN")
    void testGetRevenue_AdminAccess() throws Exception {
        // Given
        transaction.setType(Transaction.TransactionType.INCOME);
        List<Transaction> revenue = Arrays.asList(transaction);
        Page<Transaction> page = new PageImpl<>(revenue);
        when(walletService.getRevenue(eq(1L), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/wallet/revenue")
                        .with(user(new CustomUserDetails(1L, "13800138000", "password", "ADMIN", "ACTIVE")))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取收益明细 - 使用默认分页参数")
    @WithMockUser(roles = "BLOGGER")
    void testGetRevenue_DefaultPagination() throws Exception {
        // Given
        List<Transaction> revenue = Arrays.asList(transaction);
        Page<Transaction> page = new PageImpl<>(revenue);
        when(walletService.getRevenue(eq(1L), any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/wallet/revenue")
                        .with(user(new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("未登录用户无法访问充值接口")
    void testRecharge_Unauthorized() throws Exception {
        // Given
        RechargeRequest request = new RechargeRequest(new BigDecimal("50.00"));

        // When & Then
        mockMvc.perform(post("/api/wallet/recharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("未登录用户无法访问提现接口")
    void testWithdraw_Unauthorized() throws Exception {
        // Given
        WithdrawRequest request = new WithdrawRequest(new BigDecimal("30.00"));

        // When & Then
        mockMvc.perform(post("/api/wallet/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("未登录用户无法访问余额接口")
    void testGetBalance_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wallet/balance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("未登录用户无法访问交易记录接口")
    void testGetTransactions_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wallet/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("未登录用户无法访问收益明细接口")
    void testGetRevenue_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wallet/revenue"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("分页参数验证 - 页码不能为负数")
    @WithMockUser
    void testGetTransactions_InvalidPageNumber() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wallet/transactions")
                        .with(user(userDetails))
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("分页参数验证 - 每页大小不能小于1")
    @WithMockUser
    void testGetTransactions_InvalidPageSize() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/wallet/transactions")
                        .with(user(userDetails))
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}
