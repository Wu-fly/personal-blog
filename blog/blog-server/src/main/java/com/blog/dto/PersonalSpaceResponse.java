package com.blog.dto;

import com.blog.entity.Announcement;
import com.blog.entity.SpaceSetting;
import com.blog.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 个人空间响应DTO
 * 需求: 20.1-20.8
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalSpaceResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 粉丝数
     */
    private Long followerCount;

    /**
     * 文章总数
     */
    private Long articleCount;

    /**
     * 公告内容
     */
    private String announcement;

    /**
     * 个人空间设置
     */
    private SpaceSettingInfo spaceSettings;

    /**
     * 个人空间设置信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpaceSettingInfo {
        private String themeColor;
        private String backgroundImage;
        private String layoutStyle;

        public static SpaceSettingInfo fromEntity(SpaceSetting setting) {
            if (setting == null) {
                return SpaceSettingInfo.builder()
                        .themeColor("#409EFF")
                        .layoutStyle("CARD")
                        .build();
            }
            return SpaceSettingInfo.builder()
                    .themeColor(setting.getThemeColor())
                    .backgroundImage(setting.getBackgroundImage())
                    .layoutStyle(setting.getLayoutStyle().name())
                    .build();
        }
    }

    /**
     * 从User实体和其他信息构建响应
     */
    public static PersonalSpaceResponse fromEntity(User user, Long followerCount, Long articleCount, 
                                                   Announcement announcement, SpaceSetting spaceSettings) {
        return PersonalSpaceResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .followerCount(followerCount)
                .articleCount(articleCount)
                .announcement(announcement != null ? announcement.getContent() : null)
                .spaceSettings(SpaceSettingInfo.fromEntity(spaceSettings))
                .build();
    }
}
