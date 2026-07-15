package com.blog.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for CORS configuration
 * 
 * Verifies that CORS is properly configured to allow cross-origin requests
 * from the frontend applications.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Test that preflight OPTIONS request is handled correctly
     */
    @Test
    void testPreflightRequest() throws Exception {
        mockMvc.perform(options("/api/articles")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    /**
     * Test that actual request includes CORS headers
     */
    @Test
    void testActualRequest() throws Exception {
        mockMvc.perform(get("/api/articles")
                .header("Origin", "http://localhost:5173"))
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    /**
     * Test that allowed origin is configured correctly
     */
    @Test
    void testAllowedOrigin() throws Exception {
        mockMvc.perform(get("/api/articles")
                .header("Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    /**
     * Test that credentials are allowed
     */
    @Test
    void testAllowCredentials() throws Exception {
        mockMvc.perform(options("/api/articles")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    /**
     * Test that common HTTP methods are allowed
     */
    @Test
    void testAllowedMethods() throws Exception {
        mockMvc.perform(options("/api/articles")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }

    /**
     * Test that Authorization header is allowed
     */
    @Test
    void testAllowedHeaders() throws Exception {
        mockMvc.perform(options("/api/articles")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    /**
     * Test that max age is set for preflight caching
     */
    @Test
    void testMaxAge() throws Exception {
        mockMvc.perform(options("/api/articles")
                .header("Origin", "http://localhost:5173")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().exists("Access-Control-Max-Age"));
    }
}
