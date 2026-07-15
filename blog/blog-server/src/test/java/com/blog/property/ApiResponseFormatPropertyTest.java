package com.blog.property;

import com.blog.controller.*;
import com.blog.dto.*;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.entity.Wallet;
import com.blog.exception.BusinessException;
import com.blog.security.CustomUserDetails;
import com.blog.service.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.StringLength;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * API响应格式统一属性测试
 * 使用jqwik进行基于属性的测试
 * 
 * Feature: personal-blog-system, Property 16: API响应格式统一
 * 验证需求: 12.2, 12.3
 */
class ApiResponseFormatPropertyTest {

    private final ObjectMapper objectMapper;
    
    public ApiResponseFormatPropertyTest() {
        this.objectMapper = new ObjectMapper();
        // 配置ObjectMapper以支持Java 8日期时间类型
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - 成功响应格式
     * 验证需求: 12.2
     * 
     * 对于任意API调用成功，系统应该返回统一格式的JSON响应，包含状态码和数据
     */
    @Property(tries = 100)
    void testSuccessResponseFormat_AuthController(
            @ForAll("validPhones") String phone,
            @ForAll("validEmails") String email,
            @ForAll("validPasswords") String password,
            @ForAll("validSmsCode") String smsCode) throws Exception {
        
        // 模拟AuthService
        AuthService authService = mock(AuthService.class);
        AuthResponse mockAuthResponse = AuthResponse.builder()
                .accessToken("mock-token")
                .tokenType("Bearer")
                .userId(1L)
                .role("USER")
                .nickname("测试用户")
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockAuthResponse);
        
        // 创建控制器
        AuthController controller = new AuthController(authService);
        
        // 创建注册请求
        RegisterRequest request = new RegisterRequest();
        request.setPhone(phone);
        request.setEmail(email);
        request.setPassword(password);
        request.setSmsCode(smsCode);
        
        // 调用API
        ResponseEntity<ApiResponse<AuthResponse>> response = controller.register(request);
        
        // 属性验证：成功响应应该包含统一格式
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        ApiResponse<AuthResponse> apiResponse = response.getBody();
        
        // 验证必需字段
        assertTrue(apiResponse.isSuccess(), "Success field should be true for successful response");
        assertNotNull(apiResponse.getMessage(), "Message field should not be null");
        assertNotNull(apiResponse.getData(), "Data field should not be null for successful response");
        assertNotNull(apiResponse.getTimestamp(), "Timestamp field should not be null");
        
        // 验证数据内容
        assertNotNull(apiResponse.getData().getAccessToken(), "Access token should be present in data");
        assertNotNull(apiResponse.getData().getUserId(), "User ID should be present in data");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - 文章列表成功响应
     * 验证需求: 12.2
     * 
     * 对于任意文章列表查询成功，系统应该返回统一格式的JSON响应
     */
    @Property(tries = 100)
    void testSuccessResponseFormat_ArticleController(
            @ForAll @LongRange(min = 1, max = 100) Long categoryId,
            @ForAll @LongRange(min = 0, max = 10) int page,
            @ForAll @LongRange(min = 1, max = 50) int size) throws Exception {
        
        // Skip invalid parameters (page must be >= 0, size must be >= 1)
        Assume.that(page >= 0 && size >= 1);
        
        // 模拟ArticleService
        ArticleService articleService = mock(ArticleService.class);
        Article mockArticle = new Article();
        mockArticle.setId(1L);
        mockArticle.setTitle("测试文章");
        mockArticle.setContent("测试内容");
        mockArticle.setUserId(1L);
        
        Page<Article> mockPage = new PageImpl<>(
                Collections.singletonList(mockArticle),
                PageRequest.of(page, size),
                1L
        );
        when(articleService.getArticles(anyLong(), any(), any(), any())).thenReturn(mockPage);
        
        // 创建控制器
        ArticleController controller = new ArticleController(articleService);
        
        // 调用API
        ResponseEntity<ApiResponse<Map<String, Object>>> response = 
                controller.getArticles(categoryId, null, null, "createdAt", page, size);
        
        // 属性验证：成功响应应该包含统一格式
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        ApiResponse<Map<String, Object>> apiResponse = response.getBody();
        
        // 验证必需字段
        assertTrue(apiResponse.isSuccess(), "Success field should be true for successful response");
        assertNotNull(apiResponse.getData(), "Data field should not be null for successful response");
        assertNotNull(apiResponse.getTimestamp(), "Timestamp field should not be null");
        
        // 验证分页数据结构
        Map<String, Object> data = apiResponse.getData();
        assertTrue(data.containsKey("content"), "Data should contain 'content' field");
        assertTrue(data.containsKey("totalElements"), "Data should contain 'totalElements' field");
        assertTrue(data.containsKey("totalPages"), "Data should contain 'totalPages' field");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - 钱包操作成功响应
     * 验证需求: 12.2
     * 
     * 对于任意钱包操作成功，系统应该返回统一格式的JSON响应
     */
    @Property(tries = 100)
    void testSuccessResponseFormat_WalletController(
            @ForAll @LongRange(min = 1, max = 10000) Long amount) throws Exception {
        
        // 模拟WalletService
        WalletService walletService = mock(WalletService.class);
        Wallet mockWallet = new Wallet();
        mockWallet.setId(1L);
        mockWallet.setUserId(1L);
        mockWallet.setBalance(java.math.BigDecimal.valueOf(amount));
        mockWallet.setTotalIncome(java.math.BigDecimal.valueOf(amount));
        
        when(walletService.getBalance(anyLong())).thenReturn(mockWallet);
        
        // 创建控制器
        WalletController controller = new WalletController(walletService);
        
        // 创建用户详情
        CustomUserDetails userDetails = new CustomUserDetails(
                1L, "13800138000", "password", "USER", "ACTIVE"
        );
        
        // 调用API
        ResponseEntity<ApiResponse<WalletResponse>> response = 
                controller.getBalance(userDetails);
        
        // 属性验证：成功响应应该包含统一格式
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        
        ApiResponse<WalletResponse> apiResponse = response.getBody();
        
        // 验证必需字段
        assertTrue(apiResponse.isSuccess(), "Success field should be true for successful response");
        assertNotNull(apiResponse.getData(), "Data field should not be null for successful response");
        assertNotNull(apiResponse.getTimestamp(), "Timestamp field should not be null");
        
        // 验证数据内容
        assertNotNull(apiResponse.getData().getUserId(), "User ID should be present in data");
        assertNotNull(apiResponse.getData().getBalance(), "Balance should be present in data");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - 错误响应格式
     * 验证需求: 12.3
     * 
     * 对于任意API调用失败，系统应该返回统一格式的错误响应，包含错误码和错误信息
     */
    @Property(tries = 100)
    void testErrorResponseFormat_BusinessException(
            @ForAll("errorCodes") String errorCode,
            @ForAll("errorMessages") String errorMessage) throws Exception {
        
        // 创建业务异常
        BusinessException exception = new BusinessException(errorCode, errorMessage);
        
        // 创建错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error(errorCode, errorMessage);
        
        // 属性验证：错误响应应该包含统一格式
        assertNotNull(errorResponse, "Error response should not be null");
        assertFalse(errorResponse.isSuccess(), "Success field should be false for error response");
        assertNotNull(errorResponse.getErrorCode(), "Error code should not be null");
        assertNotNull(errorResponse.getMessage(), "Error message should not be null");
        assertNotNull(errorResponse.getTimestamp(), "Timestamp should not be null");
        
        // 验证错误码和错误信息
        assertEquals(errorCode, errorResponse.getErrorCode(), 
                "Error code should match the exception error code");
        assertEquals(errorMessage, errorResponse.getMessage(), 
                "Error message should match the exception message");
        
        // 验证数据字段为null（错误响应通常不包含数据）
        assertNull(errorResponse.getData(), "Data field should be null for error response");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - 验证错误响应格式
     * 验证需求: 12.3
     * 
     * 对于任意验证失败，系统应该返回统一格式的错误响应
     */
    @Property(tries = 100)
    void testErrorResponseFormat_ValidationError() throws Exception {
        
        // 创建验证错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error("VALIDATION_ERROR", "Validation failed");
        
        // 属性验证：验证错误响应应该包含统一格式
        assertNotNull(errorResponse, "Error response should not be null");
        assertFalse(errorResponse.isSuccess(), "Success field should be false for validation error");
        assertEquals("VALIDATION_ERROR", errorResponse.getErrorCode(), 
                "Error code should be VALIDATION_ERROR");
        assertNotNull(errorResponse.getMessage(), "Error message should not be null");
        assertNotNull(errorResponse.getTimestamp(), "Timestamp should not be null");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - 认证错误响应格式
     * 验证需求: 12.3
     * 
     * 对于任意认证失败，系统应该返回统一格式的错误响应
     */
    @Property(tries = 100)
    void testErrorResponseFormat_AuthenticationError() throws Exception {
        
        // 创建认证错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error("UNAUTHORIZED", "Authentication failed");
        
        // 属性验证：认证错误响应应该包含统一格式
        assertNotNull(errorResponse, "Error response should not be null");
        assertFalse(errorResponse.isSuccess(), "Success field should be false for auth error");
        assertEquals("UNAUTHORIZED", errorResponse.getErrorCode(), 
                "Error code should be UNAUTHORIZED");
        assertEquals("Authentication failed", errorResponse.getMessage(), 
                "Error message should match");
        assertNotNull(errorResponse.getTimestamp(), "Timestamp should not be null");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - 权限错误响应格式
     * 验证需求: 12.3
     * 
     * 对于任意权限不足，系统应该返回统一格式的错误响应
     */
    @Property(tries = 100)
    void testErrorResponseFormat_ForbiddenError() throws Exception {
        
        // 创建权限错误响应
        ApiResponse<Object> errorResponse = ApiResponse.error("FORBIDDEN", "Access denied - insufficient permissions");
        
        // 属性验证：权限错误响应应该包含统一格式
        assertNotNull(errorResponse, "Error response should not be null");
        assertFalse(errorResponse.isSuccess(), "Success field should be false for forbidden error");
        assertEquals("FORBIDDEN", errorResponse.getErrorCode(), 
                "Error code should be FORBIDDEN");
        assertTrue(errorResponse.getMessage().contains("Access denied"), 
                "Error message should indicate access denied");
        assertNotNull(errorResponse.getTimestamp(), "Timestamp should not be null");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - 资源不存在错误响应格式
     * 验证需求: 12.3
     * 
     * 对于任意资源不存在，系统应该返回统一格式的错误响应
     */
    @Property(tries = 100)
    void testErrorResponseFormat_NotFoundError(
            @ForAll @LongRange(min = 1, max = 999999) Long resourceId) throws Exception {
        
        // 创建资源不存在错误响应
        String message = "Resource not found with id: " + resourceId;
        ApiResponse<Object> errorResponse = ApiResponse.error("NOT_FOUND", message);
        
        // 属性验证：资源不存在错误响应应该包含统一格式
        assertNotNull(errorResponse, "Error response should not be null");
        assertFalse(errorResponse.isSuccess(), "Success field should be false for not found error");
        assertEquals("NOT_FOUND", errorResponse.getErrorCode(), 
                "Error code should be NOT_FOUND");
        assertTrue(errorResponse.getMessage().contains("not found"), 
                "Error message should indicate resource not found");
        assertNotNull(errorResponse.getTimestamp(), "Timestamp should not be null");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - JSON序列化验证
     * 验证需求: 12.2, 12.3
     * 
     * 对于任意API响应，序列化为JSON后应该包含所有必需字段
     */
    @Property(tries = 100)
    void testResponseJsonSerialization_SuccessResponse(
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content) throws Exception {
        
        // 创建成功响应（使用Java 8兼容的Map创建方式）
        Map<String, String> data = new java.util.HashMap<>();
        data.put("title", title);
        data.put("content", content);
        ApiResponse<Map<String, String>> response = ApiResponse.success("操作成功", data);
        
        // 序列化为JSON
        String json = objectMapper.writeValueAsString(response);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // 属性验证：JSON应该包含所有必需字段
        assertTrue(jsonNode.has("success"), "JSON should have 'success' field");
        assertTrue(jsonNode.has("message"), "JSON should have 'message' field");
        assertTrue(jsonNode.has("data"), "JSON should have 'data' field");
        assertTrue(jsonNode.has("timestamp"), "JSON should have 'timestamp' field");
        
        // 验证字段值
        assertTrue(jsonNode.get("success").asBoolean(), "success should be true");
        assertEquals("操作成功", jsonNode.get("message").asText(), "message should match");
        assertNotNull(jsonNode.get("data"), "data should not be null");
        assertNotNull(jsonNode.get("timestamp"), "timestamp should not be null");
    }

    /**
     * Feature: personal-blog-system, Property 16: API响应格式统一 - JSON序列化验证（错误响应）
     * 验证需求: 12.3
     * 
     * 对于任意错误响应，序列化为JSON后应该包含所有必需字段
     */
    @Property(tries = 100)
    void testResponseJsonSerialization_ErrorResponse(
            @ForAll("errorCodes") String errorCode,
            @ForAll("errorMessages") String errorMessage) throws Exception {
        
        // 创建错误响应
        ApiResponse<Object> response = ApiResponse.error(errorCode, errorMessage);
        
        // 序列化为JSON
        String json = objectMapper.writeValueAsString(response);
        JsonNode jsonNode = objectMapper.readTree(json);
        
        // 属性验证：JSON应该包含所有必需字段
        assertTrue(jsonNode.has("success"), "JSON should have 'success' field");
        assertTrue(jsonNode.has("message"), "JSON should have 'message' field");
        assertTrue(jsonNode.has("errorCode"), "JSON should have 'errorCode' field");
        assertTrue(jsonNode.has("timestamp"), "JSON should have 'timestamp' field");
        
        // 验证字段值
        assertFalse(jsonNode.get("success").asBoolean(), "success should be false");
        assertEquals(errorMessage, jsonNode.get("message").asText(), "message should match");
        assertEquals(errorCode, jsonNode.get("errorCode").asText(), "errorCode should match");
        assertNotNull(jsonNode.get("timestamp"), "timestamp should not be null");
        
        // 验证data字段为null或不存在
        if (jsonNode.has("data")) {
            assertTrue(jsonNode.get("data").isNull(), "data should be null for error response");
        }
    }

    /**
     * 生成有效的手机号
     */
    @Provide
    Arbitrary<String> validPhones() {
        return Arbitraries.of(
                "13800138000",
                "13900139000",
                "15800158000",
                "18600186000",
                "17700177000"
        );
    }

    /**
     * 生成有效的邮箱
     */
    @Provide
    Arbitrary<String> validEmails() {
        return Arbitraries.of(
                "test@example.com",
                "user@test.com",
                "admin@blog.com",
                "blogger@site.com",
                "demo@mail.com"
        );
    }

    /**
     * 生成有效的密码
     */
    @Provide
    Arbitrary<String> validPasswords() {
        return Arbitraries.of(
                "Test123456",
                "Pass@word1",
                "Secure123!",
                "MyPass2023",
                "Strong#Pass1"
        );
    }

    /**
     * 生成有效的短信验证码
     */
    @Provide
    Arbitrary<String> validSmsCode() {
        return Arbitraries.strings()
                .numeric()
                .ofLength(6);
    }

    /**
     * 生成错误码
     */
    @Provide
    Arbitrary<String> errorCodes() {
        return Arbitraries.of(
                "VALIDATION_ERROR",
                "UNAUTHORIZED",
                "FORBIDDEN",
                "NOT_FOUND",
                "BUSINESS_ERROR",
                "INTERNAL_ERROR",
                "INSUFFICIENT_BALANCE",
                "DUPLICATE_ENTRY",
                "INVALID_CREDENTIALS"
        );
    }

    /**
     * 生成错误消息
     */
    @Provide
    Arbitrary<String> errorMessages() {
        return Arbitraries.of(
                "Validation failed",
                "Authentication required",
                "Access denied",
                "Resource not found",
                "Operation failed",
                "Internal server error",
                "Insufficient balance",
                "Duplicate entry",
                "Invalid credentials"
        );
    }

    /**
     * 生成有效的文章标题
     */
    @Provide
    Arbitrary<String> validTitles() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars("中文测试标题")
                .ofMinLength(1)
                .ofMaxLength(200)
                .filter(s -> s != null && !s.trim().isEmpty());
    }

    /**
     * 生成有效的内容
     */
    @Provide
    Arbitrary<String> validContents() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars("中文测试内容。\n")
                .ofMinLength(1)
                .ofMaxLength(1000)
                .filter(s -> s != null && !s.trim().isEmpty());
    }
}
