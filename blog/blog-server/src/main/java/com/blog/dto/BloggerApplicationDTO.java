package com.blog.dto;

import com.blog.entity.BloggerApplication;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 博主申请DTO
 * 包含用户信息的完整申请数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BloggerApplicationDTO {
    
    private Long id;
    private Long userId;
    private String userName;
    private String userPhone;
    private String userEmail;
    private String nickname;
    private String bio;
    private String field;
    private BloggerApplication.ApplicationStatus status;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    
    /**
     * 从实体类转换为DTO
     */
    public static BloggerApplicationDTO fromEntity(BloggerApplication application) {
        BloggerApplicationDTO dto = new BloggerApplicationDTO();
        dto.setId(application.getId());
        dto.setUserId(application.getUserId());
        dto.setNickname(application.getNickname());
        dto.setBio(application.getBio());
        dto.setField(application.getField());
        dto.setStatus(application.getStatus());
        dto.setReviewComment(application.getReviewComment());
        dto.setCreatedAt(application.getCreatedAt());
        dto.setReviewedAt(application.getReviewedAt());
        
        // 从关联的User对象获取用户信息
        if (application.getUser() != null) {
            dto.setUserName(application.getUser().getNickname());
            dto.setUserPhone(application.getUser().getPhone());
            dto.setUserEmail(application.getUser().getEmail());
        }
        
        return dto;
    }
}
