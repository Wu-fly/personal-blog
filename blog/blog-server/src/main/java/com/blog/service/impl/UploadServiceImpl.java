package com.blog.service.impl;

import com.blog.exception.BusinessException;
import com.blog.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现
 * 注意：这是一个简化的本地存储实现
 * 生产环境应该集成阿里云OSS
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    
    // 允许的图片类型
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    
    // 允许的文件扩展名
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );
    
    // 最大文件大小：5MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    
    // 本地存储路径（开发环境使用）
    @Value("${upload.local.path:uploads}")
    private String uploadPath;
    
    // 访问URL前缀
    @Value("${upload.url.prefix:http://localhost:8080/api/files}")
    private String urlPrefix;
    
    // 阿里云OSS配置（生产环境使用）
    @Value("${aliyun.oss.endpoint:}")
    private String ossEndpoint;
    
    @Value("${aliyun.oss.access-key-id:}")
    private String ossAccessKeyId;
    
    @Value("${aliyun.oss.access-key-secret:}")
    private String ossAccessKeySecret;
    
    @Value("${aliyun.oss.bucket-name:}")
    private String ossBucketName;
    
    @Override
    public String uploadImage(MultipartFile file) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        
        if (!isValidImageType(file)) {
            throw new BusinessException("不支持的文件类型，仅支持 JPG、PNG、GIF、WEBP 格式");
        }
        
        if (!isValidFileSize(file)) {
            throw new BusinessException("文件大小超过限制，最大支持 5MB");
        }
        
        // 如果配置了阿里云OSS，使用OSS上传
        if (isOssConfigured()) {
            return uploadToOss(file);
        }
        
        // 否则使用本地存储（开发环境）
        return uploadToLocal(file);
    }
    
    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            // 如果是OSS文件
            if (isOssConfigured() && fileUrl.contains(ossEndpoint)) {
                deleteFromOss(fileUrl);
            } else {
                // 本地文件
                deleteFromLocal(fileUrl);
            }
        } catch (Exception e) {
            log.error("删除文件失败: {}", fileUrl, e);
            throw new BusinessException("删除文件失败");
        }
    }
    
    @Override
    public boolean isValidImageType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
    
    @Override
    public boolean isValidFileSize(MultipartFile file) {
        return file.getSize() <= MAX_FILE_SIZE;
    }
    
    /**
     * 上传到本地存储
     */
    private String uploadToLocal(MultipartFile file) {
        try {
            // 创建上传目录 - 使用绝对路径
            Path uploadDir;
            if (Paths.get(uploadPath).isAbsolute()) {
                uploadDir = Paths.get(uploadPath);
            } else {
                // 如果是相对路径,使用项目根目录
                uploadDir = Paths.get(System.getProperty("user.dir"), uploadPath);
            }
            
            log.info("上传目录: {}", uploadDir.toAbsolutePath());
            
            // 确保目录存在
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建上传目录: {}", uploadDir.toAbsolutePath());
            }
            
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = uploadDir.resolve(filename);
            log.info("保存文件到: {}", filePath.toAbsolutePath());
            file.transferTo(filePath.toFile());
            
            // 返回访问URL
            String fileUrl = urlPrefix + "/" + filename;
            log.info("文件上传成功: {}", fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 从本地存储删除
     */
    private void deleteFromLocal(String fileUrl) {
        try {
            // 从URL提取文件名
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadPath, filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("文件删除成功: {}", fileUrl);
            }
        } catch (IOException e) {
            log.error("文件删除失败: {}", fileUrl, e);
            throw new BusinessException("文件删除失败");
        }
    }
    
    /**
     * 上传到阿里云OSS
     * 注意：这是一个占位实现，需要添加阿里云OSS SDK依赖
     */
    private String uploadToOss(MultipartFile file) {
        // TODO: 集成阿里云OSS SDK
        // 1. 创建OSSClient实例
        // 2. 生成唯一的对象名称
        // 3. 上传文件到OSS
        // 4. 返回文件的公网访问URL
        
        log.warn("阿里云OSS上传功能尚未实现，使用本地存储");
        return uploadToLocal(file);
    }
    
    /**
     * 从阿里云OSS删除
     */
    private void deleteFromOss(String fileUrl) {
        // TODO: 集成阿里云OSS SDK
        // 1. 创建OSSClient实例
        // 2. 从URL提取对象名称
        // 3. 删除OSS对象
        
        log.warn("阿里云OSS删除功能尚未实现");
    }
    
    /**
     * 检查是否配置了阿里云OSS
     */
    private boolean isOssConfigured() {
        return ossEndpoint != null && !ossEndpoint.isEmpty()
            && ossAccessKeyId != null && !ossAccessKeyId.isEmpty()
            && ossAccessKeySecret != null && !ossAccessKeySecret.isEmpty()
            && ossBucketName != null && !ossBucketName.isEmpty();
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
