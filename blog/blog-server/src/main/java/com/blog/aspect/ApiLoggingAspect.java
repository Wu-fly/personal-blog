package com.blog.aspect;

import com.blog.util.SensitiveDataMasker;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * API请求日志切面
 * 记录所有Controller层的API请求和响应
 */
@Aspect
@Component
public class ApiLoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingAspect.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 定义切点：所有Controller层的方法
     */
    @Pointcut("execution(* com.blog.controller..*(..))")
    public void controllerMethods() {}
    
    /**
     * 环绕通知：记录API请求和响应
     */
    @Around("controllerMethods()")
    public Object logApiRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        // 生成请求ID
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;
        
        // 记录请求开始时间
        long startTime = System.currentTimeMillis();
        
        // 记录请求信息
        if (request != null) {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            String remoteAddr = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 获取方法参数
            Object[] args = joinPoint.getArgs();
            String params = maskSensitiveData(args);
            
            logger.info("API Request - ID: {}, Method: {}, URI: {}, Query: {}, IP: {}, UserAgent: {}, Params: {}",
                    requestId, method, uri, queryString, remoteAddr, userAgent, params);
        }
        
        Object result = null;
        Exception exception = null;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录响应信息
            if (exception != null) {
                logger.error("API Response - ID: {}, Status: ERROR, Time: {}ms, Exception: {}",
                        requestId, executionTime, exception.getMessage());
            } else {
                String response = maskSensitiveData(result);
                logger.info("API Response - ID: {}, Status: SUCCESS, Time: {}ms, Response: {}",
                        requestId, executionTime, response);
            }
            
            // 清理MDC
            MDC.remove("requestId");
        }
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 脱敏敏感数据
     */
    private String maskSensitiveData(Object data) {
        if (data == null) {
            return "null";
        }
        
        try {
            String json = objectMapper.writeValueAsString(data);
            return SensitiveDataMasker.mask(json);
        } catch (Exception e) {
            return data.toString();
        }
    }
}
