package com.blog.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface UploadService {
    
    /**
     * 上传图片到阿里云OSS
     * 
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadImage(MultipartFile file);
    
    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     */
    void deleteFile(String fileUrl);
    
    /**
     * 验证文件类型
     * 
     * @param file 文件
     * @return 是否为有效的图片类型
     */
    boolean isValidImageType(MultipartFile file);
    
    /**
     * 验证文件大小
     * 
     * @param file 文件
     * @return 是否在允许的大小范围内
     */
    boolean isValidFileSize(MultipartFile file);
}
