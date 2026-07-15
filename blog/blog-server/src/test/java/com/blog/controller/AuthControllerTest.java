package com.blog.controller;

import com.blog.dto.AuthResponse;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.dto.SendSmsRequest;
import com.blog.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController集成测试
 * 验证所有认证接口的正确性
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        mockAuthResponse = AuthResponse.builder()
                .accessToken("mock-jwt-token")
                .tokenType("Bearer")
                .userId(1L)
                .role("USER")
                .nickname("测试用户")
                .avatar("https://example.com/avatar.jpg")
                .build();
    }

    @Test
    void testRegister_Success() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setEmail("test@example.com");
        request.setPassword("Test123456");
        request.setSmsCode("123456");

        when(authService.register(any(RegisterRequest.class))).thenReturn(mockAuthResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("注册成功"))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.role").value("USER"));
    }

    @Test
    void testRegister_InvalidPhone() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setPhone("invalid-phone");
        request.setEmail("test@example.com");
        request.setPassword("Test123456");
        request.setSmsCode("123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_InvalidEmail() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setEmail("invalid-email");
        request.setPassword("Test123456");
        request.setSmsCode("123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegister_WeakPassword() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setPhone("13800138000");
        request.setEmail("test@example.com");
        request.setPassword("weak");
        request.setSmsCode("123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSendSms_Success() throws Exception {
        // Arrange
        SendSmsRequest request = new SendSmsRequest();
        request.setPhone("13800138000");

        when(authService.sendSmsCode(anyString())).thenReturn("123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/send-sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("验证码发送成功"))
                .andExpect(jsonPath("$.data.message").value("验证码已发送"));
    }

    @Test
    void testSendSms_InvalidPhone() throws Exception {
        // Arrange
        SendSmsRequest request = new SendSmsRequest();
        request.setPhone("invalid-phone");

        // Act & Assert
        mockMvc.perform(post("/api/auth/send-sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setPhone("13800138000");
        request.setSmsCode("123456");

        when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登录成功"))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.userId").value(1));
    }

    @Test
    void testLogin_InvalidPhone() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setPhone("invalid-phone");
        request.setSmsCode("123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_InvalidSmsCode() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setPhone("13800138000");
        request.setSmsCode("invalid");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogout_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    void testLogout_WithoutToken() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登出成功"));
    }

    @Test
    void testRefreshToken_Success() throws Exception {
        // Arrange
        when(authService.refreshToken(anyString())).thenReturn("new-mock-jwt-token");

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .header("Authorization", "Bearer old-mock-jwt-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("令牌刷新成功"))
                .andExpect(jsonPath("$.data.accessToken").value("new-mock-jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    void testRefreshToken_MissingAuthorizationHeader() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isBadRequest());
    }
}
