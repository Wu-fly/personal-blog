package com.blog.service;

import com.blog.entity.SensitiveWord;
import com.blog.exception.BusinessException;
import com.blog.repository.SensitiveWordRepository;
import com.blog.service.impl.SecurityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SecurityService单元测试
 * 需求: 25.1-25.7, 11.1-11.5
 */
@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private SensitiveWordRepository sensitiveWordRepository;

    @InjectMocks
    private SecurityServiceImpl securityService;

    @BeforeEach
    void setUp() {
        // 模拟敏感词库
        when(sensitiveWordRepository.findAllWords())
            .thenReturn(Arrays.asList("敏感词", "违禁词", "色情", "暴力", "fuck", "shit"));
        
        // 初始化敏感词库
        securityService.init();
    }

    @Test
    void testContainsSensitiveWord_WithSensitiveWord_ReturnsTrue() {
        // 测试包含敏感词的内容
        assertTrue(securityService.containsSensitiveWord("这是一个敏感词测试"));
        assertTrue(securityService.containsSensitiveWord("包含违禁词的内容"));
        assertTrue(securityService.containsSensitiveWord("This is a fuck test"));
    }

    @Test
    void testContainsSensitiveWord_WithoutSensitiveWord_ReturnsFalse() {
        // 测试不包含敏感词的内容
        assertFalse(securityService.containsSensitiveWord("这是正常的内容"));
        assertFalse(securityService.containsSensitiveWord("This is a normal test"));
    }

    @Test
    void testContainsSensitiveWord_WithEmptyContent_ReturnsFalse() {
        // 测试空内容
        assertFalse(securityService.containsSensitiveWord(""));
        assertFalse(securityService.containsSensitiveWord(null));
    }

    @Test
    void testFindSensitiveWords_WithMultipleSensitiveWords_ReturnsAllMatches() {
        // 测试查找多个敏感词
        List<String> found = securityService.findSensitiveWords("这个内容包含敏感词和违禁词");
        
        assertEquals(2, found.size());
        assertTrue(found.contains("敏感词"));
        assertTrue(found.contains("违禁词"));
    }

    @Test
    void testFindSensitiveWords_WithoutSensitiveWords_ReturnsEmptyList() {
        // 测试不包含敏感词的内容
        List<String> found = securityService.findSensitiveWords("这是正常的内容");
        
        assertTrue(found.isEmpty());
    }

    @Test
    void testContainsSqlInjection_WithSqlInjectionPattern_ReturnsTrue() {
        // 测试SQL注入检测
        assertTrue(securityService.containsSqlInjection("SELECT * FROM users"));
        assertTrue(securityService.containsSqlInjection("1' OR '1'='1"));
        assertTrue(securityService.containsSqlInjection("admin'--"));
        assertTrue(securityService.containsSqlInjection("1 OR 1=1"));
        assertTrue(securityService.containsSqlInjection("'; DROP TABLE users--"));
        assertTrue(securityService.containsSqlInjection("UNION SELECT password FROM users"));
        assertTrue(securityService.containsSqlInjection("' OR 1=1--"));
        assertTrue(securityService.containsSqlInjection("admin' AND 1=1--"));
    }

    @Test
    void testContainsSqlInjection_WithNormalInput_ReturnsFalse() {
        // 测试正常输入
        assertFalse(securityService.containsSqlInjection("这是正常的文章标题"));
        assertFalse(securityService.containsSqlInjection("My article about technology"));
        assertFalse(securityService.containsSqlInjection("用户名: admin"));
    }

    @Test
    void testContainsSqlInjection_WithEmptyInput_ReturnsFalse() {
        // 测试空输入
        assertFalse(securityService.containsSqlInjection(""));
        assertFalse(securityService.containsSqlInjection(null));
    }

    @Test
    void testContainsXss_WithXssPattern_ReturnsTrue() {
        // 测试XSS攻击检测
        assertTrue(securityService.containsXss("<script>alert('XSS')</script>"));
        assertTrue(securityService.containsXss("<img src=x onerror=alert('XSS')>"));
        assertTrue(securityService.containsXss("<iframe src='javascript:alert(1)'></iframe>"));
        assertTrue(securityService.containsXss("<body onload=alert('XSS')>"));
        assertTrue(securityService.containsXss("javascript:alert('XSS')"));
        assertTrue(securityService.containsXss("<svg onload=alert('XSS')>"));
    }

    @Test
    void testContainsXss_WithNormalInput_ReturnsFalse() {
        // 测试正常输入
        assertFalse(securityService.containsXss("这是正常的文章内容"));
        assertFalse(securityService.containsXss("<p>This is a paragraph</p>"));
        assertFalse(securityService.containsXss("<div>Normal HTML content</div>"));
    }

    @Test
    void testContainsXss_WithEmptyInput_ReturnsFalse() {
        // 测试空输入
        assertFalse(securityService.containsXss(""));
        assertFalse(securityService.containsXss(null));
    }

    @Test
    void testEscapeXss_WithHtmlContent_ReturnsEscapedContent() {
        // 测试HTML转义
        String input = "<script>alert('XSS')</script>";
        String escaped = securityService.escapeXss(input);
        
        assertNotNull(escaped);
        assertFalse(escaped.contains("<script>"));
        assertTrue(escaped.contains("&lt;script&gt;"));
    }

    @Test
    void testEscapeXss_WithNormalContent_ReturnsOriginalContent() {
        // 测试正常内容转义
        String input = "这是正常的内容";
        String escaped = securityService.escapeXss(input);
        
        assertEquals(input, escaped);
    }

    @Test
    void testEscapeXss_WithNullInput_ReturnsNull() {
        // 测试null输入
        assertNull(securityService.escapeXss(null));
    }

    @Test
    void testAddSensitiveWord_WithValidWord_Success() {
        // 准备测试数据
        String word = "新敏感词";
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word);

        when(sensitiveWordRepository.existsByWord(word)).thenReturn(false);
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenReturn(sensitiveWord);
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList(word));

        // 执行测试
        SensitiveWord result = securityService.addSensitiveWord(word);

        // 验证结果
        assertNotNull(result);
        assertEquals(word, result.getWord());
        verify(sensitiveWordRepository).save(any(SensitiveWord.class));
    }

    @Test
    void testAddSensitiveWord_WithExistingWord_ThrowsException() {
        // 准备测试数据
        String word = "已存在的敏感词";

        when(sensitiveWordRepository.existsByWord(word)).thenReturn(true);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            securityService.addSensitiveWord(word);
        });

        verify(sensitiveWordRepository, never()).save(any(SensitiveWord.class));
    }

    @Test
    void testAddSensitiveWord_WithEmptyWord_ThrowsException() {
        // 测试空敏感词
        assertThrows(BusinessException.class, () -> {
            securityService.addSensitiveWord("");
        });

        assertThrows(BusinessException.class, () -> {
            securityService.addSensitiveWord(null);
        });

        verify(sensitiveWordRepository, never()).save(any(SensitiveWord.class));
    }

    @Test
    void testDeleteSensitiveWord_WithExistingWord_Success() {
        // 准备测试数据
        String word = "要删除的敏感词";

        when(sensitiveWordRepository.existsByWord(word)).thenReturn(true);
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());

        // 执行测试
        securityService.deleteSensitiveWord(word);

        // 验证结果
        verify(sensitiveWordRepository).deleteByWord(word);
    }

    @Test
    void testDeleteSensitiveWord_WithNonExistingWord_ThrowsException() {
        // 准备测试数据
        String word = "不存在的敏感词";

        when(sensitiveWordRepository.existsByWord(word)).thenReturn(false);

        // 执行测试并验证异常
        assertThrows(BusinessException.class, () -> {
            securityService.deleteSensitiveWord(word);
        });

        verify(sensitiveWordRepository, never()).deleteByWord(anyString());
    }

    @Test
    void testGetAllSensitiveWords_ReturnsAllWords() {
        // 准备测试数据
        SensitiveWord word1 = new SensitiveWord();
        word1.setWord("敏感词1");
        SensitiveWord word2 = new SensitiveWord();
        word2.setWord("敏感词2");

        when(sensitiveWordRepository.findAll()).thenReturn(Arrays.asList(word1, word2));

        // 执行测试
        List<SensitiveWord> result = securityService.getAllSensitiveWords();

        // 验证结果
        assertEquals(2, result.size());
        verify(sensitiveWordRepository).findAll();
    }

    @Test
    void testImportSensitiveWords_WithValidWords_Success() {
        // 准备测试数据
        List<String> words = Arrays.asList("新词1", "新词2", "新词3");

        when(sensitiveWordRepository.existsByWord(anyString())).thenReturn(false);
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sensitiveWordRepository.findAllWords()).thenReturn(words);

        // 执行测试
        securityService.importSensitiveWords(words);

        // 验证结果
        verify(sensitiveWordRepository, times(3)).save(any(SensitiveWord.class));
    }

    @Test
    void testImportSensitiveWords_WithDuplicateWords_SkipsDuplicates() {
        // 准备测试数据
        List<String> words = Arrays.asList("新词1", "已存在词", "新词2");

        when(sensitiveWordRepository.existsByWord("新词1")).thenReturn(false);
        when(sensitiveWordRepository.existsByWord("已存在词")).thenReturn(true);
        when(sensitiveWordRepository.existsByWord("新词2")).thenReturn(false);
        when(sensitiveWordRepository.save(any(SensitiveWord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList("新词1", "新词2"));

        // 执行测试
        securityService.importSensitiveWords(words);

        // 验证结果 - 只保存了2个新词
        verify(sensitiveWordRepository, times(2)).save(any(SensitiveWord.class));
    }

    @Test
    void testReloadSensitiveWords_Success() {
        // 准备测试数据
        List<String> words = Arrays.asList("重新加载词1", "重新加载词2");

        when(sensitiveWordRepository.findAllWords()).thenReturn(words);

        // 执行测试
        securityService.reloadSensitiveWords();

        // 验证结果 - 调用了至少一次
        verify(sensitiveWordRepository, atLeastOnce()).findAllWords();
    }
}
