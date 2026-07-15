package com.blog.service;

import com.blog.dto.PersonalSpaceResponse;
import com.blog.dto.UserProfileResponse;
import com.blog.entity.User;

/**
 * 用户服务接口
 * 处理用户信息管理
 */
public interface UserService {
    
    /**
     * 获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户信息
     * @throws com.blog.exception.BusinessException 如果用户不存在
     */
    UserProfileResponse getUserProfile(Long userId);
    
    /**
     * 更新用户信息
     * 
     * @param userId 用户ID
     * @param nickname 昵称
     * @param avatar 头像URL
     * @param bio 个人简介
     * @return 更新后的用户信息
     * @throws com.blog.exception.BusinessException 如果用户不存在
     */
    UserProfileResponse updateUserProfile(Long userId, String nickname, String avatar, String bio);
    
    /**
     * 获取用户个人空间信息
     * 
     * @param userId 用户ID
     * @return 个人空间信息
     * @throws com.blog.exception.BusinessException 如果用户不存在或不是博主
     */
    PersonalSpaceResponse getPersonalSpace(Long userId);
    
    /**
     * 根据ID获取用户实体
     * 
     * @param userId 用户ID
     * @return 用户实体
     * @throws com.blog.exception.BusinessException 如果用户不存在
     */
    User getUserById(Long userId);
}
