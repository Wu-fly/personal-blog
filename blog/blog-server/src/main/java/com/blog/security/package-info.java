/**
 * Security layer - Authentication and authorization
 * 
 * This package contains all security-related classes including:
 * - JwtUtil: JWT token generation, validation, and parsing
 * - JwtAuthenticationFilter: Filter to intercept requests and validate JWT tokens
 * - JwtAuthenticationEntryPoint: Handle unauthorized access attempts
 * - SecurityConfig: Spring Security configuration (CORS, CSRF, authorization rules)
 * - CustomUserDetails: Custom implementation of UserDetails for Spring Security
 * 
 * Requirements:
 * - 1.3: JWT token generation and validation with 7-day expiration
 * - 1.9: Token expiration checking and re-authentication
 */
package com.blog.security;
