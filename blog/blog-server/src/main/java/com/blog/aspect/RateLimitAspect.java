package com.blog.aspect;

import com.blog.annotation.RateLimit;
import com.blog.exception.BusinessException;
import com.blog.util.RateLimiter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Rate limit aspect to intercept and enforce rate limits on API endpoints
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimiter rateLimiter;

    public RateLimitAspect(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Before("@annotation(com.blog.annotation.RateLimit)")
    public void checkRateLimit(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        if (rateLimit == null) {
            return;
        }

        String key = generateKey(rateLimit, joinPoint);
        boolean allowed = rateLimiter.isAllowed(
                key,
                rateLimit.maxCount(),
                rateLimit.duration(),
                rateLimit.unit()
        );

        if (!allowed) {
            long remaining = rateLimiter.getRemainingRequests(
                    key,
                    rateLimit.maxCount(),
                    rateLimit.duration(),
                    rateLimit.unit()
            );
            
            logger.warn("Rate limit exceeded for key: {}, method: {}", key, method.getName());
            throw new BusinessException("请求过于频繁，请稍后再试");
        }

        logger.debug("Rate limit check passed for key: {}, method: {}", key, method.getName());
    }

    private String generateKey(RateLimit rateLimit, JoinPoint joinPoint) {
        String prefix = rateLimit.keyPrefix().isEmpty() ? 
                joinPoint.getSignature().getName() : rateLimit.keyPrefix();

        switch (rateLimit.keyType()) {
            case IP:
                return prefix + ":" + getClientIp();
            case USER:
                return prefix + ":" + getCurrentUserId();
            case PHONE:
                return prefix + ":" + getPhoneFromRequest(joinPoint);
            case CUSTOM:
                return prefix + ":" + getCustomKey(joinPoint);
            default:
                return prefix + ":" + getClientIp();
        }
    }

    private String getClientIp() {
        ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "anonymous";
    }

    private String getPhoneFromRequest(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                try {
                    Method getPhone = arg.getClass().getMethod("getPhone");
                    Object phone = getPhone.invoke(arg);
                    if (phone != null) {
                        return phone.toString();
                    }
                } catch (Exception e) {
                    // Ignore if getPhone method doesn't exist
                }
            }
        }
        return getClientIp();
    }

    private String getCustomKey(JoinPoint joinPoint) {
        // For custom keys, use the first string argument or fall back to IP
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof String) {
                return (String) arg;
            }
        }
        return getClientIp();
    }
}
