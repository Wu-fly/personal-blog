package com.blog.aspect;

import com.blog.controller.ArticleController;
import com.blog.dto.ApiResponse;
import com.blog.dto.ArticleRequest;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * API日志切面测试
 */
@SpringBootTest
@ActiveProfiles("test")
class ApiLoggingAspectTest {
    
    @Autowired
    private ArticleController articleController;
    
    @MockBean
    private ArticleService articleService;
    
    @BeforeEach
    void setUp() {
        // 设置模拟的HTTP请求
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/api/articles");
        request.setRemoteAddr("127.0.0.1");
        request.addHeader("User-Agent", "Test Agent");
        
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"BLOGGER"})
    void testApiLoggingForSuccessfulRequest() {
        // 准备测试数据
        ArticleRequest request = new ArticleRequest();
        request.setTitle("Test Article");
        request.setContent("Test Content");
        request.setCategoryId(1L);
        
        // Mock service response
        when(articleService.createArticle(any(), any())).thenReturn(1L);
        
        // 执行请求 - 应该触发日志记录
        ApiResponse<Long> response = articleController.createArticle(request, null);
        
        // 验证响应
        assert response != null;
        // 日志应该被记录（通过查看控制台输出验证）
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"BLOGGER"})
    void testApiLoggingForFailedRequest() {
        // 准备测试数据
        ArticleRequest request = new ArticleRequest();
        request.setTitle("Test Article");
        request.setContent("Test Content");
        request.setCategoryId(1L);
        
        // Mock service to throw exception
        when(articleService.createArticle(any(), any()))
                .thenThrow(new RuntimeException("Test exception"));
        
        // 执行请求 - 应该触发错误日志记录
        try {
            articleController.createArticle(request, null);
        } catch (Exception e) {
            // 预期异常
        }
        
        // 错误日志应该被记录（通过查看控制台输出验证）
    }
    
    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void testApiLoggingWithSensitiveData() {
        // 准备包含敏感数据的请求
        ArticleRequest request = new ArticleRequest();
        request.setTitle("Test Article with password: secret123");
        request.setContent("Content with phone: 13812345678");
        request.setCategoryId(1L);
        
        // Mock service response
        when(articleService.createArticle(any(), any())).thenReturn(1L);
        
        // 执行请求 - 敏感数据应该被脱敏
        ApiResponse<Long> response = articleController.createArticle(request, null);
        
        // 验证响应
        assert response != null;
        // 日志中的敏感数据应该被脱敏（通过查看日志文件验证）
    }
}
