package com.blog.controller;

import com.blog.dto.ApiResponse;
import com.blog.dto.UploadResponse;
import com.blog.exception.BusinessException;
import com.blog.service.UploadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 文件上传控制器测试
 */
@SpringBootTest
@AutoConfigureMockMvc
class UploadControllerTest extends com.blog.BaseTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private UploadService uploadService;
    
    private MockMultipartFile validImageFile;
    private MockMultipartFile invalidTypeFile;
    private MockMultipartFile oversizedFile;
    
    @BeforeEach
    void setUp() {
        // 有效的图片文件
        validImageFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        // 无效类型的文件
        invalidTypeFile = new MockMultipartFile(
            "file",
            "test-document.pdf",
            "application/pdf",
            "test pdf content".getBytes()
        );
        
        // 超大文件（模拟）
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        oversizedFile = new MockMultipartFile(
            "file",
            "large-image.jpg",
            "image/jpeg",
            largeContent
        );
    }
    
    @Test
    @WithMockUser
    void uploadImage_Success() throws Exception {
        // Arrange
        String expectedUrl = "http://localhost:8080/api/files/test-uuid.jpg";
        when(uploadService.uploadImage(any())).thenReturn(expectedUrl);
        
        // Act & Assert
        mockMvc.perform(multipart("/upload/image")
                .file(validImageFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value(expectedUrl))
                .andExpect(jsonPath("$.data.filename").value("test-image.jpg"))
                .andExpect(jsonPath("$.data.contentType").value("image/jpeg"));
        
        verify(uploadService, times(1)).uploadImage(any());
    }
    
    @Test
    void uploadImage_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户应该被拒绝
        mockMvc.perform(multipart("/upload/image")
                .file(validImageFile))
                .andExpect(status().isUnauthorized());
        
        verify(uploadService, never()).uploadImage(any());
    }
    
    @Test
    @WithMockUser
    void uploadImage_InvalidFileType() throws Exception {
        // Arrange
        when(uploadService.uploadImage(any()))
            .thenThrow(new BusinessException("不支持的文件类型"));
        
        // Act & Assert
        mockMvc.perform(multipart("/upload/image")
                .file(invalidTypeFile))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不支持的文件类型"));
    }
    
    @Test
    @WithMockUser
    void uploadImage_FileSizeExceeded() throws Exception {
        // Arrange
        when(uploadService.uploadImage(any()))
            .thenThrow(new BusinessException("文件大小超过限制"));
        
        // Act & Assert
        mockMvc.perform(multipart("/upload/image")
                .file(oversizedFile))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件大小超过限制"));
    }
    
    @Test
    @WithMockUser
    void uploadImage_EmptyFile() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );
        
        when(uploadService.uploadImage(any()))
            .thenThrow(new BusinessException("文件不能为空"));
        
        // Act & Assert
        mockMvc.perform(multipart("/upload/image")
                .file(emptyFile))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件不能为空"));
    }
    
    @Test
    @WithMockUser
    void uploadImage_ServiceException() throws Exception {
        // Arrange
        when(uploadService.uploadImage(any()))
            .thenThrow(new RuntimeException("服务器内部错误"));
        
        // Act & Assert
        mockMvc.perform(multipart("/upload/image")
                .file(validImageFile))
                .andExpect(status().isInternalServerError());
    }
    
    @Test
    @WithMockUser
    void deleteFile_Success() throws Exception {
        // Arrange
        String fileUrl = "http://localhost:8080/api/files/test-uuid.jpg";
        doNothing().when(uploadService).deleteFile(fileUrl);
        
        // Act & Assert
        mockMvc.perform(delete("/upload")
                .param("url", fileUrl))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        
        verify(uploadService, times(1)).deleteFile(fileUrl);
    }
    
    @Test
    void deleteFile_Unauthorized() throws Exception {
        // Act & Assert - 未登录用户应该被拒绝
        mockMvc.perform(delete("/upload")
                .param("url", "http://localhost:8080/api/files/test.jpg"))
                .andExpect(status().isUnauthorized());
        
        verify(uploadService, never()).deleteFile(any());
    }
    
    @Test
    @WithMockUser
    void deleteFile_InvalidUrl() throws Exception {
        // Arrange
        String invalidUrl = "invalid-url";
        doThrow(new BusinessException("无效的文件URL"))
            .when(uploadService).deleteFile(invalidUrl);
        
        // Act & Assert
        mockMvc.perform(delete("/upload")
                .param("url", invalidUrl))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("无效的文件URL"));
    }
    
    @Test
    @WithMockUser
    void deleteFile_FileNotFound() throws Exception {
        // Arrange
        String fileUrl = "http://localhost:8080/api/files/nonexistent.jpg";
        doThrow(new BusinessException("文件不存在"))
            .when(uploadService).deleteFile(fileUrl);
        
        // Act & Assert
        mockMvc.perform(delete("/upload")
                .param("url", fileUrl))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("文件不存在"));
    }
    
    @Test
    @WithMockUser
    void uploadImage_MultipleFiles() throws Exception {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "image1.jpg", "image/jpeg", "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "image2.png", "image/png", "content2".getBytes()
        );
        
        when(uploadService.uploadImage(any()))
            .thenReturn("http://localhost:8080/api/files/uuid1.jpg")
            .thenReturn("http://localhost:8080/api/files/uuid2.png");
        
        // Act & Assert - 上传第一个文件
        mockMvc.perform(multipart("/upload/image")
                .file(file1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("http://localhost:8080/api/files/uuid1.jpg"));
        
        // 上传第二个文件
        mockMvc.perform(multipart("/upload/image")
                .file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("http://localhost:8080/api/files/uuid2.png"));
        
        verify(uploadService, times(2)).uploadImage(any());
    }
}
