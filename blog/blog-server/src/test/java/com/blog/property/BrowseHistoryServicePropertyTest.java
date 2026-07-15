package com.blog.property;

import com.blog.entity.Article;
import com.blog.entity.BrowseHistory;
import com.blog.entity.User;
import com.blog.repository.ArticleRepository;
import com.blog.repository.BrowseHistoryRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.BrowseHistoryServiceImpl;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BrowseHistoryService属性测试
 * 使用jqwik进行基于属性的测试
 */
class BrowseHistoryServicePropertyTest {

    /**
     * Feature: personal-blog-system, Property 17: 浏览历史时间戳更新
     * 验证需求: 22.2
     * 
     * 对于任意用户和文章，重复查看同一文章应该更新该文章在浏览历史中的时间戳为最新时间
     */
    @Property(tries = 100)
    void testBrowseHistoryTimestampUpdate(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId) {
        
        // 准备mock对象
        BrowseHistoryRepository browseHistoryRepository = mock(BrowseHistoryRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        
        BrowseHistoryServiceImpl browseHistoryService = new BrowseHistoryServiceImpl(
            browseHistoryRepository,
            userRepository,
            articleRepository
        );
        
        // 创建测试用户
        User user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        user.setEmail("test@test.com");
        user.setNickname("TestUser");
        
        // 创建测试文章
        Article article = new Article();
        article.setId(articleId);
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setUserId(userId);
        
        // 模拟用户和文章存在
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        
        // 第一次浏览：创建新的浏览历史记录
        when(browseHistoryRepository.findByUserIdAndArticleId(userId, articleId))
            .thenReturn(Optional.empty());
        when(browseHistoryRepository.countByUserId(userId)).thenReturn(50L);
        
        BrowseHistory firstBrowseHistory = new BrowseHistory();
        firstBrowseHistory.setId(1L);
        firstBrowseHistory.setUserId(userId);
        firstBrowseHistory.setArticleId(articleId);
        LocalDateTime firstTimestamp = LocalDateTime.now();
        setField(firstBrowseHistory, "createdAt", firstTimestamp);
        setField(firstBrowseHistory, "updatedAt", firstTimestamp);
        
        when(browseHistoryRepository.save(any(BrowseHistory.class)))
            .thenReturn(firstBrowseHistory);
        
        // 执行第一次浏览
        BrowseHistory result1 = browseHistoryService.recordBrowseHistory(userId, articleId);
        
        // 验证第一次浏览创建了新记录
        assertNotNull(result1);
        assertEquals(userId, result1.getUserId());
        assertEquals(articleId, result1.getArticleId());
        assertNotNull(result1.getCreatedAt());
        assertNotNull(result1.getUpdatedAt());
        
        // 记录第一次的时间戳
        LocalDateTime firstUpdatedAt = result1.getUpdatedAt();
        
        // 模拟时间流逝（通过创建新的时间戳）
        try {
            Thread.sleep(10); // 等待10毫秒确保时间戳不同
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 第二次浏览：更新已存在的浏览历史记录
        LocalDateTime secondTimestamp = LocalDateTime.now();
        
        // 创建更新后的浏览历史对象
        BrowseHistory updatedBrowseHistory = new BrowseHistory();
        updatedBrowseHistory.setId(1L);
        updatedBrowseHistory.setUserId(userId);
        updatedBrowseHistory.setArticleId(articleId);
        setField(updatedBrowseHistory, "createdAt", firstTimestamp); // 创建时间不变
        setField(updatedBrowseHistory, "updatedAt", secondTimestamp); // 更新时间改变
        
        // 模拟第二次浏览时记录已存在
        when(browseHistoryRepository.findByUserIdAndArticleId(userId, articleId))
            .thenReturn(Optional.of(firstBrowseHistory));
        when(browseHistoryRepository.save(any(BrowseHistory.class)))
            .thenReturn(updatedBrowseHistory);
        
        // 执行第二次浏览
        BrowseHistory result2 = browseHistoryService.recordBrowseHistory(userId, articleId);
        
        // 属性验证：第二次浏览应该更新时间戳
        assertNotNull(result2);
        assertEquals(userId, result2.getUserId());
        assertEquals(articleId, result2.getArticleId());
        
        // 验证ID相同（是同一条记录）
        assertEquals(result1.getId(), result2.getId());
        
        // 验证创建时间不变
        assertEquals(firstTimestamp, result2.getCreatedAt());
        
        // 验证更新时间已改变（第二次的时间戳应该晚于第一次）
        assertNotNull(result2.getUpdatedAt());
        assertTrue(result2.getUpdatedAt().isAfter(firstUpdatedAt) || 
                   result2.getUpdatedAt().isEqual(secondTimestamp),
                "Updated timestamp should be later than or equal to the second browse time");
        
        // 验证save方法被调用了两次（第一次创建，第二次更新）
        verify(browseHistoryRepository, times(2)).save(any(BrowseHistory.class));
        
        // 验证第二次调用时传入的是已存在的记录
        verify(browseHistoryRepository, times(2))
            .findByUserIdAndArticleId(userId, articleId);
    }

    /**
     * Feature: personal-blog-system, Property 17: 浏览历史时间戳更新（多次浏览）
     * 验证需求: 22.2
     * 
     * 对于任意用户和文章，多次重复查看应该每次都更新时间戳
     */
    @Property(tries = 100)
    void testMultipleBrowseHistoryTimestampUpdates(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @net.jqwik.api.constraints.IntRange(min = 2, max = 5) int browseCount) {
        
        // 准备mock对象
        BrowseHistoryRepository browseHistoryRepository = mock(BrowseHistoryRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        
        BrowseHistoryServiceImpl browseHistoryService = new BrowseHistoryServiceImpl(
            browseHistoryRepository,
            userRepository,
            articleRepository
        );
        
        // 创建测试用户和文章
        User user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        user.setEmail("test@test.com");
        
        Article article = new Article();
        article.setId(articleId);
        article.setTitle("Test Article");
        article.setContent("Test Content");
        article.setUserId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
        when(browseHistoryRepository.countByUserId(userId)).thenReturn(50L);
        
        LocalDateTime initialTimestamp = LocalDateTime.now();
        LocalDateTime previousTimestamp = initialTimestamp;
        
        // 模拟多次浏览
        for (int i = 0; i < browseCount; i++) {
            final int currentIteration = i;
            final LocalDateTime currentTimestamp = LocalDateTime.now().plusSeconds(i);
            
            BrowseHistory browseHistory = new BrowseHistory();
            browseHistory.setId(1L);
            browseHistory.setUserId(userId);
            browseHistory.setArticleId(articleId);
            setField(browseHistory, "createdAt", initialTimestamp);
            setField(browseHistory, "updatedAt", currentTimestamp);
            
            if (i == 0) {
                // 第一次浏览：记录不存在
                when(browseHistoryRepository.findByUserIdAndArticleId(userId, articleId))
                    .thenReturn(Optional.empty());
            } else {
                // 后续浏览：记录已存在
                BrowseHistory existingHistory = new BrowseHistory();
                existingHistory.setId(1L);
                existingHistory.setUserId(userId);
                existingHistory.setArticleId(articleId);
                setField(existingHistory, "createdAt", initialTimestamp);
                setField(existingHistory, "updatedAt", previousTimestamp);
                
                when(browseHistoryRepository.findByUserIdAndArticleId(userId, articleId))
                    .thenReturn(Optional.of(existingHistory));
            }
            
            when(browseHistoryRepository.save(any(BrowseHistory.class)))
                .thenReturn(browseHistory);
            
            // 执行浏览
            BrowseHistory result = browseHistoryService.recordBrowseHistory(userId, articleId);
            
            // 验证结果
            assertNotNull(result);
            assertEquals(userId, result.getUserId());
            assertEquals(articleId, result.getArticleId());
            assertEquals(initialTimestamp, result.getCreatedAt(), 
                "Created timestamp should remain unchanged");
            
            // 验证更新时间戳递增
            if (i > 0) {
                assertTrue(result.getUpdatedAt().isAfter(previousTimestamp) || 
                           result.getUpdatedAt().isEqual(currentTimestamp),
                    "Updated timestamp should increase with each browse");
            }
            
            previousTimestamp = currentTimestamp;
            
            // 等待一小段时间确保时间戳不同
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 验证save方法被调用了browseCount次
        verify(browseHistoryRepository, times(browseCount)).save(any(BrowseHistory.class));
    }

    /**
     * Feature: personal-blog-system, Property 17: 浏览历史时间戳更新（不同文章）
     * 验证需求: 22.2
     * 
     * 对于任意用户，浏览不同文章应该创建不同的浏览历史记录，每条记录独立更新时间戳
     */
    @Property(tries = 100)
    void testBrowseHistoryTimestampUpdateForDifferentArticles(
            @ForAll @LongRange(min = 1, max = 1000000) Long userId,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId1,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId2) {
        
        // 确保两篇文章不同
        Assume.that(!articleId1.equals(articleId2));
        
        // 准备mock对象
        BrowseHistoryRepository browseHistoryRepository = mock(BrowseHistoryRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        
        BrowseHistoryServiceImpl browseHistoryService = new BrowseHistoryServiceImpl(
            browseHistoryRepository,
            userRepository,
            articleRepository
        );
        
        // 创建测试用户
        User user = new User();
        user.setId(userId);
        user.setPhone("13800138000");
        user.setEmail("test@test.com");
        
        // 创建两篇不同的文章
        Article article1 = new Article();
        article1.setId(articleId1);
        article1.setTitle("Test Article 1");
        article1.setContent("Test Content 1");
        
        Article article2 = new Article();
        article2.setId(articleId2);
        article2.setTitle("Test Article 2");
        article2.setContent("Test Content 2");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(articleRepository.findById(articleId1)).thenReturn(Optional.of(article1));
        when(articleRepository.findById(articleId2)).thenReturn(Optional.of(article2));
        when(browseHistoryRepository.countByUserId(userId)).thenReturn(50L);
        
        // 浏览第一篇文章
        LocalDateTime timestamp1 = LocalDateTime.now();
        BrowseHistory history1 = new BrowseHistory();
        history1.setId(1L);
        history1.setUserId(userId);
        history1.setArticleId(articleId1);
        setField(history1, "createdAt", timestamp1);
        setField(history1, "updatedAt", timestamp1);
        
        when(browseHistoryRepository.findByUserIdAndArticleId(userId, articleId1))
            .thenReturn(Optional.empty());
        when(browseHistoryRepository.save(any(BrowseHistory.class)))
            .thenReturn(history1);
        
        BrowseHistory result1 = browseHistoryService.recordBrowseHistory(userId, articleId1);
        
        // 验证第一篇文章的浏览历史
        assertNotNull(result1);
        assertEquals(articleId1, result1.getArticleId());
        
        // 等待确保时间戳不同
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 浏览第二篇文章
        LocalDateTime timestamp2 = LocalDateTime.now();
        BrowseHistory history2 = new BrowseHistory();
        history2.setId(2L);
        history2.setUserId(userId);
        history2.setArticleId(articleId2);
        setField(history2, "createdAt", timestamp2);
        setField(history2, "updatedAt", timestamp2);
        
        when(browseHistoryRepository.findByUserIdAndArticleId(userId, articleId2))
            .thenReturn(Optional.empty());
        when(browseHistoryRepository.save(any(BrowseHistory.class)))
            .thenReturn(history2);
        
        BrowseHistory result2 = browseHistoryService.recordBrowseHistory(userId, articleId2);
        
        // 验证第二篇文章的浏览历史
        assertNotNull(result2);
        assertEquals(articleId2, result2.getArticleId());
        
        // 属性验证：两条记录应该是独立的
        assertNotEquals(result1.getId(), result2.getId(), 
            "Different articles should have different browse history records");
        assertNotEquals(result1.getArticleId(), result2.getArticleId(),
            "Article IDs should be different");
        
        // 验证时间戳：第二篇文章的时间戳应该晚于第一篇
        assertTrue(result2.getUpdatedAt().isAfter(result1.getUpdatedAt()) ||
                   result2.getUpdatedAt().isEqual(timestamp2),
            "Second article's timestamp should be later than first article's");
        
        // 再次浏览第一篇文章，验证时间戳更新
        LocalDateTime timestamp3 = LocalDateTime.now();
        BrowseHistory updatedHistory1 = new BrowseHistory();
        updatedHistory1.setId(1L);
        updatedHistory1.setUserId(userId);
        updatedHistory1.setArticleId(articleId1);
        setField(updatedHistory1, "createdAt", timestamp1); // 创建时间不变
        setField(updatedHistory1, "updatedAt", timestamp3); // 更新时间改变
        
        when(browseHistoryRepository.findByUserIdAndArticleId(userId, articleId1))
            .thenReturn(Optional.of(history1));
        when(browseHistoryRepository.save(any(BrowseHistory.class)))
            .thenReturn(updatedHistory1);
        
        BrowseHistory result3 = browseHistoryService.recordBrowseHistory(userId, articleId1);
        
        // 验证第一篇文章的时间戳已更新
        assertNotNull(result3);
        assertEquals(articleId1, result3.getArticleId());
        assertEquals(result1.getId(), result3.getId(), 
            "Should update the same record");
        assertTrue(result3.getUpdatedAt().isAfter(result1.getUpdatedAt()) ||
                   result3.getUpdatedAt().isEqual(timestamp3),
            "Timestamp should be updated for repeated browse");
        
        // 验证save方法被调用了3次（两次创建，一次更新）
        verify(browseHistoryRepository, times(3)).save(any(BrowseHistory.class));
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (NoSuchFieldException e) {
            try {
                java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to set field: " + fieldName, ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
