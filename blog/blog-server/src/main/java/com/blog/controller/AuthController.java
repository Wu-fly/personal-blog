package com.blog.controller;

import com.blog.annotation.RateLimit;
import com.blog.dto.ApiResponse;
import com.blog.dto.AuthResponse;
import com.blog.dto.LoginRequest;
import com.blog.dto.RegisterRequest;
import com.blog.dto.SendSmsRequest;
import com.blog.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证控制器
 * 处理用户注册、登录、短信验证、令牌管理
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册
     * POST /auth/register
     * 
     * @param request 注册请求
     * @return 认证响应（包含令牌和用户信息）
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("用户注册请求: phone={}, email={}", request.getPhone(), request.getEmail());
        
        try {
            AuthResponse response = authService.register(request);
            log.info("用户注册成功: userId={}, role={}", response.getUserId(), response.getRole());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("注册成功", response));
        } catch (Exception e) {
            log.error("用户注册失败: phone={}, error={}", request.getPhone(), e.getMessage());
            throw e;
        }
    }

    /**
     * 发送短信验证码
     * POST /auth/send-sms
     * 限流: 每小时最多3次
     * 
     * @param request 发送短信请求
     * @return 成功消息
     */
    @PostMapping("/send-sms")
    @RateLimit(maxCount = 3, duration = 1, unit = TimeUnit.HOURS, keyType = RateLimit.KeyType.PHONE, keyPrefix = "sms")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendSms(@Valid @RequestBody SendSmsRequest request) {
        log.info("发送短信验证码请求: phone={}", request.getPhone());
        
        try {
            String smsCode = authService.sendSmsCode(request.getPhone());
            log.info("短信验证码发送成功: phone={}", request.getPhone());
            
            // 测试环境返回验证码，生产环境不返回
            Map<String, String> data = new HashMap<>();
            data.put("message", "验证码已发送");
            if (smsCode != null) {
                data.put("smsCode", smsCode); // 仅测试环境
            }
            
            return ResponseEntity.ok(ApiResponse.success("验证码发送成功", data));
        } catch (Exception e) {
            log.error("短信验证码发送失败: phone={}, error={}", request.getPhone(), e.getMessage());
            throw e;
        }
    }

    /**
     * 用户登录
     * POST /auth/login
     * 限流: 每分钟最多5次
     * 
     * @param request 登录请求
     * @return 认证响应（包含令牌和用户信息）
     */
    @PostMapping("/login")
    @RateLimit(maxCount = 5, duration = 1, unit = TimeUnit.MINUTES, keyType = RateLimit.KeyType.IP, keyPrefix = "login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("用户登录请求: phone={}", request.getPhone());
        
        try {
            AuthResponse response = authService.login(request);
            log.info("用户登录成功: userId={}, role={}", response.getUserId(), response.getRole());
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
        } catch (Exception e) {
            log.error("用户登录失败: phone={}, error={}", request.getPhone(), e.getMessage());
            throw e;
        }
    }

    /**
     * 用户登出
     * POST /auth/logout
     * 
     * 注意: 由于使用JWT无状态认证，登出主要由前端处理（删除本地令牌）
     * 
     * @return 成功消息
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        log.info("用户登出请求");
        
        // 从Authorization头中提取令牌
        String token = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        
        log.info("用户登出成功");
        return ResponseEntity.ok(ApiResponse.success("登出成功", null));
    }

    /**
     * 刷新令牌
     * POST /auth/refresh
     * 
     * @return 新令牌
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, String>>> refreshToken(
            @RequestHeader("Authorization") String authorization) {
        log.info("刷新令牌请求");
        
        try {
            // 从Authorization头中提取令牌
            String oldToken = authorization.substring(7); // 移除 "Bearer " 前缀
            
            String newToken = authService.refreshToken(oldToken);
            log.info("令牌刷新成功");
            
            Map<String, String> data = new HashMap<>();
            data.put("accessToken", newToken);
            data.put("tokenType", "Bearer");
            
            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", data));
        } catch (Exception e) {
            log.error("令牌刷新失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 密码登录（支持所有用户）
     * POST /auth/admin/login
     * 
     * @param request 包含phone/email和password的登录请求
     * @return 认证响应（包含令牌和用户信息）
     */
    @PostMapping("/admin/login")
    @RateLimit(maxCount = 10, duration = 1, unit = TimeUnit.MINUTES, keyType = RateLimit.KeyType.IP, keyPrefix = "password-login")
    public ResponseEntity<ApiResponse<AuthResponse>> adminLogin(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String email = request.get("email");
        String password = request.get("password");
        
        log.info("Password login request: phone={}, email={}", phone, email);
        
        try {
            AuthResponse response = authService.adminLogin(phone, email, password);
            log.info("User logged in successfully: userId={}, role={}", response.getUserId(), response.getRole());
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
            
        } catch (Exception e) {
            log.error("Password login failed: error={}", e.getMessage());
            throw e;
        }
    }
}
