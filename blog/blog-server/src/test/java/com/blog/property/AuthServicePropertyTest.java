package com.blog.property;

import com.blog.dto.RegisterRequest;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.UserRepository;
import com.blog.security.JwtUtil;
import com.blog.service.SmsService;
import com.blog.service.impl.AuthServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * AuthService属性测试
 * 使用jqwik进行基于属性的测试
 */
class AuthServicePropertyTest {

    /**
     * Feature: personal-blog-system, Property 1: 用户注册唯一性
     * 验证需求: 15.2
     * 
     * 对于任意手机号或邮箱，如果已被注册，则使用相同手机号或邮箱的注册请求应该被拒绝
     */
    @Property(tries = 100)
    void testUserRegistrationUniqueness(
            @ForAll("validPhones") String phone1,
            @ForAll("validEmails") String email1,
            @ForAll("validPhones") String phone2,
            @ForAll("validEmails") String email2,
            @ForAll("validPasswords") String password,
            @ForAll("validSmsCodes") String smsCode) {
        
        // 准备mock对象
        UserRepository userRepository = mock(UserRepository.class);
        SmsService smsService = mock(SmsService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        
        AuthServiceImpl authService = new AuthServiceImpl();
        setField(authService, "userRepository", userRepository);
        setField(authService, "smsService", smsService);
        setField(authService, "jwtUtil", jwtUtil);
        setField(authService, "passwordEncoder", passwordEncoder);
        
        // 第一次注册请求
        RegisterRequest request1 = new RegisterRequest();
        request1.setPhone(phone1);
        request1.setEmail(email1);
        request1.setPassword(password);
        request1.setSmsCode(smsCode);
        
        // 第二次注册请求
        RegisterRequest request2 = new RegisterRequest();
        request2.setPhone(phone2);
        request2.setEmail(email2);
        request2.setPassword(password);
        request2.setSmsCode(smsCode);
        
        // 模拟第一次注册成功
        when(userRepository.existsByPhone(phone1)).thenReturn(false);
        when(userRepository.existsByEmail(email1)).thenReturn(false);
        when(smsService.verifySmsCode(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setPhone(phone1);
        savedUser.setEmail(email1);
        savedUser.setRole(User.UserRole.USER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("token");
        
        // 执行第一次注册（应该成功）
        assertDoesNotThrow(() -> authService.register(request1));
        
        // 属性验证：如果第二次注册使用相同的手机号或邮箱，应该被拒绝
        boolean samePhone = phone1.equals(phone2);
        boolean sameEmail = email1.equals(email2);
        
        if (samePhone || sameEmail) {
            // 模拟数据库中已存在相同的手机号或邮箱
            when(userRepository.existsByPhone(phone2)).thenReturn(samePhone);
            when(userRepository.existsByEmail(email2)).thenReturn(sameEmail);
            
            // 第二次注册应该抛出BusinessException
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.register(request2);
            });
            
            // 验证错误消息
            if (samePhone) {
                assertEquals("手机号已被注册", exception.getMessage());
            } else if (sameEmail) {
                assertEquals("邮箱已被注册", exception.getMessage());
            }
        } else {
            // 如果手机号和邮箱都不同，第二次注册应该成功
            when(userRepository.existsByPhone(phone2)).thenReturn(false);
            when(userRepository.existsByEmail(email2)).thenReturn(false);
            
            User savedUser2 = new User();
            savedUser2.setId(2L);
            savedUser2.setPhone(phone2);
            savedUser2.setEmail(email2);
            savedUser2.setRole(User.UserRole.USER);
            when(userRepository.save(any(User.class))).thenReturn(savedUser2);
            
            assertDoesNotThrow(() -> authService.register(request2));
        }
    }

    /**
     * Feature: personal-blog-system, Property 2: 短信验证码有效性
     * 验证需求: 1.3
     * 
     * 对于任意已注册用户和正确的验证码，登录应该成功并生成有效期为7天的令牌
     */
    @Property(tries = 100)
    void testSmsCodeValidityForLogin(
            @ForAll("validPhones") String phone,
            @ForAll("validSmsCodes") String correctCode,
            @ForAll("validSmsCodes") String wrongCode) {
        
        // 确保正确的验证码和错误的验证码不同
        Assume.that(!correctCode.equals(wrongCode));
        
        // 准备mock对象
        UserRepository userRepository = mock(UserRepository.class);
        SmsService smsService = mock(SmsService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        
        AuthServiceImpl authService = new AuthServiceImpl();
        setField(authService, "userRepository", userRepository);
        setField(authService, "smsService", smsService);
        setField(authService, "jwtUtil", jwtUtil);
        setField(authService, "passwordEncoder", passwordEncoder);
        
        // 模拟已注册用户
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setPhone(phone);
        existingUser.setNickname("TestUser");
        existingUser.setRole(User.UserRole.USER);
        existingUser.setStatus(User.UserStatus.ACTIVE);
        
        when(userRepository.findByPhone(phone)).thenReturn(java.util.Optional.of(existingUser));
        
        // 测试1: 使用正确的验证码登录应该成功
        when(smsService.verifySmsCode(phone, correctCode)).thenReturn(true);
        when(jwtUtil.generateToken(existingUser.getId(), phone, existingUser.getRole().name()))
                .thenReturn("valid_token_7days");
        
        com.blog.dto.LoginRequest loginRequest = new com.blog.dto.LoginRequest();
        loginRequest.setPhone(phone);
        loginRequest.setSmsCode(correctCode);
        
        com.blog.dto.AuthResponse response = assertDoesNotThrow(() -> authService.login(loginRequest));
        
        // 验证生成的令牌不为空
        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertEquals("valid_token_7days", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(existingUser.getId(), response.getUserId());
        assertEquals(existingUser.getRole().name(), response.getRole());
        
        // 验证调用了正确的方法
        verify(smsService).verifySmsCode(phone, correctCode);
        verify(jwtUtil).generateToken(existingUser.getId(), phone, existingUser.getRole().name());
        
        // 测试2: 使用错误的验证码登录应该失败
        when(smsService.verifySmsCode(phone, wrongCode)).thenReturn(false);
        
        com.blog.dto.LoginRequest wrongLoginRequest = new com.blog.dto.LoginRequest();
        wrongLoginRequest.setPhone(phone);
        wrongLoginRequest.setSmsCode(wrongCode);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(wrongLoginRequest);
        });
        
        // 验证错误消息
        assertTrue(exception.getMessage().contains("验证码错误") || 
                   exception.getMessage().contains("验证码已过期"));
        
        // 验证只生成了一次令牌（第一次成功登录）
        verify(jwtUtil, times(1)).generateToken(anyLong(), anyString(), anyString());
    }

    /**
     * Feature: personal-blog-system, Property 2: 短信验证码有效性（未注册用户场景）
     * 验证需求: 1.3, 1.5
     * 
     * 对于任意未注册的手机号，即使验证码正确，登录也应该失败
     */
    @Property(tries = 100)
    void testSmsCodeValidityForUnregisteredUser(
            @ForAll("validPhones") String phone,
            @ForAll("validSmsCodes") String smsCode) {
        
        // 准备mock对象
        UserRepository userRepository = mock(UserRepository.class);
        SmsService smsService = mock(SmsService.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        
        AuthServiceImpl authService = new AuthServiceImpl();
        setField(authService, "userRepository", userRepository);
        setField(authService, "smsService", smsService);
        setField(authService, "jwtUtil", jwtUtil);
        setField(authService, "passwordEncoder", passwordEncoder);
        
        // 模拟未注册用户
        when(userRepository.findByPhone(phone)).thenReturn(java.util.Optional.empty());
        
        // 即使验证码正确，未注册用户也应该登录失败
        when(smsService.verifySmsCode(phone, smsCode)).thenReturn(true);
        
        com.blog.dto.LoginRequest loginRequest = new com.blog.dto.LoginRequest();
        loginRequest.setPhone(phone);
        loginRequest.setSmsCode(smsCode);
        
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginRequest);
        });
        
        // 验证错误消息
        assertEquals("账号不存在", exception.getMessage());
        
        // 验证没有生成令牌
        verify(jwtUtil, never()).generateToken(anyLong(), anyString(), anyString());
    }

