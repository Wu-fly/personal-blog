package com.blog.service;

import com.blog.entity.Announcement;
import com.blog.entity.BloggerApplication;
import com.blog.entity.SpaceSetting;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.AnnouncementRepository;
import com.blog.repository.BloggerApplicationRepository;
import com.blog.repository.SpaceSettingRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.BloggerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * BloggerService单元测试
 */
@ExtendWith(MockitoExtension.class)
class BloggerServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private BloggerApplicationRepository bloggerApplicationRepository;
    
    @Mock
    private AnnouncementRepository announcementRepository;
    
    @Mock
    private SpaceSettingRepository spaceSettingRepository;
    
    @InjectMocks
    private BloggerServiceImpl bloggerService;
    
    private User testUser;
    private User testBlogger;
    
    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setRole(User.UserRole.USER);
        
        // 创建测试博主
        testBlogger = new User();
        testBlogger.setId(2L);
        testBlogger.setPhone("13800138001");
        testBlogger.setEmail("blogger@example.com");
        testBlogger.setRole(User.UserRole.BLOGGER);
    }
    
    @Test
    void testApplyForBlogger_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bloggerApplicationRepository.existsByUserIdAndStatus(1L, BloggerApplication.ApplicationStatus.PENDING))
            .thenReturn(false);
        
        BloggerApplication savedApplication = new BloggerApplication();
        savedApplication.setId(1L);
        savedApplication.setUserId(1L);
        savedApplication.setNickname("TestBlogger");
        savedApplication.setBio("Test bio");
        savedApplication.setStatus(BloggerApplication.ApplicationStatus.PENDING);
        
        when(bloggerApplicationRepository.save(any(BloggerApplication.class))).thenReturn(savedApplication);
        
        // When
        BloggerApplication result = bloggerService.applyForBlogger(1L, "TestBlogger", "Test bio");
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TestBlogger", result.getNickname());
        assertEquals("Test bio", result.getBio());
        assertEquals(BloggerApplication.ApplicationStatus.PENDING, result.getStatus());
        
        verify(bloggerApplicationRepository).save(any(BloggerApplication.class));
    }
    
    @Test
    void testApplyForBlogger_UserNotFound() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.applyForBlogger(999L, "TestBlogger", "Test bio"));
        
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        assertEquals("用户不存在", exception.getMessage());
    }
    
    @Test
    void testApplyForBlogger_AlreadyBlogger() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.applyForBlogger(2L, "TestBlogger", "Test bio"));
        
        assertEquals("ALREADY_BLOGGER", exception.getErrorCode());
        assertEquals("用户已经是博主", exception.getMessage());
    }
    
    @Test
    void testApplyForBlogger_PendingApplicationExists() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bloggerApplicationRepository.existsByUserIdAndStatus(1L, BloggerApplication.ApplicationStatus.PENDING))
            .thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.applyForBlogger(1L, "TestBlogger", "Test bio"));
        
        assertEquals("APPLICATION_PENDING", exception.getErrorCode());
        assertEquals("已有待审核的博主申请", exception.getMessage());
    }
    
    @Test
    void testApplyForBlogger_EmptyNickname() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bloggerApplicationRepository.existsByUserIdAndStatus(1L, BloggerApplication.ApplicationStatus.PENDING))
            .thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.applyForBlogger(1L, "", "Test bio"));
        
        assertEquals("INVALID_NICKNAME", exception.getErrorCode());
        assertEquals("博主昵称不能为空", exception.getMessage());
    }
    
    @Test
    void testGetApplicationStatus_Found() {
        // Given
        BloggerApplication application = new BloggerApplication();
        application.setId(1L);
        application.setUserId(1L);
        application.setStatus(BloggerApplication.ApplicationStatus.PENDING);
        
        when(bloggerApplicationRepository.findByUserId(1L)).thenReturn(Optional.of(application));
        
        // When
        Optional<BloggerApplication> result = bloggerService.getApplicationStatus(1L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(BloggerApplication.ApplicationStatus.PENDING, result.get().getStatus());
    }
    
    @Test
    void testGetApplicationStatus_NotFound() {
        // Given
        when(bloggerApplicationRepository.findByUserId(1L)).thenReturn(Optional.empty());
        
        // When
        Optional<BloggerApplication> result = bloggerService.getApplicationStatus(1L);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void testSaveAnnouncement_CreateNew() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        when(announcementRepository.findByUserId(2L)).thenReturn(Optional.empty());
        
        Announcement savedAnnouncement = new Announcement();
        savedAnnouncement.setId(1L);
        savedAnnouncement.setUserId(2L);
        savedAnnouncement.setContent("Test announcement");
        
        when(announcementRepository.save(any(Announcement.class))).thenReturn(savedAnnouncement);
        
        // When
        Announcement result = bloggerService.saveAnnouncement(2L, "Test announcement");
        
        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test announcement", result.getContent());
        
        verify(announcementRepository).save(any(Announcement.class));
    }
    
    @Test
    void testSaveAnnouncement_UpdateExisting() {
        // Given
        Announcement existingAnnouncement = new Announcement();
        existingAnnouncement.setId(1L);
        existingAnnouncement.setUserId(2L);
        existingAnnouncement.setContent("Old announcement");
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        when(announcementRepository.findByUserId(2L)).thenReturn(Optional.of(existingAnnouncement));
        
        Announcement updatedAnnouncement = new Announcement();
        updatedAnnouncement.setId(1L);
        updatedAnnouncement.setUserId(2L);
        updatedAnnouncement.setContent("New announcement");
        
        when(announcementRepository.save(any(Announcement.class))).thenReturn(updatedAnnouncement);
        
        // When
        Announcement result = bloggerService.saveAnnouncement(2L, "New announcement");
        
        // Then
        assertNotNull(result);
        assertEquals("New announcement", result.getContent());
        
        verify(announcementRepository).save(any(Announcement.class));
    }
    
    @Test
    void testSaveAnnouncement_NotBlogger() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.saveAnnouncement(1L, "Test announcement"));
        
        assertEquals("NOT_BLOGGER", exception.getErrorCode());
        assertEquals("用户不是博主", exception.getMessage());
    }
    
    @Test
    void testSaveAnnouncement_EmptyContent() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.saveAnnouncement(2L, ""));
        
        assertEquals("INVALID_CONTENT", exception.getErrorCode());
        assertEquals("公告内容不能为空", exception.getMessage());
    }
    
    @Test
    void testGetAnnouncement_Found() {
        // Given
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setUserId(2L);
        announcement.setContent("Test announcement");
        
        when(announcementRepository.findByUserId(2L)).thenReturn(Optional.of(announcement));
        
        // When
        Optional<Announcement> result = bloggerService.getAnnouncement(2L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("Test announcement", result.get().getContent());
    }
    
    @Test
    void testDeleteAnnouncement_Success() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        doNothing().when(announcementRepository).deleteByUserId(2L);
        
        // When
        bloggerService.deleteAnnouncement(2L);
        
        // Then
        verify(announcementRepository).deleteByUserId(2L);
    }
    
    @Test
    void testDeleteAnnouncement_NotBlogger() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.deleteAnnouncement(1L));
        
        assertEquals("NOT_BLOGGER", exception.getErrorCode());
    }
    
    @Test
    void testSaveSpaceSettings_CreateNew() {
        // Given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        when(spaceSettingRepository.findByUserId(2L)).thenReturn(Optional.empty());
        
        SpaceSetting savedSettings = new SpaceSetting();
        savedSettings.setId(1L);
        savedSettings.setUserId(2L);
        savedSettings.setThemeColor("#FF0000");
        savedSettings.setBackgroundImage("bg.jpg");
        savedSettings.setLayoutStyle(SpaceSetting.LayoutStyle.DOUBLE);
        
        when(spaceSettingRepository.save(any(SpaceSetting.class))).thenReturn(savedSettings);
        
        // When
        SpaceSetting result = bloggerService.saveSpaceSettings(2L, "#FF0000", "bg.jpg", 
            SpaceSetting.LayoutStyle.DOUBLE);
        
        // Then
        assertNotNull(result);
        assertEquals("#FF0000", result.getThemeColor());
        assertEquals("bg.jpg", result.getBackgroundImage());
        assertEquals(SpaceSetting.LayoutStyle.DOUBLE, result.getLayoutStyle());
        
        verify(spaceSettingRepository).save(any(SpaceSetting.class));
    }
    
    @Test
    void testSaveSpaceSettings_UpdateExisting() {
        // Given
        SpaceSetting existingSettings = new SpaceSetting();
        existingSettings.setId(1L);
        existingSettings.setUserId(2L);
        existingSettings.setThemeColor("#409EFF");
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        when(spaceSettingRepository.findByUserId(2L)).thenReturn(Optional.of(existingSettings));
        
        SpaceSetting updatedSettings = new SpaceSetting();
        updatedSettings.setId(1L);
        updatedSettings.setUserId(2L);
        updatedSettings.setThemeColor("#FF0000");
        
        when(spaceSettingRepository.save(any(SpaceSetting.class))).thenReturn(updatedSettings);
        
        // When
        SpaceSetting result = bloggerService.saveSpaceSettings(2L, "#FF0000", null, null);
        
        // Then
        assertNotNull(result);
        assertEquals("#FF0000", result.getThemeColor());
        
        verify(spaceSettingRepository).save(any(SpaceSetting.class));
    }
    
    @Test
    void testSaveSpaceSettings_NotBlogger() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.saveSpaceSettings(1L, "#FF0000", null, null));
        
        assertEquals("NOT_BLOGGER", exception.getErrorCode());
    }
    
    @Test
    void testGetSpaceSettings_Found() {
        // Given
        SpaceSetting settings = new SpaceSetting();
        settings.setId(1L);
        settings.setUserId(2L);
        settings.setThemeColor("#409EFF");
        
        when(spaceSettingRepository.findByUserId(2L)).thenReturn(Optional.of(settings));
        
        // When
        Optional<SpaceSetting> result = bloggerService.getSpaceSettings(2L);
        
        // Then
        assertTrue(result.isPresent());
        assertEquals("#409EFF", result.get().getThemeColor());
    }
    
    @Test
    void testResetSpaceSettings_Success() {
        // Given
        SpaceSetting existingSettings = new SpaceSetting();
        existingSettings.setId(1L);
        existingSettings.setUserId(2L);
        existingSettings.setThemeColor("#FF0000");
        existingSettings.setBackgroundImage("custom.jpg");
        existingSettings.setLayoutStyle(SpaceSetting.LayoutStyle.DOUBLE);
        
        when(userRepository.findById(2L)).thenReturn(Optional.of(testBlogger));
        when(spaceSettingRepository.findByUserId(2L)).thenReturn(Optional.of(existingSettings));
        
        SpaceSetting resetSettings = new SpaceSetting();
        resetSettings.setId(1L);
        resetSettings.setUserId(2L);
        resetSettings.setThemeColor("#409EFF");
        resetSettings.setBackgroundImage(null);
        resetSettings.setLayoutStyle(SpaceSetting.LayoutStyle.CARD);
        
        when(spaceSettingRepository.save(any(SpaceSetting.class))).thenReturn(resetSettings);
        
        // When
        SpaceSetting result = bloggerService.resetSpaceSettings(2L);
        
        // Then
        assertNotNull(result);
        assertEquals("#409EFF", result.getThemeColor());
        assertNull(result.getBackgroundImage());
        assertEquals(SpaceSetting.LayoutStyle.CARD, result.getLayoutStyle());
        
        verify(spaceSettingRepository).save(any(SpaceSetting.class));
    }
    
    @Test
    void testResetSpaceSettings_NotBlogger() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> bloggerService.resetSpaceSettings(1L));
        
        assertEquals("NOT_BLOGGER", exception.getErrorCode());
    }
}
