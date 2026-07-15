package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.BloggerApplication;
import com.blog.entity.SpaceSetting;
import com.blog.entity.User;
import com.blog.security.CustomUserDetails;
import com.blog.service.BloggerService;
import com.blog.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController单元测试
 * 需求: 9.1-9.4, 19.1-19.6, 20.1-20.8, 40.1-40.7
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private BloggerService bloggerService;

    private CustomUserDetails userDetails;
    private UserProfileResponse userProfile;
    private PersonalSpaceResponse personalSpace;

    @BeforeEach
    void setUp() {
        // 创建测试用户详情
        userDetails = new CustomUserDetails(1L, "13800138000", "password", "USER", "ACTIVE");

        // 创建测试用户信息
        userProfile = UserProfileResponse.builder()
                .id(1L)
                .phone("13800138000")
                .email("test@example.com")
                .nickname("测试用户")
                .avatar("http://example.com/avatar.jpg")
                .bio("这是测试用户的简介")
                .role("USER")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 创建测试个人空间信息
        personalSpace = PersonalSpaceResponse.builder()
                .userId(1L)
                .nickname("测试博主")
                .avatar("http://example.com/avatar.jpg")
                .bio("这是测试博主的简介")
                .followerCount(100L)
                .articleCount(50L)
                .announcement("欢迎来到我的个人空间")
                .spaceSettings(PersonalSpaceResponse.SpaceSettingInfo.builder()
                        .themeColor("#409EFF")
                        .backgroundImage("http://example.com/bg.jpg")
                        .layoutStyle("CARD")
                        .build())
                .build();
    }

    @Test
    @DisplayName("获取用户信息 - 成功")
    @WithMockUser
    void testGetUserProfile_Success() throws Exception {
        // Given
        when(userService.getUserProfile(1L)).thenReturn(userProfile);

        // When & Then
        mockMvc.perform(get("/api/users/profile")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));
    }

    @Test
    @DisplayName("更新用户信息 - 成功")
    @WithMockUser
    void testUpdateUserProfile_Success() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("新昵称");
        request.setAvatar("http://example.com/new-avatar.jpg");
        request.setBio("新的个人简介");

        UserProfileResponse updatedProfile = UserProfileResponse.builder()
                .id(1L)
                .phone("13800138000")
                .email("test@example.com")
                .nickname("新昵称")
                .avatar("http://example.com/new-avatar.jpg")
                .bio("新的个人简介")
                .role("USER")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userService.updateUserProfile(eq(1L), eq("新昵称"), 
                eq("http://example.com/new-avatar.jpg"), eq("新的个人简介")))
                .thenReturn(updatedProfile);

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.avatar").value("http://example.com/new-avatar.jpg"))
                .andExpect(jsonPath("$.data.bio").value("新的个人简介"));
    }

    @Test
    @DisplayName("更新用户信息 - 昵称过长")
    @WithMockUser
    void testUpdateUserProfile_NicknameTooLong() throws Exception {
        // Given
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setNickname("这是一个非常非常非常非常非常非常非常非常非常非常非常非常非常非常长的昵称超过了50个字符的限制");

        // When & Then
        mockMvc.perform(put("/api/users/profile")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("申请成为博主 - 成功")
    @WithMockUser
    void testApplyForBlogger_Success() throws Exception {
        // Given
        BloggerApplicationRequest request = new BloggerApplicationRequest();
        request.setNickname("测试博主");
        request.setBio("我想成为博主，分享我的知识和经验");

        BloggerApplication application = new BloggerApplication();
        application.setId(1L);
        application.setUserId(1L);
        application.setNickname("测试博主");
        application.setBio("我想成为博主，分享我的知识和经验");
        application.setStatus(BloggerApplication.ApplicationStatus.PENDING);

        when(bloggerService.applyForBlogger(eq(1L), eq("测试博主"), 
                eq("我想成为博主，分享我的知识和经验")))
                .thenReturn(application);

        // When & Then
        mockMvc.perform(post("/api/users/apply-blogger")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("博主申请已提交，请等待审核"))
                .andExpect(jsonPath("$.data.nickname").value("测试博主"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("申请成为博主 - 昵称为空")
    @WithMockUser
    void testApplyForBlogger_NicknameBlank() throws Exception {
        // Given
        BloggerApplicationRequest request = new BloggerApplicationRequest();
        request.setNickname("");
        request.setBio("我想成为博主，分享我的知识和经验");

        // When & Then
        mockMvc.perform(post("/api/users/apply-blogger")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("申请成为博主 - 简介过短")
    @WithMockUser
    void testApplyForBlogger_BioTooShort() throws Exception {
        // Given
        BloggerApplicationRequest request = new BloggerApplicationRequest();
        request.setNickname("测试博主");
        request.setBio("太短了");

        // When & Then
        mockMvc.perform(post("/api/users/apply-blogger")
                        .with(user(userDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取博主申请状态 - 有申请记录")
    @WithMockUser
    void testGetBloggerApplicationStatus_HasApplication() throws Exception {
        // Given
        BloggerApplication application = new BloggerApplication();
        application.setId(1L);
        application.setUserId(1L);
        application.setNickname("测试博主");
        application.setBio("我想成为博主");
        application.setStatus(BloggerApplication.ApplicationStatus.PENDING);

        when(bloggerService.getApplicationStatus(1L))
                .thenReturn(Optional.of(application));

        // When & Then
        mockMvc.perform(get("/api/users/blogger-application")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("获取博主申请状态 - 无申请记录")
    @WithMockUser
    void testGetBloggerApplicationStatus_NoApplication() throws Exception {
        // Given
        when(bloggerService.getApplicationStatus(1L))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/users/blogger-application")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("暂无申请记录"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("获取个人空间 - 成功")
    void testGetPersonalSpace_Success() throws Exception {
        // Given
        when(userService.getPersonalSpace(1L)).thenReturn(personalSpace);

        // When & Then
        mockMvc.perform(get("/api/users/1/space"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(1))
                .andExpect(jsonPath("$.data.nickname").value("测试博主"))
                .andExpect(jsonPath("$.data.followerCount").value(100))
                .andExpect(jsonPath("$.data.articleCount").value(50))
                .andExpect(jsonPath("$.data.announcement").value("欢迎来到我的个人空间"))
                .andExpect(jsonPath("$.data.spaceSettings.themeColor").value("#409EFF"))
                .andExpect(jsonPath("$.data.spaceSettings.layoutStyle").value("CARD"));
    }

    @Test
    @DisplayName("更新个人空间设置 - 成功")
    @WithMockUser(roles = "BLOGGER")
    void testUpdateSpaceSettings_Success() throws Exception {
        // Given
        CustomUserDetails bloggerDetails = new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE");
        
        SpaceSettingsRequest request = new SpaceSettingsRequest();
        request.setThemeColor("#FF5722");
        request.setBackgroundImage("http://example.com/new-bg.jpg");
        request.setLayoutStyle("DOUBLE");

        SpaceSetting settings = new SpaceSetting();
        settings.setId(1L);
        settings.setUserId(1L);
        settings.setThemeColor("#FF5722");
        settings.setBackgroundImage("http://example.com/new-bg.jpg");
        settings.setLayoutStyle(SpaceSetting.LayoutStyle.DOUBLE);

        when(bloggerService.saveSpaceSettings(eq(1L), eq("#FF5722"), 
                eq("http://example.com/new-bg.jpg"), eq(SpaceSetting.LayoutStyle.DOUBLE)))
                .thenReturn(settings);

        // When & Then
        mockMvc.perform(put("/api/users/space/settings")
                        .with(user(bloggerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("个人空间设置已更新"))
                .andExpect(jsonPath("$.data.themeColor").value("#FF5722"))
                .andExpect(jsonPath("$.data.layoutStyle").value("DOUBLE"));
    }

    @Test
    @DisplayName("更新个人空间设置 - 主题颜色格式错误")
    @WithMockUser(roles = "BLOGGER")
    void testUpdateSpaceSettings_InvalidThemeColor() throws Exception {
        // Given
        CustomUserDetails bloggerDetails = new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE");
        
        SpaceSettingsRequest request = new SpaceSettingsRequest();
        request.setThemeColor("invalid-color");
        request.setLayoutStyle("CARD");

        // When & Then
        mockMvc.perform(put("/api/users/space/settings")
                        .with(user(bloggerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("更新个人空间设置 - 布局样式无效")
    @WithMockUser(roles = "BLOGGER")
    void testUpdateSpaceSettings_InvalidLayoutStyle() throws Exception {
        // Given
        CustomUserDetails bloggerDetails = new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE");
        
        SpaceSettingsRequest request = new SpaceSettingsRequest();
        request.setThemeColor("#409EFF");
        request.setLayoutStyle("INVALID");

        // When & Then
        mockMvc.perform(put("/api/users/space/settings")
                        .with(user(bloggerDetails))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("获取个人空间设置 - 成功")
    @WithMockUser(roles = "BLOGGER")
    void testGetSpaceSettings_Success() throws Exception {
        // Given
        CustomUserDetails bloggerDetails = new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE");
        
        SpaceSetting settings = new SpaceSetting();
        settings.setId(1L);
        settings.setUserId(1L);
        settings.setThemeColor("#409EFF");
        settings.setBackgroundImage("http://example.com/bg.jpg");
        settings.setLayoutStyle(SpaceSetting.LayoutStyle.CARD);

        when(bloggerService.getSpaceSettings(1L))
                .thenReturn(Optional.of(settings));

        // When & Then
        mockMvc.perform(get("/api/users/space/settings")
                        .with(user(bloggerDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.themeColor").value("#409EFF"))
                .andExpect(jsonPath("$.data.layoutStyle").value("CARD"));
    }

    @Test
    @DisplayName("重置个人空间设置 - 成功")
    @WithMockUser(roles = "BLOGGER")
    void testResetSpaceSettings_Success() throws Exception {
        // Given
        CustomUserDetails bloggerDetails = new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE");
        
        SpaceSetting settings = new SpaceSetting();
        settings.setId(1L);
        settings.setUserId(1L);
        settings.setThemeColor("#409EFF");
        settings.setLayoutStyle(SpaceSetting.LayoutStyle.CARD);

        when(bloggerService.resetSpaceSettings(1L))
                .thenReturn(settings);

        // When & Then
        mockMvc.perform(post("/api/users/space/settings/reset")
                        .with(user(bloggerDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("个人空间设置已重置为默认值"))
                .andExpect(jsonPath("$.data.themeColor").value("#409EFF"))
                .andExpect(jsonPath("$.data.layoutStyle").value("CARD"));
    }
}