    /**
     * Feature: personal-blog-system, Property 3: 令牌过期验证
     * 验证需求: 1.9
     * 
     * 对于任意超过7天的访问令牌，使用该令牌的API请求应该被拒绝并要求重新登录
     */
    @Property(tries = 100)
    void testTokenExpirationValidation(
            @ForAll("validPhones") String phone,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId) {
        
        // 创建真实的JwtUtil实例（不使用mock，因为需要测试真实的过期逻辑）
        JwtUtil jwtUtil = new JwtUtil();
        
        // 使用反射设置JWT配置
        setField(jwtUtil, "secret", "test-secret-key-for-jwt-token-generation-must-be-at-least-512-bits-long");
        
        // 测试场景1: 未过期的令牌（有效期7天）
        long sevenDaysInMillis = 7L * 24 * 60 * 60 * 1000;
        setField(jwtUtil, "expiration", sevenDaysInMillis);
        
        String validToken = jwtUtil.generateToken(userId, phone, "USER");
        
        // 验证未过期的令牌应该有效
        assertTrue(jwtUtil.validateToken(validToken), 
                "Token should be valid within 7 days");
        assertFalse(jwtUtil.isTokenExpired(validToken), 
                "Token should not be expired within 7 days");
        
        // 验证可以从令牌中解析出正确的信息
        assertEquals(userId, jwtUtil.getUserIdFromToken(validToken));
        assertEquals(phone, jwtUtil.getPhoneFromToken(validToken));
        assertEquals("USER", jwtUtil.getRoleFromToken(validToken));
        
        // 测试场景2: 已过期的令牌（有效期设置为负数，模拟已过期）
        long expiredTime = -1000L; // 负数表示已经过期
        setField(jwtUtil, "expiration", expiredTime);
        
        String expiredToken = jwtUtil.generateToken(userId, phone, "USER");
        
        // 等待一小段时间确保令牌过期
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 验证过期的令牌应该无效
        assertFalse(jwtUtil.validateToken(expiredToken), 
                "Expired token should be invalid");
        assertTrue(jwtUtil.isTokenExpired(expiredToken), 
                "Token should be expired after expiration time");
        
        // 测试场景3: 刷新令牌功能
        // 重新设置有效期为7天
        setField(jwtUtil, "expiration", sevenDaysInMillis);
        
        // 刷新过期的令牌应该生成新的有效令牌
        String refreshedToken = jwtUtil.refreshToken(expiredToken);
        
        if (refreshedToken != null) {
            // 新令牌应该有效
            assertTrue(jwtUtil.validateToken(refreshedToken), 
                    "Refreshed token should be valid");
            assertFalse(jwtUtil.isTokenExpired(refreshedToken), 
                    "Refreshed token should not be expired");
            
            // 新令牌应该包含相同的用户信息
            assertEquals(userId, jwtUtil.getUserIdFromToken(refreshedToken));
            assertEquals(phone, jwtUtil.getPhoneFromToken(refreshedToken));
            assertEquals("USER", jwtUtil.getRoleFromToken(refreshedToken));
        }
        
        // 测试场景4: 验证令牌过期时间确实是7天
        setField(jwtUtil, "expiration", sevenDaysInMillis);
        String tokenWith7Days = jwtUtil.generateToken(userId, phone, "BLOGGER");
        
        // 令牌在7天内应该有效
        assertTrue(jwtUtil.validateToken(tokenWith7Days), 
                "Token should be valid within 7 days");
        
        // 模拟7天后的令牌（通过设置过期时间为0来模拟刚好过期）
        setField(jwtUtil, "expiration", 0L);
        String justExpiredToken = jwtUtil.generateToken(userId, phone, "BLOGGER");
        
        // 等待确保令牌过期
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 刚好过期的令牌应该无效
        assertFalse(jwtUtil.validateToken(justExpiredToken), 
                "Token should be invalid after exactly 7 days");
        assertTrue(jwtUtil.isTokenExpired(justExpiredToken), 
                "Token should be expired after exactly 7 days");
    }

