package com.blog.service;

import com.blog.dto.PersonalSpaceResponse;
import com.blog.dto.UserProfileResponse;
import com.blog.entity.*;
import com.blog.exception.BusinessException;
import com.blog.repository.*;
import com.blog.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * UserService单元测试
 * 需求: 9.1-9.4, 20.1-20.8
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private AnnouncementRepository announcementRepository;

    @Mock
    private SpaceSettingRepository spaceSettingRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User testBlogger;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setNickname("测试用户");
        testUser.setAvatar("http://example.com/avatar.jpg");
        testUser.setBio("这是测试用户的简介");
        testUser.setRole(User.UserRole.USER);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        testBlogger = new User();
        testBlogger.setId(2L);
        testBlogger.setPhone("13900139000");
        testBlogger.setEmail("blogger@example.com");
        testBlogger.setNickname("测试博主");
        testBlogger.setAvatar("http://example.com/blogger-avatar.jpg");
        testBlogger.setBio("这是测试博主的简介");
        testBlogger.setRole(User.UserRole.BLOGGER);
        testBlogger.setStatus(User.UserStatus.ACTIVE);
        testBlogger.setCreatedAt(LocalDateTime.now());
        testBlogger.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    void testGetUserProfile_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserProfileResponse profile = userService.getUserProfile(1L);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getId()).isEqualTo(1L);
        assertThat(profile.getPhone()).isEqualTo("13800138000");
        assertThat(profile.getEmail()).isEqualTo("test@example.com");
        assertThat(profile.getNickname()).isEqualTo("测试用户");
        assertThat(profile.getRole()).isEqualTo("USER");
        
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    void testGetUserProfile_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserProfile(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
        
        verify(userRepository).findById(999L);
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    void testUpdateUserProfile_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserProfileResponse profile = userService.updateUserProfile(1L, "新昵称", 
                "http://example.com/new-avatar.jpg", "新的个人简介");

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getNickname()).isEqualTo("新昵称");
        assertThat(profile.getAvatar()).isEqualTo("http://example.com/new-avatar.jpg");
        assertThat(profile.getBio()).isEqualTo("新的个人简介");
        
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("更新用户信息 - 部分更新")
    void testUpdateUserProfile_PartialUpdate() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - 只更新昵称
        UserProfileResponse profile = userService.updateUserProfile(1L, "新昵称", null, null);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getNickname()).isEqualTo("新昵称");
        assertThat(profile.getAvatar()).isEqualTo("http://example.com/avatar.jpg"); // 保持原值
        assertThat(profile.getBio()).isEqualTo("这是测试用户的简介"); // 保持原值
        
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("获取个人空间 - 博主成功")
    void testGetPersonalSpace_BloggerSuccess() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        when(followRepository.countByFollowingId(2L)).thenReturn(100L);
        when(articleRepository.countByUserIdAndReviewStatus(2L, Article.ReviewStatus.APPROVED))
                .thenReturn(50L);
        
        Announcement announcement = new Announcement();
        announcement.setContent("欢迎来到我的个人空间");
        when(announcementRepository.findByUserId(2L)).thenReturn(Optional.of(announcement));
        
        SpaceSetting settings = new SpaceSetting();
        settings.setThemeColor("#409EFF");
        settings.setBackgroundImage("http://example.com/bg.jpg");
        settings.setLayoutStyle(SpaceSetting.LayoutStyle.CARD);
        when(spaceSettingRepository.findByUserId(2L)).thenReturn(Optional.of(settings));

        // When
        PersonalSpaceResponse space = userService.getPersonalSpace(2L);

        // Then
        assertThat(space).isNotNull();
        assertThat(space.getUserId()).isEqualTo(2L);
        assertThat(space.getNickname()).isEqualTo("测试博主");
        assertThat(space.getFollowerCount()).isEqualTo(100L);
        assertThat(space.getArticleCount()).isEqualTo(50L);
        assertThat(space.getAnnouncement()).isEqualTo("欢迎来到我的个人空间");
        assertThat(space.getSpaceSettings().getThemeColor()).isEqualTo("#409EFF");
        assertThat(space.getSpaceSettings().getLayoutStyle()).isEqualTo("CARD");
        
        verify(userRepository).findById(2L);
        verify(followRepository).countByFollowingId(2L);
        verify(articleRepository).countByUserIdAndReviewStatus(2L, Article.ReviewStatus.APPROVED);
        verify(announcementRepository).findByUserId(2L);
        verify(spaceSettingRepository).findByUserId(2L);
    }

    @Test
    @DisplayName("获取个人空间 - 非博主用户")
    void testGetPersonalSpace_NotBlogger() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThatThrownBy(() -> userService.getPersonalSpace(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("该用户尚未开通个人空间");
        
        verify(userRepository).findById(1L);
        verify(followRepository, never()).countByFollowingId(any());
    }

    @Test
    @DisplayName("获取个人空间 - 无公告和设置")
    void testGetPersonalSpace_NoAnnouncementAndSettings() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        when(followRepository.countByFollowingId(2L)).thenReturn(50L);
        when(articleRepository.countByUserIdAndReviewStatus(2L, Article.ReviewStatus.APPROVED))
                .thenReturn(20L);
        when(announcementRepository.findByUserId(2L)).thenReturn(Optional.empty());
        when(spaceSettingRepository.findByUserId(2L)).thenReturn(Optional.empty());

        // When
        PersonalSpaceResponse space = userService.getPersonalSpace(2L);

        // Then
        assertThat(space).isNotNull();
        assertThat(space.getAnnouncement()).isNull();
        assertThat(space.getSpaceSettings()).isNotNull();
        assertThat(space.getSpaceSettings().getThemeColor()).isEqualTo("#409EFF"); // 默认值
        assertThat(space.getSpaceSettings().getLayoutStyle()).isEqualTo("CARD"); // 默认值
    }

    @Test
    @DisplayName("获取用户实体 - 成功")
    void testGetUserById_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        User user = userService.getUserById(1L);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getPhone()).isEqualTo("13800138000");
        
        verify(userRepository).findById(1L);
    }

    @Test
    @DisplayName("获取用户实体 - 用户不存在")
    void testGetUserById_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("用户不存在");
        
        verify(userRepository).findById(999L);
    }
}
