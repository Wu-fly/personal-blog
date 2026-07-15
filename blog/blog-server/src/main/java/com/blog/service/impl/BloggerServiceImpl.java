package com.blog.service.impl;

import com.blog.entity.Announcement;
import com.blog.entity.BloggerApplication;
import com.blog.entity.SpaceSetting;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.AnnouncementRepository;
import com.blog.repository.BloggerApplicationRepository;
import com.blog.repository.SpaceSettingRepository;
import com.blog.repository.UserRepository;
import com.blog.service.BloggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 博主功能服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BloggerServiceImpl implements BloggerService {
    
    private final UserRepository userRepository;
    private final BloggerApplicationRepository bloggerApplicationRepository;
    private final AnnouncementRepository announcementRepository;
    private final SpaceSettingRepository spaceSettingRepository;
    
    /**
     * Apply for blogger role
     * 权限: 需要登录才能申请成为博主
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public BloggerApplication applyForBlogger(Long userId, String nickname, String bio) {
        return applyForBlogger(userId, nickname, bio, null);
    }
    
    /**
     * Apply for blogger role with field information
     * 权限: 需要登录才能申请成为博主
     */
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public BloggerApplication applyForBlogger(Long userId, String nickname, String bio, String field) {
        log.info("User {} applying for blogger with nickname: {}, field: {}", userId, nickname, field);
        
        // 验证用户存在
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 检查用户是否已经是博主
        if (user.getRole() == User.UserRole.BLOGGER || user.getRole() == User.UserRole.ADMIN) {
            throw new BusinessException("ALREADY_BLOGGER", "用户已经是博主");
        }
        
        // 检查是否已有待审核的申请
        if (bloggerApplicationRepository.existsByUserIdAndStatus(userId, BloggerApplication.ApplicationStatus.PENDING)) {
            throw new BusinessException("APPLICATION_PENDING", "已有待审核的博主申请");
        }
        
        // 验证昵称不为空
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new BusinessException("INVALID_NICKNAME", "博主昵称不能为空");
        }
        
        // 创建博主申请记录
        BloggerApplication application = new BloggerApplication();
        application.setUserId(userId);
        application.setNickname(nickname.trim());
        application.setBio(bio != null ? bio.trim() : "");
        application.setField(field != null ? field.trim() : null);
        application.setStatus(BloggerApplication.ApplicationStatus.PENDING);
        
        BloggerApplication saved = bloggerApplicationRepository.save(application);
        log.info("Blogger application created with ID: {}", saved.getId());
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<BloggerApplication> getApplicationStatus(Long userId) {
        log.debug("Getting application status for user: {}", userId);
        return bloggerApplicationRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void cancelApplication(Long userId) {
        log.info("User {} canceling blogger application", userId);
        
        // 查找用户的申请
        BloggerApplication application = bloggerApplicationRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("APPLICATION_NOT_FOUND", "没有找到申请记录"));
        
        // 只能取消待审核的申请
        if (application.getStatus() != BloggerApplication.ApplicationStatus.PENDING) {
            throw new BusinessException("CANNOT_CANCEL", "只能取消待审核的申请");
        }
        
        // 删除申请记录
        bloggerApplicationRepository.delete(application);
        log.info("Blogger application cancelled for user: {}", userId);
    }
    
    /**
     * Save announcement
     * 权限: 需求 39.3 - 只有博主可以设置公告
     */
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public Announcement saveAnnouncement(Long userId, String content) {
        log.info("Saving announcement for user: {}", userId);
        
        // 验证用户是博主
        validateBlogger(userId);
        
        // 验证内容不为空
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("INVALID_CONTENT", "公告内容不能为空");
        }
        
        // 查找现有公告或创建新公告
        Announcement announcement = announcementRepository.findByUserId(userId)
            .orElse(new Announcement());
        
        announcement.setUserId(userId);
        announcement.setContent(content.trim());
        
        Announcement saved = announcementRepository.save(announcement);
        log.info("Announcement saved with ID: {}", saved.getId());
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Announcement> getAnnouncement(Long userId) {
        log.debug("Getting announcement for user: {}", userId);
        return announcementRepository.findByUserId(userId);
    }
    
    /**
     * Delete announcement
     * 权限: 需求 39.3 - 只有博主可以删除公告
     */
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public void deleteAnnouncement(Long userId) {
        log.info("Deleting announcement for user: {}", userId);
        
        // 验证用户是博主
        validateBlogger(userId);
        
        // 删除公告
        announcementRepository.deleteByUserId(userId);
        log.info("Announcement deleted for user: {}", userId);
    }
    
    /**
     * Save space settings
     * 权限: 需求 39.1 - 只有博主可以设置个人空间
     */
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public SpaceSetting saveSpaceSettings(Long userId, String themeColor, String backgroundImage, 
                                         SpaceSetting.LayoutStyle layoutStyle) {
        log.info("Saving space settings for user: {}", userId);
        
        // 验证用户是博主
        validateBlogger(userId);
        
        // 查找现有设置或创建新设置
        SpaceSetting settings = spaceSettingRepository.findByUserId(userId)
            .orElse(new SpaceSetting());
        
        settings.setUserId(userId);
        
        // 更新主题颜色（如果提供）
        if (themeColor != null && !themeColor.trim().isEmpty()) {
            settings.setThemeColor(themeColor.trim());
        }
        
        // 更新背景图片（如果提供）
        if (backgroundImage != null) {
            settings.setBackgroundImage(backgroundImage.trim().isEmpty() ? null : backgroundImage.trim());
        }
        
        // 更新布局样式（如果提供）
        if (layoutStyle != null) {
            settings.setLayoutStyle(layoutStyle);
        }
        
        SpaceSetting saved = spaceSettingRepository.save(settings);
        log.info("Space settings saved with ID: {}", saved.getId());
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<SpaceSetting> getSpaceSettings(Long userId) {
        log.debug("Getting space settings for user: {}", userId);
        return spaceSettingRepository.findByUserId(userId);
    }
    
    /**
     * Reset space settings
     * 权限: 需求 39.1 - 只有博主可以重置个人空间设置
     */
    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public SpaceSetting resetSpaceSettings(Long userId) {
        log.info("Resetting space settings for user: {}", userId);
        
        // 验证用户是博主
        validateBlogger(userId);
        
        // 查找现有设置或创建新设置
        SpaceSetting settings = spaceSettingRepository.findByUserId(userId)
            .orElse(new SpaceSetting());
        
        // 重置为默认值
        settings.setUserId(userId);
        settings.setThemeColor("#409EFF");
        settings.setBackgroundImage(null);
        settings.setLayoutStyle(SpaceSetting.LayoutStyle.CARD);
        
        SpaceSetting saved = spaceSettingRepository.save(settings);
        log.info("Space settings reset for user: {}", userId);
        
        return saved;
    }
    
    /**
     * 验证用户是否为博主
     * 
     * @param userId 用户ID
     * @throws BusinessException 如果用户不是博主
     */
    private void validateBlogger(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        if (user.getRole() != User.UserRole.BLOGGER && user.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException("NOT_BLOGGER", "用户不是博主");
        }
    }
}
