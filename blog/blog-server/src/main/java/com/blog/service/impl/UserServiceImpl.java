package com.blog.service.impl;

import com.blog.config.CacheConfig;
import com.blog.dto.PersonalSpaceResponse;
import com.blog.dto.UserProfileResponse;
import com.blog.entity.Announcement;
import com.blog.entity.SpaceSetting;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.AnnouncementRepository;
import com.blog.repository.ArticleRepository;
import com.blog.repository.FollowRepository;
import com.blog.repository.SpaceSettingRepository;
import com.blog.repository.UserRepository;
import com.blog.service.CacheService;
import com.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 * 需求: 9.1-9.4, 20.1-20.8
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final ArticleRepository articleRepository;
    private final AnnouncementRepository announcementRepository;
    private final SpaceSettingRepository spaceSettingRepository;
    private final CacheService cacheService;

    /**
     * 获取用户信息
     * 需求: 9.1
     * 权限: 需求 38.2 - 需要登录才能查看用户信息
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    // @Cacheable(value = CacheConfig.USER_INFO_CACHE, key = "#userId", unless = "#result == null")
    public UserProfileResponse getUserProfile(Long userId) {
        log.info("Getting user profile for userId: {}", userId);
        
        User user = getUserById(userId);
        return UserProfileResponse.fromEntity(user);
    }

    /**
     * 更新用户信息
     * 需求: 9.1
     * 权限: 需求 38.2 - 需要登录才能更新用户信息
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public UserProfileResponse updateUserProfile(Long userId, String nickname, String avatar, String bio) {
        log.info("Updating user profile for userId: {}", userId);
        
        User user = getUserById(userId);
        
        // 更新用户信息
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (avatar != null) {
            user.setAvatar(avatar);
        }
        if (bio != null) {
            user.setBio(bio);
        }
        
        User updatedUser = userRepository.save(user);
        
        // Evict user info cache after updating
        cacheService.evictUserInfoCache(userId);
        
        log.info("User profile updated successfully for userId: {}", userId);
        
        return UserProfileResponse.fromEntity(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalSpaceResponse getPersonalSpace(Long userId) {
        log.info("Getting personal space for userId: {}", userId);
        
        User user = getUserById(userId);
        
        // 检查用户是否是博主
        if (user.getRole() != User.UserRole.BLOGGER && user.getRole() != User.UserRole.ADMIN) {
            log.warn("User {} is not a blogger", userId);
            throw new BusinessException("该用户尚未开通个人空间");
        }
        
        // 获取粉丝数
        Long followerCount = followRepository.countByFollowingId(userId);
        
        // 获取文章总数（只统计审核通过的文章）
        Long articleCount = articleRepository.countByUserIdAndReviewStatus(userId, 
                com.blog.entity.Article.ReviewStatus.APPROVED);
        
        // 获取公告
        Announcement announcement = announcementRepository.findByUserId(userId).orElse(null);
        
        // 获取个人空间设置
        SpaceSetting spaceSettings = spaceSettingRepository.findByUserId(userId).orElse(null);
        
        return PersonalSpaceResponse.fromEntity(user, followerCount, articleCount, announcement, spaceSettings);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}
