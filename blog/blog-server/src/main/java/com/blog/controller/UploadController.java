package com.blog.controller;

import com.blog.dto.ApiResponse;
import com.blog.dto.UploadResponse;
import com.blog.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制�?
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private final UploadService uploadService;
    
    /**
     * 上传图片
     * 
     * @param file 图片文件
     * @return 上传结果
     */
    @PostMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        
        log.info("收到图片上传请求，文件名: {}, 大小: {} bytes", 
                file.getOriginalFilename(), file.getSize());
        
        // 上传文件
        String fileUrl = uploadService.uploadImage(file);
        
        // 构建响应
        UploadResponse response = new UploadResponse(
            fileUrl,
            file.getOriginalFilename(),
            file.getSize(),
            file.getContentType()
        );
        
        log.info("图片上传成功: {}", fileUrl);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 删除文件
     * 
     * @param fileUrl 文件URL
     * @return 删除结果
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @RequestParam("url") String fileUrl) {
        
        log.info("收到文件删除请求，URL: {}", fileUrl);
        
        uploadService.deleteFile(fileUrl);
        
        log.info("文件删除成功: {}", fileUrl);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

