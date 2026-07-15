package com.blog.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 * 配置认证、授权、CORS、CSRF等安全策略
 * 
 * 需求: 1.3 - 配置JWT认证
 * 需求: 1.9 - 配置权限验证规则
 * 需求: 12.4 - 支持跨域请求（CORS配置在CorsConfig中）
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置Security过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT不需要CSRF保护）
            .csrf().disable()
            
            // 配置CORS（使用CorsConfig中的配置）
            .cors()
            
            .and()
            // 配置会话管理为无状态
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            
            .and()
            // 配置异常处理
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            
            .and()
            // 配置请求授权规则
            .authorizeRequests()
            
            // 允许所有OPTIONS请求（CORS预检请求）
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            
            // 公开接口 - 认证相关
            .antMatchers("/auth/**").permitAll()
            
            // 公开接口 - 静态资源（上传的文件）
            .antMatchers("/files/**").permitAll()
            
            // 测试接口（临时）
            .antMatchers("/test/**").permitAll()
            
            // 文章接口 - 公开访问（GET请求）
            .antMatchers(HttpMethod.GET, "/articles", "/articles/**").permitAll()
            // 文章接口 - 需要认证（POST, PUT, DELETE）
            .antMatchers(HttpMethod.POST, "/articles/**").authenticated()
            .antMatchers(HttpMethod.PUT, "/articles/**").authenticated()
            .antMatchers(HttpMethod.DELETE, "/articles/**").authenticated()
            
            // 公开接口 - 分类和标签
            .antMatchers(HttpMethod.GET, "/categories/**").permitAll()
            .antMatchers(HttpMethod.GET, "/tags/**").permitAll()
            
            // 公开接口 - 评论查看（访客可访问）
            .antMatchers(HttpMethod.GET, "/comments/article/**").permitAll()
            .antMatchers(HttpMethod.GET, "/comments/*/replies").permitAll()
            
            // 公开接口 - 博主个人空间（访客可访问）
            .antMatchers(HttpMethod.GET, "/users/*/space").permitAll()
            
            // 公开接口 - 轮播图配置（访客可访问）
            .antMatchers(HttpMethod.GET, "/carousel").permitAll()
            
            // 管理员接口
            .antMatchers("/admin/**").hasRole("ADMIN")
            
            // 博主个人空间设置
            .antMatchers("/users/space/settings").hasAnyRole("BLOGGER", "ADMIN")
            
            // 私信接口 - 需要认证
            .antMatchers("/messages/**").authenticated()
            
            // 其他所有接口需要认证
            .anyRequest().authenticated();
        
        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
