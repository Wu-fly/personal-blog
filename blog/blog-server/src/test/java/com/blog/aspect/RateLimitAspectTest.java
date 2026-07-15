package com.blog.aspect;

import com.blog.annotation.RateLimit;
import com.blog.exception.BusinessException;
import com.blog.util.RateLimiter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitAspect
 */
@ExtendWith(MockitoExtension.class)
class RateLimitAspectTest {

    @Mock
    private RateLimiter rateLimiter;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private RateLimitAspect rateLimitAspect;

    @BeforeEach
    void setUp() {
        rateLimitAspect = new RateLimitAspect(rateLimiter);
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void testCheckRateLimit_Allowed_ShouldPass() throws NoSuchMethodException {
        // Arrange
        Method method = TestController.class.getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getName()).thenReturn("testMethod");
        when(rateLimiter.isAllowed(anyString(), eq(5), eq(1L), eq(TimeUnit.MINUTES)))
                .thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act & Assert
        assertDoesNotThrow(() -> rateLimitAspect.checkRateLimit(joinPoint));
        verify(rateLimiter).isAllowed(anyString(), eq(5), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    void testCheckRateLimit_Denied_ShouldThrowException() throws NoSuchMethodException {
        // Arrange
        Method method = TestController.class.getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getName()).thenReturn("testMethod");
        when(rateLimiter.isAllowed(anyString(), eq(5), eq(1L), eq(TimeUnit.MINUTES)))
                .thenReturn(false);
        when(rateLimiter.getRemainingRequests(anyString(), eq(5), eq(1L), eq(TimeUnit.MINUTES)))
                .thenReturn(0L);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act & Assert
        BusinessException exception = assertThrows(BusinessException.class,
                () -> rateLimitAspect.checkRateLimit(joinPoint));
        assertEquals("请求过于频繁，请稍后再试", exception.getMessage());
    }

    @Test
    void testCheckRateLimit_WithXForwardedFor_ShouldUseCorrectIp() throws NoSuchMethodException {
        // Arrange
        Method method = TestController.class.getMethod("testMethod");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getName()).thenReturn("testMethod");
        when(rateLimiter.isAllowed(contains("10.0.0.1"), eq(5), eq(1L), eq(TimeUnit.MINUTES)))
                .thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "10.0.0.1, 192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        assertDoesNotThrow(() -> rateLimitAspect.checkRateLimit(joinPoint));

        // Assert
        verify(rateLimiter).isAllowed(contains("10.0.0.1"), eq(5), eq(1L), eq(TimeUnit.MINUTES));
    }

    @Test
    void testCheckRateLimit_WithUserKeyType_ShouldUseUserId() throws NoSuchMethodException {
        // Arrange
        Method method = TestController.class.getMethod("testMethodWithUserKey");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getName()).thenReturn("testMethodWithUserKey");
        when(rateLimiter.isAllowed(contains("user123"), eq(10), eq(1L), eq(TimeUnit.SECONDS)))
                .thenReturn(true);

        // Set up authentication
        Authentication auth = new UsernamePasswordAuthenticationToken("user123", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        assertDoesNotThrow(() -> rateLimitAspect.checkRateLimit(joinPoint));

        // Assert
        verify(rateLimiter).isAllowed(contains("user123"), eq(10), eq(1L), eq(TimeUnit.SECONDS));
    }

    @Test
    void testCheckRateLimit_WithPhoneKeyType_ShouldUsePhone() throws NoSuchMethodException {
        // Arrange
        Method method = TestController.class.getMethod("testMethodWithPhoneKey");
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getName()).thenReturn("testMethodWithPhoneKey");
        
        TestRequest testRequest = new TestRequest();
        testRequest.setPhone("13800138000");
        when(joinPoint.getArgs()).thenReturn(new Object[]{testRequest});
        
        when(rateLimiter.isAllowed(contains("13800138000"), eq(3), eq(1L), eq(TimeUnit.HOURS)))
                .thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // Act
        assertDoesNotThrow(() -> rateLimitAspect.checkRateLimit(joinPoint));

        // Assert
        verify(rateLimiter).isAllowed(contains("13800138000"), eq(3), eq(1L), eq(TimeUnit.HOURS));
    }

    // Test controller class with rate limit annotations
    static class TestController {
        @RateLimit(maxCount = 5, duration = 1, unit = TimeUnit.MINUTES)
        public void testMethod() {
        }

        @RateLimit(maxCount = 10, duration = 1, unit = TimeUnit.SECONDS, keyType = RateLimit.KeyType.USER)
        public void testMethodWithUserKey() {
        }

        @RateLimit(maxCount = 3, duration = 1, unit = TimeUnit.HOURS, keyType = RateLimit.KeyType.PHONE)
        public void testMethodWithPhoneKey() {
        }
    }

    // Test request class
    static class TestRequest {
        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
