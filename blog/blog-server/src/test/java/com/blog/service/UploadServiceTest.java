package com.blog.service;

import com.blog.exception.BusinessException;
import com.blog.service.impl.UploadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 文件上传服务测试
 */
class UploadServiceTest {
    
    private UploadService uploadService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        uploadService = new UploadServiceImpl();
        // 设置临时上传路径
        ReflectionTestUtils.setField(uploadService, "uploadPath", tempDir.toString());
        ReflectionTestUtils.setField(uploadService, "urlPrefix", "http://localhost:8080/api/files");
    }
    
    @Test
    void uploadImage_ValidJpegImage_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );
        
        // Act
        String fileUrl = uploadService.uploadImage(file);
        
        // Assert
        assertNotNull(fileUrl);
        assertTrue(fileUrl.startsWith("http://localhost:8080/api/files/"));
        assertTrue(fileUrl.endsWith(".jpg"));
    }
    
    @Test
    void uploadImage_ValidPngImage_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            "test image content".getBytes()
        );
        
        // Act
        String fileUrl = uploadService.uploadImage(file);
        
        // Assert
        assertNotNull(fileUrl);
        assertTrue(fileUrl.endsWith(".png"));
    }
    
    @Test
    void uploadImage_NullFile_ThrowsException() {
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> uploadService.uploadImage(null)
        );
        assertEquals("文件不能为空", exception.getMessage());
    }
    
    @Test
    void uploadImage_EmptyFile_ThrowsException() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );
        
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> uploadService.uploadImage(emptyFile)
        );
        assertEquals("文件不能为空", exception.getMessage());
    }
    
    @Test
    void uploadImage_InvalidFileType_ThrowsException() {
        // Arrange
        MockMultipartFile pdfFile = new MockMultipartFile(
            "file",
            "document.pdf",
            "application/pdf",
            "pdf content".getBytes()
        );
        
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> uploadService.uploadImage(pdfFile)
        );
        assertTrue(exception.getMessage().contains("不支持的文件类型"));
    }
    
    @Test
    void uploadImage_OversizedFile_ThrowsException() {
        // Arrange - 创建6MB的文件（超过5MB限制）
        byte[] largeContent = new byte[6 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            largeContent
        );
        
        // Act & Assert
        BusinessException exception = assertThrows(
            BusinessException.class,
            () -> uploadService.uploadImage(largeFile)
        );
        assertTrue(exception.getMessage().contains("文件大小超过限制"));
    }
    
    @Test
    void isValidImageType_JpegImage_ReturnsTrue() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "content".getBytes()
        );
        
        // Act & Assert
        assertTrue(uploadService.isValidImageType(file));
    }
    
    @Test
    void isValidImageType_PngImage_ReturnsTrue() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            "content".getBytes()
        );
        
        // Act & Assert
        assertTrue(uploadService.isValidImageType(file));
    }
    
    @Test
    void isValidImageType_GifImage_ReturnsTrue() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.gif",
            "image/gif",
            "content".getBytes()
        );
        
        // Act & Assert
        assertTrue(uploadService.isValidImageType(file));
    }
    
    @Test
    void isValidImageType_WebpImage_ReturnsTrue() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.webp",
            "image/webp",
            "content".getBytes()
        );
        
        // Act & Assert
        assertTrue(uploadService.isValidImageType(file));
    }
    
    @Test
    void isValidImageType_PdfFile_ReturnsFalse() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "document.pdf",
            "application/pdf",
            "content".getBytes()
        );
        
        // Act & Assert
        assertFalse(uploadService.isValidImageType(file));
    }
    
    @Test
    void isValidImageType_TextFile_ReturnsFalse() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "document.txt",
            "text/plain",
            "content".getBytes()
        );
        
        // Act & Assert
        assertFalse(uploadService.isValidImageType(file));
    }
    
    @Test
    void isValidImageType_NullContentType_ReturnsFalse() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            null,
            "content".getBytes()
        );
        
        // Act & Assert
        assertFalse(uploadService.isValidImageType(file));
    }
    
    @Test
    void isValidFileSize_SmallFile_ReturnsTrue() {
        // Arrange - 1MB文件
        byte[] content = new byte[1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // Act & Assert
        assertTrue(uploadService.isValidFileSize(file));
    }
    
    @Test
    void isValidFileSize_MaxSizeFile_ReturnsTrue() {
        // Arrange - 正好5MB
        byte[] content = new byte[5 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // Act & Assert
        assertTrue(uploadService.isValidFileSize(file));
    }
    
    @Test
    void isValidFileSize_OversizedFile_ReturnsFalse() {
        // Arrange - 6MB文件
        byte[] content = new byte[6 * 1024 * 1024];
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            content
        );
        
        // Act & Assert
        assertFalse(uploadService.isValidFileSize(file));
    }
    
    @Test
    void deleteFile_ExistingFile_Success() throws IOException {
        // Arrange - 先上传一个文件
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );
        String fileUrl = uploadService.uploadImage(file);
        
        // Act - 删除文件
        assertDoesNotThrow(() -> uploadService.deleteFile(fileUrl));
        
        // Assert - 验证文件已被删除
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        Path filePath = tempDir.resolve(filename);
        assertFalse(Files.exists(filePath));
    }
    
    @Test
    void deleteFile_NonExistentFile_DoesNotThrowException() {
        // Arrange
        String nonExistentUrl = "http://localhost:8080/api/files/nonexistent.jpg";
        
        // Act & Assert - 删除不存在的文件不应该抛出异常（静默处理）
        assertDoesNotThrow(() -> uploadService.deleteFile(nonExistentUrl));
    }
    
    @Test
    void deleteFile_NullUrl_DoesNothing() {
        // Act & Assert
        assertDoesNotThrow(() -> uploadService.deleteFile(null));
    }
    
    @Test
    void deleteFile_EmptyUrl_DoesNothing() {
        // Act & Assert
        assertDoesNotThrow(() -> uploadService.deleteFile(""));
    }
    
    @Test
    void uploadImage_CreatesUploadDirectory() throws IOException {
        // Arrange - 使用不存在的目录
        Path newUploadDir = tempDir.resolve("new-uploads");
        ReflectionTestUtils.setField(uploadService, "uploadPath", newUploadDir.toString());
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "content".getBytes()
        );
        
        // Act
        String fileUrl = uploadService.uploadImage(file);
        
        // Assert - 验证目录已创建
        assertTrue(Files.exists(newUploadDir));
        assertNotNull(fileUrl);
    }
    
    @Test
    void uploadImage_GeneratesUniqueFilenames() {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "content2".getBytes()
        );
        
        // Act
        String url1 = uploadService.uploadImage(file1);
        String url2 = uploadService.uploadImage(file2);
        
        // Assert - 两个文件应该有不同的URL
        assertNotEquals(url1, url2);
    }
}
