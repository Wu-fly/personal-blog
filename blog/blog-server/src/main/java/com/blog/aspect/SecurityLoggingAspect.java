package com.blog.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 安全事件日志切面
 * 记录安全相关的操作和异常
 */
@Aspect
@Component
public class SecurityLoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger("com.blog.security");
    
    /**
     * 定义切点：认证相关方法
     */
    @Pointcut("execution(* com.blog.service.impl.AuthServiceImpl.*(..))")
    public void authMethods() {}
    
    /**
     * 定义切点：安全服务方法
     */
    @Pointcut("execution(* com.blog.service.impl.SecurityServiceImpl.*(..))")
    public void securityMethods() {}
    
    /**
     * 定义切点：管理员操作方法
     */
    @Pointcut("execution(* com.blog.service.impl.AdminServiceImpl.*(..))")
    public void adminMethods() {}
    
    /**
     * 记录用户登录
     */
    @Before("execution(* com.blog.service.impl.AuthServiceImpl.login(..))")
    public void logLogin(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            String phone = args[0].toString();
            String maskedPhone = maskPhone(phone);
            String ip = getClientIp();
            logger.info("User login attempt - Phone: {}, IP: {}", maskedPhone, ip);
        }
    }
    
    /**
     * 记录用户注册
     */
    @Before("execution(* com.blog.service.impl.AuthServiceImpl.register(..))")
    public void logRegister(JoinPoint joinPoint) {
        String ip = getClientIp();
        logger.info("User registration attempt - IP: {}", ip);
    }
    
    /**
     * 记录敏感词检测
     */
    @Before("execution(* com.blog.service.impl.SecurityServiceImpl.checkContent(..))")
    public void logSensitiveWordCheck(JoinPoint joinPoint) {
        String ip = getClientIp();
        logger.info("Sensitive word check - IP: {}", ip);
    }
    
    /**
     * 记录SQL注入检测
     */
    @Before("execution(* com.blog.service.impl.SecurityServiceImpl.detectSqlInjection(..))")
    public void logSqlInjectionCheck(JoinPoint joinPoint) {
        String ip = getClientIp();
        logger.warn("SQL injection detection triggered - IP: {}", ip);
    }
    
    /**
     * 记录XSS攻击检测
     */
    @Before("execution(* com.blog.service.impl.SecurityServiceImpl.detectXss(..))")
    public void logXssCheck(JoinPoint joinPoint) {
        String ip = getClientIp();
        logger.warn("XSS attack detection triggered - IP: {}", ip);
    }
    
    /**
     * 记录管理员操作
     */
    @Before("adminMethods()")
    public void logAdminOperation(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        String ip = getClientIp();
        logger.info("Admin operation - Method: {}, IP: {}", methodName, ip);
    }
    
    /**
     * 记录安全异常
     */
    @AfterThrowing(pointcut = "authMethods() || securityMethods()", throwing = "ex")
    public void logSecurityException(JoinPoint joinPoint, Exception ex) {
        String methodName = joinPoint.getSignature().getName();
        String ip = getClientIp();
        logger.error("Security exception - Method: {}, IP: {}, Exception: {}", 
                methodName, ip, ex.getMessage());
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 脱敏手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
