package com.blog.service;

import com.blog.dto.AuthResponse;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.UserRepository;
import com.blog.security.JwtUtil;
import com.blog.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService单元测试
 * 测试用户认证服务的核心功能
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SmsService smsService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        // 准备注册请求
        registerRequest = new RegisterRequest();
        registerRequest.setPhone("13800138000");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Test123456");
        registerRequest.setSmsCode("123456");

        // 准备登录请求
        loginRequest = new LoginRequest();
        loginRequest.setPhone("13800138000");
        loginRequest.setSmsCode("123456");

        // 准备测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setNickname("13800138000");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
    }

    @Test
    void testRegisterSuccess() {
        // 准备mock行为
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(smsService.verifySmsCode(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-token");

        // 执行注册
        AuthResponse response = authService.register(registerRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("test-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("USER", response.getRole());

        // 验证方法调用
        verify(userRepository).existsByPhone("13800138000");
        verify(userRepository).existsByEmail("test@example.com");
        verify(smsService).verifySmsCode("13800138000", "123456");
        verify(passwordEncoder).encode("Test123456");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken(1L, "13800138000", "USER");
    }

    @Test
    void testRegisterPhoneAlreadyExists() {
        // 准备mock行为 - 手机号已存在
        when(userRepository.existsByPhone(anyString())).thenReturn(true);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("手机号已被注册", exception.getMessage());
        verify(userRepository).existsByPhone("13800138000");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        // 准备mock行为 - 邮箱已存在
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("邮箱已被注册", exception.getMessage());
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterInvalidSmsCode() {
        // 准备mock行为 - 验证码错误
        when(userRepository.existsByPhone(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(smsService.verifySmsCode(anyString(), anyString())).thenReturn(false);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("验证码错误或已过期", exception.getMessage());
        verify(smsService).verifySmsCode("13800138000", "123456");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        // 准备mock行为
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.of(testUser));
        when(smsService.verifySmsCode(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-token");

        // 执行登录
        AuthResponse response = authService.login(loginRequest);

        // 验证结果
        assertNotNull(response);
        assertEquals("test-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(1L, response.getUserId());
        assertEquals("USER", response.getRole());

        // 验证方法调用
        verify(userRepository).findByPhone("13800138000");
        verify(smsService).verifySmsCode("13800138000", "123456");
        verify(jwtUtil).generateToken(1L, "13800138000", "USER");
    }

    @Test
    void testLoginPhoneNotRegistered() {
        // 准备mock行为 - 手机号未注册
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("账号不存在", exception.getMessage());
        verify(userRepository).findByPhone("13800138000");
        verify(smsService, never()).verifySmsCode(anyString(), anyString());
    }

    @Test
    void testLoginAccountDisabled() {
        // 准备mock行为 - 账号被禁用
        testUser.setStatus(User.UserStatus.DISABLED);
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.of(testUser));

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("账号已被禁用", exception.getMessage());
        verify(userRepository).findByPhone("13800138000");
        verify(smsService, never()).verifySmsCode(anyString(), anyString());
    }

    @Test
    void testLoginInvalidSmsCode() {
        // 准备mock行为 - 验证码错误
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.of(testUser));
        when(smsService.verifySmsCode(anyString(), anyString())).thenReturn(false);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("验证码错误或已过期", exception.getMessage());
        verify(smsService).verifySmsCode("13800138000", "123456");
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString());
    }

    @Test
    void testRefreshTokenSuccess() {
        // 准备mock行为
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.refreshToken(anyString())).thenReturn("new-token");

        // 执行刷新令牌
        String newToken = authService.refreshToken("old-token");

        // 验证结果
        assertNotNull(newToken);
        assertEquals("new-token", newToken);

        // 验证方法调用
        verify(jwtUtil).validateToken("old-token");
        verify(jwtUtil).refreshToken("old-token");
    }

    @Test
    void testRefreshTokenInvalid() {
        // 准备mock行为 - 令牌无效
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        // 执行并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.refreshToken("invalid-token");
        });

        assertEquals("令牌无效或已过期", exception.getMessage());
        verify(jwtUtil).validateToken("invalid-token");
        verify(jwtUtil, never()).refreshToken(anyString());
    }

    @Test
    void testSendSmsCode() {
        // 准备mock行为
        when(smsService.sendSmsCode(anyString())).thenReturn("123456");

        // 执行发送验证码
        String code = authService.sendSmsCode("13800138000");

        // 验证结果
        assertNotNull(code);
        assertEquals("123456", code);

        // 验证方法调用
        verify(smsService).sendSmsCode("13800138000");
    }
}
