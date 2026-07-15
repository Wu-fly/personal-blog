package com.blog.aspect;

import com.blog.config.QueryOptimizationConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Slow Query Logging Aspect
 * 
 * Monitors repository method execution time and logs slow queries.
 * Helps identify performance bottlenecks in database operations.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class SlowQueryLoggingAspect {
    
    private final QueryOptimizationConfig.QueryOptimizationProperties properties;
    
    /**
     * Monitor all repository method executions
     */
    @Around("execution(* com.blog.repository..*(..))")
    public Object logSlowQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        // Skip if slow query logging is disabled
        if (!properties.isEnableSlowQueryLog()) {
            return joinPoint.proceed();
        }
        
        String methodName = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the repository method
            Object result = joinPoint.proceed();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log if execution time exceeds threshold
            if (executionTime > properties.getSlowQueryThreshold()) {
                log.warn("SLOW QUERY DETECTED: {} took {}ms (threshold: {}ms)", 
                         methodName, executionTime, properties.getSlowQueryThreshold());
                
                // Log method arguments for debugging
                Object[] args = joinPoint.getArgs();
                if (args != null && args.length > 0) {
                    log.warn("Method arguments: {}", formatArguments(args));
                }
            } else {
                log.debug("Query executed: {} in {}ms", methodName, executionTime);
            }
            
            return result;
            
        } catch (Throwable e) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Query failed: {} after {}ms - Error: {}", 
                      methodName, executionTime, e.getMessage());
            throw e;
        }
    }
    
    /**
     * Format method arguments for logging
     */
    private String formatArguments(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            
            Object arg = args[i];
            if (arg == null) {
                sb.append("null");
            } else {
                // Truncate long strings
                String argStr = arg.toString();
                if (argStr.length() > 100) {
                    sb.append(argStr.substring(0, 100)).append("...");
                } else {
                    sb.append(argStr);
                }
            }
        }
        return sb.toString();
    }
}
