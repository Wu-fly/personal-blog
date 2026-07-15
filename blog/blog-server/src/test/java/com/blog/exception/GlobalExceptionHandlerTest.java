package com.blog.exception;

import com.blog.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.persistence.EntityNotFoundException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for GlobalExceptionHandler
 * Verifies all exception handlers return correct HTTP status codes and error responses
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // ==================== 400 Bad Request Tests ====================

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException with 400 status")
    void testHandleMethodArgumentNotValidException() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("user", "email", "Email is required");
        when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));
        
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response = 
                exceptionHandler.handleValidationException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().containsKey("email"));
    }

    @Test
    @DisplayName("Should handle ConstraintViolationException with 400 status")
    void testHandleConstraintViolationException() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(javax.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("userId");
        when(violation.getMessage()).thenReturn("must not be null");
        violations.add(violation);
        
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        // Act
        ResponseEntity<ApiResponse<Map<String, String>>> response = 
                exceptionHandler.handleConstraintViolationException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Should handle MissingServletRequestParameterException with 400 status")
    void testHandleMissingServletRequestParameterException() {
        // Arrange
        MissingServletRequestParameterException ex = 
                new MissingServletRequestParameterException("page", "int");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleMissingServletRequestParameterException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("MISSING_PARAMETER", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("page"));
    }

    @Test
    @DisplayName("Should handle MethodArgumentTypeMismatchException with 400 status")
    void testHandleMethodArgumentTypeMismatchException() {
        // Arrange
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getRequiredType()).thenReturn((Class) Long.class);

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleMethodArgumentTypeMismatchException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INVALID_PARAMETER_TYPE", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("id"));
    }

    @Test
    @DisplayName("Should handle HttpMessageNotReadableException with 400 status")
    void testHandleHttpMessageNotReadableException() {
        // Arrange
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("JSON parse error");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleHttpMessageNotReadableException(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INVALID_REQUEST_BODY", response.getBody().getErrorCode());
    }

    // ==================== 401 Unauthorized Tests ====================

    @Test
    @DisplayName("Should handle BadCredentialsException with 401 status")
    void testHandleBadCredentialsException() {
        // Arrange
        BadCredentialsException ex = new BadCredentialsException("Invalid username or password");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleBadCredentialsException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INVALID_CREDENTIALS", response.getBody().getErrorCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle InsufficientAuthenticationException with 401 status")
    void testHandleInsufficientAuthenticationException() {
        // Arrange
        InsufficientAuthenticationException ex = 
                new InsufficientAuthenticationException("Full authentication is required");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleInsufficientAuthenticationException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("AUTHENTICATION_REQUIRED", response.getBody().getErrorCode());
    }

    @Test
    @DisplayName("Should handle AuthenticationException with 401 status")
    void testHandleAuthenticationException() {
        // Arrange
        AuthenticationException ex = new AuthenticationException("Authentication failed") {};

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleAuthenticationException(ex);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("UNAUTHORIZED", response.getBody().getErrorCode());
    }

    // ==================== 403 Forbidden Tests ====================

    @Test
    @DisplayName("Should handle AccessDeniedException with 403 status")
    void testHandleAccessDeniedException() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Access is denied");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleAccessDeniedException(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("FORBIDDEN", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("Access denied"));
    }

    // ==================== 404 Not Found Tests ====================

    @Test
    @DisplayName("Should handle EntityNotFoundException with 404 status")
    void testHandleEntityNotFoundException() {
        // Arrange
        EntityNotFoundException ex = new EntityNotFoundException("Article not found");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleEntityNotFoundException(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getErrorCode());
        assertEquals("Article not found", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle NoHandlerFoundException with 404 status")
    void testHandleNoHandlerFoundException() {
        // Arrange
        NoHandlerFoundException ex = new NoHandlerFoundException(
                "GET", "/api/invalid", null);

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleNoHandlerFoundException(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("ENDPOINT_NOT_FOUND", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("GET"));
        assertTrue(response.getBody().getMessage().contains("/api/invalid"));
    }

    // ==================== 422 Unprocessable Entity Tests ====================

    @Test
    @DisplayName("Should handle BusinessException with 422 status")
    void testHandleBusinessException() {
        // Arrange
        BusinessException ex = new BusinessException("INSUFFICIENT_BALANCE", "Insufficient wallet balance");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleBusinessException(ex);

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INSUFFICIENT_BALANCE", response.getBody().getErrorCode());
        assertEquals("Insufficient wallet balance", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Should handle BusinessException with default error code")
    void testHandleBusinessExceptionWithDefaultCode() {
        // Arrange
        BusinessException ex = new BusinessException("Something went wrong");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleBusinessException(ex);

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("BUSINESS_ERROR", response.getBody().getErrorCode());
    }

    // ==================== 500 Internal Server Error Tests ====================

    @Test
    @DisplayName("Should handle generic Exception with 500 status")
    void testHandleGenericException() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertTrue(response.getBody().getMessage().contains("unexpected error"));
    }

    @Test
    @DisplayName("Should handle NullPointerException with 500 status")
    void testHandleNullPointerException() {
        // Arrange
        NullPointerException ex = new NullPointerException("Null value encountered");

        // Act
        ResponseEntity<ApiResponse<Object>> response = 
                exceptionHandler.handleException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
    }

    // ==================== Response Format Tests ====================

    @Test
    @DisplayName("All error responses should have timestamp")
    void testAllErrorResponsesHaveTimestamp() {
        // Test various exceptions
        ResponseEntity<ApiResponse<Object>> response1 = 
                exceptionHandler.handleBusinessException(new BusinessException("Test"));
        ResponseEntity<ApiResponse<Object>> response2 = 
                exceptionHandler.handleEntityNotFoundException(new EntityNotFoundException("Test"));
        ResponseEntity<ApiResponse<Object>> response3 = 
                exceptionHandler.handleAccessDeniedException(new AccessDeniedException("Test"));

        assertNotNull(response1.getBody().getTimestamp());
        assertNotNull(response2.getBody().getTimestamp());
        assertNotNull(response3.getBody().getTimestamp());
    }

    @Test
    @DisplayName("All error responses should have success=false")
    void testAllErrorResponsesHaveSuccessFalse() {
        // Test various exceptions
        ResponseEntity<ApiResponse<Object>> response1 = 
                exceptionHandler.handleBusinessException(new BusinessException("Test"));
        ResponseEntity<ApiResponse<Object>> response2 = 
                exceptionHandler.handleEntityNotFoundException(new EntityNotFoundException("Test"));
        ResponseEntity<ApiResponse<Object>> response3 = 
                exceptionHandler.handleException(new RuntimeException("Test"));

        assertFalse(response1.getBody().isSuccess());
        assertFalse(response2.getBody().isSuccess());
        assertFalse(response3.getBody().isSuccess());
    }

    @Test
    @DisplayName("All error responses should have error code")
    void testAllErrorResponsesHaveErrorCode() {
        // Test various exceptions
        ResponseEntity<ApiResponse<Object>> response1 = 
                exceptionHandler.handleBusinessException(new BusinessException("CUSTOM_ERROR", "Test"));
        ResponseEntity<ApiResponse<Object>> response2 = 
                exceptionHandler.handleEntityNotFoundException(new EntityNotFoundException("Test"));
        ResponseEntity<ApiResponse<Object>> response3 = 
                exceptionHandler.handleAccessDeniedException(new AccessDeniedException("Test"));

        assertNotNull(response1.getBody().getErrorCode());
        assertNotNull(response2.getBody().getErrorCode());
        assertNotNull(response3.getBody().getErrorCode());
    }
}
