package com.blog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Web配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${upload.local.path:uploads}")
    private String uploadPath;
    
    /**
     * 配置静态资源映射
     * 将 /files/** 映射到本地上传目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 转换为绝对路径
        String absolutePath;
        if (Paths.get(uploadPath).isAbsolute()) {
            absolutePath = uploadPath;
        } else {
            absolutePath = Paths.get(System.getProperty("user.dir"), uploadPath).toAbsolutePath().toString();
        }
        
        // 确保路径以 / 结尾
        if (!absolutePath.endsWith("/") && !absolutePath.endsWith("\\")) {
            absolutePath += "/";
        }
        
        System.out.println("Static resource path: file:" + absolutePath);
        
        // Map /files/** to local upload directory
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
