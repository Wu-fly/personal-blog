package com.blog.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 拦截所有请求，验证JWT令牌并设置认证信息
 * 
 * 需求: 1.3 - 验证访问令牌
 * 需求: 1.9 - 检查令牌是否过期
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${jwt.header}")
    private String tokenHeader;

    @Value("${jwt.prefix}")
    private String tokenPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 跳过静态资源路径
        String path = request.getRequestURI();
        if (path.startsWith("/files/")) {
            log.debug("Skipping JWT filter for static resource: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            // 从请求头中获取JWT令牌
            String token = getTokenFromRequest(request);
            
            if (token != null && jwtUtil.validateToken(token)) {
                // 令牌有效，解析用户信息
                Long userId = jwtUtil.getUserIdFromToken(token);
                String phone = jwtUtil.getPhoneFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                if (userId != null && phone != null && role != null) {
                    // 创建CustomUserDetails对象
                    CustomUserDetails userDetails = new CustomUserDetails(
                            userId,
                            phone,
                            null,  // password不需要
                            role,
                            "ACTIVE"  // 假设token有效则用户状态为ACTIVE
                    );
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,  // 使用CustomUserDetails作为principal
                                    null, 
                                    userDetails.getAuthorities()
                            );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 设置认证信息到SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Set authentication for user: {} with role: {}", phone, role);
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取JWT令牌
     * 
     * @param request HTTP请求
     * @return JWT令牌字符串，如果不存在则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(tokenHeader);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(tokenPrefix)) {
            // 移除"Bearer "前缀
            return bearerToken.substring(tokenPrefix.length()).trim();
        }
        
        return null;
    }
}
