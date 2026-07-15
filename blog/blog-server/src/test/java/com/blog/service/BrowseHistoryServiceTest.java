package com.blog.service;

import com.blog.entity.Article;
import com.blog.entity.BrowseHistory;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.BrowseHistoryRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.BrowseHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * BrowseHistoryService单元测试
 * 需求: 22.1-22.5
 */
@ExtendWith(MockitoExtension.class)
class BrowseHistoryServiceTest {

    @Mock
    private BrowseHistoryRepository browseHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private BrowseHistoryServiceImpl browseHistoryService;

    private User user;
    private Article article;
    private BrowseHistory browseHistory;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        user = new User();
        user.setId(1L);
        user.setNickname("测试用户");
        user.setPhone("13800138001");
        user.setEmail("test@test.com");

        // 创建测试文章
        article = new Article();
        article.setId(1L);
        article.setTitle("测试文章");
        article.setContent("测试内容");
        article.setUserId(2L);

        // 创建测试浏览历史
        browseHistory = new BrowseHistory();
        browseHistory.setId(1L);
        browseHistory.setUserId(1L);
        browseHistory.setArticleId(1L);
    }

    /**
     * 测试记录新的浏览历史成功
     * 需求: 22.1
     */
    @Test
    void testRecordBrowseHistory_NewRecord_Success() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(browseHistoryRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.empty());
        when(browseHistoryRepository.save(any(BrowseHistory.class))).thenAnswer(invocation -> {
            BrowseHistory history = invocation.getArgument(0);
            history.setId(1L);
            return history;
        });
        when(browseHistoryRepository.countByUserId(1L)).thenReturn(50L);

        // 执行测试
        BrowseHistory result = browseHistoryService.recordBrowseHistory(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(1L, result.getArticleId());
        verify(browseHistoryRepository, times(1)).save(any(BrowseHistory.class));
    }

    /**
     * 测试更新已存在的浏览历史时间戳
     * 需求: 22.2
     */
    @Test
    void testRecordBrowseHistory_UpdateExisting_Success() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(browseHistoryRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.of(browseHistory));
        when(browseHistoryRepository.save(any(BrowseHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 执行测试
        BrowseHistory result = browseHistoryService.recordBrowseHistory(1L, 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(browseHistoryRepository, times(1)).save(any(BrowseHistory.class));
    }

    /**
     * 测试记录浏览历史时用户不存在
     * 需求: 22.1
     */
    @Test
    void testRecordBrowseHistory_UserNotFound() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            browseHistoryService.recordBrowseHistory(1L, 1L);
        });

        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        assertEquals("用户不存在", exception.getMessage());
        verify(browseHistoryRepository, never()).save(any(BrowseHistory.class));
    }

    /**
     * 测试记录浏览历史时文章不存在
     * 需求: 22.1
     */
    @Test
    void testRecordBrowseHistory_ArticleNotFound() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            browseHistoryService.recordBrowseHistory(1L, 1L);
        });

        assertEquals("ARTICLE_NOT_FOUND", exception.getErrorCode());
        assertEquals("文章不存在", exception.getMessage());
        verify(browseHistoryRepository, never()).save(any(BrowseHistory.class));
    }

    /**
     * 测试超过100条记录时删除最旧的记录
     * 需求: 22.5
     */
    @Test
    void testRecordBrowseHistory_ExceedsLimit_DeleteOldest() {
        // 准备数据
        BrowseHistory oldestHistory = new BrowseHistory();
        oldestHistory.setId(2L);
        oldestHistory.setUserId(1L);
        oldestHistory.setArticleId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(browseHistoryRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.empty());
        when(browseHistoryRepository.save(any(BrowseHistory.class))).thenAnswer(invocation -> {
            BrowseHistory history = invocation.getArgument(0);
            history.setId(1L);
            return history;
        });
        when(browseHistoryRepository.countByUserId(1L)).thenReturn(101L);
        
        Page<BrowseHistory> oldestPage = new PageImpl<>(Arrays.asList(oldestHistory));
        when(browseHistoryRepository.findByUserIdOrderByUpdatedAtDesc(eq(1L), any(Pageable.class)))
            .thenReturn(oldestPage);
        doNothing().when(browseHistoryRepository).delete(any(BrowseHistory.class));

        // 执行测试
        BrowseHistory result = browseHistoryService.recordBrowseHistory(1L, 1L);

        // 验证结果
        assertNotNull(result);
        verify(browseHistoryRepository, times(1)).delete(oldestHistory);
    }

    /**
     * 测试查询浏览历史成功
     * 需求: 22.3, 22.4
     */
    @Test
    void testGetBrowseHistory_Success() {
        // 准备数据
        Pageable pageable = PageRequest.of(0, 10);
        List<BrowseHistory> histories = Arrays.asList(browseHistory);
        Page<BrowseHistory> historyPage = new PageImpl<>(histories, pageable, 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(browseHistoryRepository.findByUserIdOrderByUpdatedAtDesc(1L, pageable))
            .thenReturn(historyPage);

        // 执行测试
        Page<BrowseHistory> result = browseHistoryService.getBrowseHistory(1L, pageable);

        // 验证结果
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(browseHistory, result.getContent().get(0));
        verify(browseHistoryRepository, times(1)).findByUserIdOrderByUpdatedAtDesc(1L, pageable);
    }

    /**
     * 测试查询不存在用户的浏览历史失败
     * 需求: 22.3
     */
    @Test
    void testGetBrowseHistory_UserNotFound() {
        // 准备数据
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            browseHistoryService.getBrowseHistory(999L, pageable);
        });

        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        verify(browseHistoryRepository, never()).findByUserIdOrderByUpdatedAtDesc(anyLong(), any(Pageable.class));
    }

    /**
     * 测试删除浏览历史成功
     * 需求: 21.8
     */
    @Test
    void testDeleteBrowseHistory_Success() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(browseHistoryRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.of(browseHistory));
        doNothing().when(browseHistoryRepository).deleteByUserIdAndArticleId(1L, 1L);

        // 执行测试
        browseHistoryService.deleteBrowseHistory(1L, 1L);

        // 验证结果
        verify(browseHistoryRepository, times(1)).deleteByUserIdAndArticleId(1L, 1L);
    }

    /**
     * 测试删除不存在的浏览历史失败
     * 需求: 21.8
     */
    @Test
    void testDeleteBrowseHistory_HistoryNotFound() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(browseHistoryRepository.findByUserIdAndArticleId(1L, 1L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            browseHistoryService.deleteBrowseHistory(1L, 1L);
        });

        assertEquals("HISTORY_NOT_FOUND", exception.getErrorCode());
        assertEquals("浏览历史不存在", exception.getMessage());
        verify(browseHistoryRepository, never()).deleteByUserIdAndArticleId(anyLong(), anyLong());
    }

    /**
     * 测试删除用户的所有浏览历史成功
     * 需求: 21.8
     */
    @Test
    void testDeleteAllBrowseHistory_Success() {
        // 准备数据
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(browseHistoryRepository).deleteByUserId(1L);

        // 执行测试
        browseHistoryService.deleteAllBrowseHistory(1L);

        // 验证结果
        verify(browseHistoryRepository, times(1)).deleteByUserId(1L);
    }

    /**
     * 测试删除不存在用户的所有浏览历史失败
     * 需求: 21.8
     */
    @Test
    void testDeleteAllBrowseHistory_UserNotFound() {
        // 准备数据
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            browseHistoryService.deleteAllBrowseHistory(999L);
        });

        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
        verify(browseHistoryRepository, never()).deleteByUserId(anyLong());
    }
}
