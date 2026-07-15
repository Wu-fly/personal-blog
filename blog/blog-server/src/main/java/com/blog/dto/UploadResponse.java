package com.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponse {
    
    /**
     * 文件URL
     */
    private String url;
    
    /**
     * 文件名
     */
    private String filename;
    
    /**
     * 文件大小（字节）
     */
    private Long size;
    
    /**
     * 文件类型
     */
    private String contentType;
}
