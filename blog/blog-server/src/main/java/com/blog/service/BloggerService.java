package com.blog.service;

import com.blog.entity.Announcement;
import com.blog.entity.BloggerApplication;
import com.blog.entity.SpaceSetting;

import java.util.Optional;

/**
 * 博主功能服务接口
 * 处理博主申请、公告管理、个人空间设置
 */
public interface BloggerService {
    
    /**
     * 申请成为博主
     * 
     * @param userId 用户ID
     * @param nickname 博主昵称
     * @param bio 博主简介
     * @return 博主申请记录
     * @throws com.blog.exception.BusinessException 如果用户已是博主或已有待审核申请
     */
    BloggerApplication applyForBlogger(Long userId, String nickname, String bio);
    
    /**
     * 查询用户的博主申请状态
     * 
     * @param userId 用户ID
     * @return 博主申请记录（如果存在）
     */
    Optional<BloggerApplication> getApplicationStatus(Long userId);
    
    /**
     * 取消博主申请（仅待审核状态可取消）
     * 
     * @param userId 用户ID
     * @throws com.blog.exception.BusinessException 如果没有待审核的申请
     */
    void cancelApplication(Long userId);
    
    /**
     * 创建或更新公告
     * 
     * @param userId 博主用户ID
     * @param content 公告内容
     * @return 公告记录
     * @throws com.blog.exception.BusinessException 如果用户不是博主或内容为空
     */
    Announcement saveAnnouncement(Long userId, String content);
    
    /**
     * 获取博主的公告
     * 
     * @param userId 博主用户ID
     * @return 公告记录（如果存在）
     */
    Optional<Announcement> getAnnouncement(Long userId);
    
    /**
     * 删除公告
     * 
     * @param userId 博主用户ID
     * @throws com.blog.exception.BusinessException 如果用户不是博主
     */
    void deleteAnnouncement(Long userId);
    
    /**
     * 保存个人空间设置
     * 
     * @param userId 博主用户ID
     * @param themeColor 主题颜色
     * @param backgroundImage 背景图片URL
     * @param layoutStyle 布局样式
     * @return 个人空间设置记录
     * @throws com.blog.exception.BusinessException 如果用户不是博主
     */
    SpaceSetting saveSpaceSettings(Long userId, String themeColor, String backgroundImage, 
                                   SpaceSetting.LayoutStyle layoutStyle);
    
    /**
     * 获取个人空间设置
     * 
     * @param userId 博主用户ID
     * @return 个人空间设置记录（如果存在）
     */
    Optional<SpaceSetting> getSpaceSettings(Long userId);
    
    /**
     * 重置个人空间设置为默认值
     * 
     * @param userId 博主用户ID
     * @return 重置后的个人空间设置
     * @throws com.blog.exception.BusinessException 如果用户不是博主
     */
    SpaceSetting resetSpaceSettings(Long userId);
}