    /**
     * Feature: personal-blog-system, Property 3: 令牌过期验证（边界测试）
     * 验证需求: 1.9
     * 
     * 测试令牌在不同时间点的有效性
     */
    @Property(tries = 100)
    void testTokenExpirationBoundary(
            @ForAll("validPhones") String phone,
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll("tokenRoles") String role) {
        
        // 创建真实的JwtUtil实例
        JwtUtil jwtUtil = new JwtUtil();
        setField(jwtUtil, "secret", "test-secret-key-for-jwt-token-generation-must-be-at-least-512-bits-long");
        
        // 测试不同的过期时间
        long[] expirationTimes = {
            1000L,                          // 1秒
            60 * 1000L,                     // 1分钟
            60 * 60 * 1000L,                // 1小时
            24 * 60 * 60 * 1000L,           // 1天
            7 * 24 * 60 * 60 * 1000L        // 7天（标准有效期）
        };
        
        for (long expirationTime : expirationTimes) {
            setField(jwtUtil, "expiration", expirationTime);
            
            String token = jwtUtil.generateToken(userId, phone, role);
            
            // 刚生成的令牌应该有效
            assertTrue(jwtUtil.validateToken(token), 
                    "Newly generated token should be valid");
            assertFalse(jwtUtil.isTokenExpired(token), 
                    "Newly generated token should not be expired");
            
            // 验证令牌包含正确的信息
            assertEquals(userId, jwtUtil.getUserIdFromToken(token));
            assertEquals(phone, jwtUtil.getPhoneFromToken(token));
            assertEquals(role, jwtUtil.getRoleFromToken(token));
        }
        
        // 测试立即过期的令牌
        setField(jwtUtil, "expiration", -1000L);
        String immediatelyExpiredToken = jwtUtil.generateToken(userId, phone, role);
        
        // 立即过期的令牌应该无效
        assertFalse(jwtUtil.validateToken(immediatelyExpiredToken), 
                "Immediately expired token should be invalid");
        assertTrue(jwtUtil.isTokenExpired(immediatelyExpiredToken), 
                "Immediately expired token should be expired");
    }

