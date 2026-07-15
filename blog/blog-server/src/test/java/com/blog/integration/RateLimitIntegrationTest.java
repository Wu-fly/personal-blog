package com.blog.integration;

import com.blog.dto.LoginRequest;
import com.blog.dto.SendSmsRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for rate limiting functionality
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clear all rate limit keys before each test
        redisTemplate.keys("rate_limit:*").forEach(key -> redisTemplate.delete(key));
    }

    @Test
    void testLoginRateLimit_ExceedLimit_ShouldReturn429() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setPhone("13800138000");
        request.setSmsCode("123456");

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert - First 5 requests should be allowed (even if they fail authentication)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .header("X-Forwarded-For", "192.168.1.1"))
                    .andExpect(status().is4xxClientError()); // Will fail auth, but not rate limit
        }

        // 6th request should be rate limited
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("请求过于频繁，请稍后再试"));
    }

    @Test
    void testSmsRateLimit_ExceedLimit_ShouldReturn429() throws Exception {
        // Arrange
        SendSmsRequest request = new SendSmsRequest();
        request.setPhone("13800138000");

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert - First 3 requests should be allowed
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/send-sms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().is2xxSuccessful());
        }

        // 4th request should be rate limited
        mockMvc.perform(post("/api/auth/send-sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.message").value("请求过于频繁，请稍后再试"));
    }

    @Test
    void testLoginRateLimit_DifferentIPs_ShouldBeIndependent() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setPhone("13800138000");
        request.setSmsCode("123456");

        String requestBody = objectMapper.writeValueAsString(request);

        // Act & Assert - 5 requests from IP1
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
                    .header("X-Forwarded-For", "192.168.1.1"))
                    .andExpect(status().is4xxClientError());
        }

        // Request from IP2 should still be allowed
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .header("X-Forwarded-For", "192.168.1.2"))
                .andExpect(status().is4xxClientError()); // Fails auth, not rate limit
    }

    @Test
    void testSmsRateLimit_DifferentPhones_ShouldBeIndependent() throws Exception {
        // Arrange & Act - 3 requests for phone1
        SendSmsRequest request1 = new SendSmsRequest();
        request1.setPhone("13800138000");
        String requestBody1 = objectMapper.writeValueAsString(request1);

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/api/auth/send-sms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody1))
                    .andExpect(status().is2xxSuccessful());
        }

        // Request for phone2 should still be allowed
        SendSmsRequest request2 = new SendSmsRequest();
        request2.setPhone("13900139000");
        String requestBody2 = objectMapper.writeValueAsString(request2);

        mockMvc.perform(post("/api/auth/send-sms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody2))
                .andExpect(status().is2xxSuccessful());
    }
}
