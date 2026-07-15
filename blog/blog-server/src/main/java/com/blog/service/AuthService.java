package com.blog.service;

import com.blog.dto.AuthResponse;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;

/**
 * 认证服务接口
 * 需求: 1.1-1.9, 15.1-15.8
 */
public interface AuthService {

    /**
     * 用户注册
     * 需求: 15.1-15.8
     * 
     * @param request 注册请求
     * @return 认证响应（包含令牌和用户信息）
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 发送短信验证码
     * 需求: 1.2
     * 
     * @param phone 手机号
     * @return 验证码（测试环境返回，生产环境不返回）
     */
    String sendSmsCode(String phone);

    /**
     * 用户登录
     * 需求: 1.1-1.9
     * 
     * @param request 登录请求
     * @return 认证响应（包含令牌和用户信息）
     */
    AuthResponse login(LoginRequest request);

    /**
     * 刷新令牌
     * 需求: 1.9
     * 
     * @param token 旧令牌
     * @return 新令牌
     */
    String refreshToken(String token);

    /**
     * 生成管理员令牌
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @return JWT令牌
     */
    String generateAdminToken(Long userId, String username);

    /**
     * 管理员登录
     * 
     * @param phone 手机号
     * @param email 邮箱
     * @param password 密码
     * @return 认证响应
     */
    AuthResponse adminLogin(String phone, String email, String password);
}