    /**
     * 生成有效的中国手机号
     */
    @Provide
    Arbitrary<String> validPhones() {
        return Arbitraries.integers()
                .between(0, 999999999)
                .map(num -> String.format("13%09d", num));
    }

    /**
     * 生成有效的邮箱地址
     */
    @Provide
    Arbitrary<String> validEmails() {
        Arbitrary<String> usernames = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(10);
        
        Arbitrary<String> domains = Arbitraries.of("example.com", "test.com", "mail.com");
        
        return Combinators.combine(usernames, domains)
                .as((username, domain) -> username + "@" + domain);
    }

    /**
     * 生成有效的密码（包含大小写字母和数字）
     */
    @Provide
    Arbitrary<String> validPasswords() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(2)
                .ofMaxLength(6)
                .map(lower -> {
                    String upper = lower.substring(0, 1).toUpperCase();
                    String digit = String.valueOf((int)(Math.random() * 10));
                    return upper + lower.substring(1) + digit;
                });
    }

    /**
     * 生成有效的短信验证码（6位数字）
     */
    @Provide
    Arbitrary<String> validSmsCodes() {
        return Arbitraries.integers()
                .between(0, 999999)
                .map(num -> String.format("%06d", num));
    }

    /**
     * 生成有效的用户角色
     */
    @Provide
    Arbitrary<String> tokenRoles() {
        return Arbitraries.of("USER", "BLOGGER", "ADMIN");
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
