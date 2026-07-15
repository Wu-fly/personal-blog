package com.blog.property;

import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CategoryRepository;
import com.blog.repository.TagRepository;
import com.blog.service.impl.SearchServiceImpl;
import net.jqwik.api.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SearchService属性测试
 * 使用jqwik进行基于属性的测试
 */
class SearchServicePropertyTest {

    /**
     * Feature: personal-blog-system, Property 9: 搜索结果包含关键词
     * 验证需求: 4.3
     * 
     * 对于任意搜索关键词，返回的所有文章的标题或内容都应该包含该关键词
     */
    @Property(tries = 100)
    void testSearchResultsContainKeyword(
            @ForAll("searchKeywords") String keyword,
            @ForAll("articleLists") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 过滤出包含关键词的文章（模拟数据库的LIKE查询）
        List<Article> matchingArticles = allArticles.stream()
                .filter(article -> 
                    article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    article.getContent().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(matchingArticles, pageable, matchingArticles.size());
        
        // 模拟repository返回匹配的文章
        when(articleRepository.searchByKeyword(eq(keyword), any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行搜索
        Page<Article> results = searchService.searchByKeyword(keyword, pageable);
        
        // 属性验证：所有返回的文章都应该包含关键词
        assertNotNull(results, "Search results should not be null");
        
        for (Article article : results.getContent()) {
            boolean containsInTitle = article.getTitle().toLowerCase().contains(keyword.toLowerCase());
            boolean containsInContent = article.getContent().toLowerCase().contains(keyword.toLowerCase());
            
            assertTrue(containsInTitle || containsInContent,
                    String.format("Article (id=%d, title='%s') should contain keyword '%s' in title or content",
                            article.getId(), article.getTitle(), keyword));
        }
        
        // 验证返回的文章数量与预期匹配
        assertEquals(matchingArticles.size(), results.getContent().size(),
                "Number of returned articles should match the number of articles containing the keyword");
        
        // 验证调用了正确的repository方法
        verify(articleRepository).searchByKeyword(eq(keyword), any(Pageable.class));
    }

    /**
     * Feature: personal-blog-system, Property 9: 搜索结果包含关键词（空关键词场景）
     * 验证需求: 4.3
     * 
     * 当搜索关键词为空时，应该返回所有已审核的文章
     */
    @Property(tries = 100)
    void testSearchWithEmptyKeyword(
            @ForAll("emptyOrWhitespaceStrings") String emptyKeyword,
            @ForAll("articleLists") List<Article> allApprovedArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(allApprovedArticles, pageable, allApprovedArticles.size());
        
        // 模拟repository返回所有已审核文章
        when(articleRepository.findApprovedArticles(any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行搜索
        Page<Article> results = searchService.searchByKeyword(emptyKeyword, pageable);
        
        // 属性验证：应该返回所有已审核的文章
        assertNotNull(results, "Search results should not be null");
        assertEquals(allApprovedArticles.size(), results.getContent().size(),
                "Empty keyword search should return all approved articles");
        
        // 验证所有返回的文章都是已审核状态
        for (Article article : results.getContent()) {
            assertEquals(Article.ReviewStatus.APPROVED, article.getReviewStatus(),
                    "All returned articles should be approved");
        }
        
        // 验证调用了正确的repository方法（应该调用findApprovedArticles而不是searchByKeyword）
        verify(articleRepository).findApprovedArticles(any(Pageable.class));
        verify(articleRepository, never()).searchByKeyword(anyString(), any(Pageable.class));
    }

    /**
     * Feature: personal-blog-system, Property 9: 搜索结果包含关键词（大小写不敏感）
     * 验证需求: 4.3
     * 
     * 搜索应该是大小写不敏感的，无论关键词是大写、小写还是混合大小写
     */
    @Property(tries = 100)
    void testSearchCaseInsensitivity(
            @ForAll("searchKeywords") String keyword,
            @ForAll("articleLists") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 创建不同大小写版本的关键词
        String lowerKeyword = keyword.toLowerCase();
        String upperKeyword = keyword.toUpperCase();
        String mixedKeyword = keyword.length() > 0 ? 
                Character.toUpperCase(keyword.charAt(0)) + keyword.substring(1).toLowerCase() : keyword;
        
        // 过滤出包含关键词的文章（大小写不敏感）
        List<Article> matchingArticles = allArticles.stream()
                .filter(article -> 
                    article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    article.getContent().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(matchingArticles, pageable, matchingArticles.size());
        
        // 模拟repository对不同大小写的关键词返回相同的结果
        when(articleRepository.searchByKeyword(eq(lowerKeyword), any(Pageable.class)))
                .thenReturn(expectedPage);
        when(articleRepository.searchByKeyword(eq(upperKeyword), any(Pageable.class)))
                .thenReturn(expectedPage);
        when(articleRepository.searchByKeyword(eq(mixedKeyword), any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行搜索（使用不同大小写的关键词）
        Page<Article> resultsLower = searchService.searchByKeyword(lowerKeyword, pageable);
        Page<Article> resultsUpper = searchService.searchByKeyword(upperKeyword, pageable);
        Page<Article> resultsMixed = searchService.searchByKeyword(mixedKeyword, pageable);
        
        // 属性验证：不同大小写的关键词应该返回相同数量的结果
        assertEquals(resultsLower.getTotalElements(), resultsUpper.getTotalElements(),
                "Lowercase and uppercase keywords should return same number of results");
        assertEquals(resultsLower.getTotalElements(), resultsMixed.getTotalElements(),
                "Lowercase and mixed-case keywords should return same number of results");
        
        // 验证所有结果都包含关键词（大小写不敏感）
        for (Article article : resultsLower.getContent()) {
            boolean containsKeyword = 
                    article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    article.getContent().toLowerCase().contains(keyword.toLowerCase());
            
            assertTrue(containsKeyword,
                    String.format("Article should contain keyword '%s' (case-insensitive)", keyword));
        }
    }

    /**
     * Feature: personal-blog-system, Property 9: 搜索结果包含关键词（分页正确性）
     * 验证需求: 4.3, 4.5
     * 
     * 搜索结果应该正确支持分页功能
     */
    @Property(tries = 100)
    void testSearchPaginationCorrectness(
            @ForAll("searchKeywords") String keyword,
            @ForAll("largeArticleLists") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 过滤出包含关键词的文章
        List<Article> matchingArticles = allArticles.stream()
                .filter(article -> 
                    article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    article.getContent().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
        
        // 如果没有匹配的文章，跳过此测试
        Assume.that(matchingArticles.size() > 0);
        
        // 测试不同的分页参数
        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) matchingArticles.size() / pageSize);
        
        for (int pageNum = 0; pageNum < Math.min(totalPages, 3); pageNum++) {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            
            // 计算当前页应该包含的文章
            int start = pageNum * pageSize;
            int end = Math.min(start + pageSize, matchingArticles.size());
            List<Article> pageArticles = matchingArticles.subList(start, end);
            
            Page<Article> expectedPage = new PageImpl<>(pageArticles, pageable, matchingArticles.size());
            
            // 模拟repository返回分页结果
            when(articleRepository.searchByKeyword(eq(keyword), eq(pageable)))
                    .thenReturn(expectedPage);
            
            // 执行搜索
            Page<Article> results = searchService.searchByKeyword(keyword, pageable);
            
            // 属性验证：分页信息应该正确
            assertNotNull(results, "Search results should not be null");
            assertEquals(pageNum, results.getNumber(), "Page number should match");
            assertEquals(pageSize, results.getSize(), "Page size should match");
            assertEquals(matchingArticles.size(), results.getTotalElements(), 
                    "Total elements should match total matching articles");
            assertEquals(totalPages, results.getTotalPages(), "Total pages should be correct");
            
            // 验证当前页的所有文章都包含关键词
            for (Article article : results.getContent()) {
                boolean containsKeyword = 
                        article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        article.getContent().toLowerCase().contains(keyword.toLowerCase());
                
                assertTrue(containsKeyword,
                        String.format("Article in page %d should contain keyword '%s'", pageNum, keyword));
            }
        }
    }

    /**
     * Feature: personal-blog-system, Property 9: 搜索结果包含关键词（特殊字符处理）
     * 验证需求: 4.3
     * 
     * 搜索应该正确处理包含特殊字符的关键词
     */
    @Property(tries = 100)
    void testSearchWithSpecialCharacters(
            @ForAll("keywordsWithSpecialChars") String keyword,
            @ForAll("articleLists") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 过滤出包含关键词的文章
        List<Article> matchingArticles = allArticles.stream()
                .filter(article -> 
                    article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    article.getContent().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
        
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(matchingArticles, pageable, matchingArticles.size());
        
        // 模拟repository返回匹配的文章
        when(articleRepository.searchByKeyword(eq(keyword), any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行搜索
        Page<Article> results = searchService.searchByKeyword(keyword, pageable);
        
        // 属性验证：所有返回的文章都应该包含关键词（包括特殊字符）
        assertNotNull(results, "Search results should not be null");
        
        for (Article article : results.getContent()) {
            boolean containsKeyword = 
                    article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    article.getContent().toLowerCase().contains(keyword.toLowerCase());
            
            assertTrue(containsKeyword,
                    String.format("Article should contain keyword '%s' with special characters", keyword));
        }
    }

    /**
     * 生成搜索关键词
     */
    @Provide
    Arbitrary<String> searchKeywords() {
        return Arbitraries.of(
                "技术", "AI", "人工智能", "Java", "Spring", "Vue",
                "博客", "文章", "教程", "开发", "编程", "测试",
                "数据库", "MySQL", "Redis", "架构", "设计", "算法"
        );
    }

    /**
     * 生成包含特殊字符的关键词
     */
    @Provide
    Arbitrary<String> keywordsWithSpecialChars() {
        return Arbitraries.of(
                "C++", "C#", ".NET", "Vue.js", "Node.js",
                "Spring Boot", "AI技术", "Web开发",
                "前端-后端", "数据库/缓存", "API接口"
        );
    }

    /**
     * 生成空字符串或空白字符串
     */
    @Provide
    Arbitrary<String> emptyOrWhitespaceStrings() {
        return Arbitraries.of("", " ", "  ", "\t", "\n", "   ");
    }

    /**
     * 生成文章列表
     */
    @Provide
    Arbitrary<List<Article>> articleLists() {
        return Arbitraries.integers().between(3, 10).flatMap(size -> {
            List<Arbitrary<Article>> articleArbitraries = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                articleArbitraries.add(articles());
            }
            return Combinators.combine(articleArbitraries).as(articles -> articles);
        });
    }

    /**
     * 生成大量文章列表（用于分页测试）
     */
    @Provide
    Arbitrary<List<Article>> largeArticleLists() {
        return Arbitraries.integers().between(10, 30).flatMap(size -> {
            List<Arbitrary<Article>> articleArbitraries = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                articleArbitraries.add(articles());
            }
            return Combinators.combine(articleArbitraries).as(articles -> articles);
        });
    }

    /**
     * 生成单个文章
     */
    @Provide
    Arbitrary<Article> articles() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L);
        Arbitrary<String> titles = articleTitles();
        Arbitrary<String> contents = articleContents();
        Arbitrary<Long> userIds = Arbitraries.longs().between(1L, 10000L);
        Arbitrary<Long> categoryIds = Arbitraries.longs().between(1L, 20L);
        
        return Combinators.combine(ids, titles, contents, userIds, categoryIds)
                .as((id, title, content, userId, categoryId) -> {
                    Article article = new Article();
                    article.setId(id);
                    article.setTitle(title);
                    article.setContent(content);
                    article.setUserId(userId);
                    article.setCategoryId(categoryId);
                    article.setReviewStatus(Article.ReviewStatus.APPROVED);
                    article.setIsPaid(false);
                    article.setPrice(BigDecimal.ZERO);
                    article.setIsPinned(false);
                    article.setViewCount(0);
                    article.setLikeCount(0);
                    article.setFavoriteCount(0);
                    article.setPurchaseCount(0);
                    article.setCreatedAt(LocalDateTime.now());
                    article.setUpdatedAt(LocalDateTime.now());
                    return article;
                });
    }

    /**
     * 生成文章标题（包含各种关键词）
     */
    @Provide
    Arbitrary<String> articleTitles() {
        return Arbitraries.of(
                "深入理解Java虚拟机技术",
                "AI人工智能在医疗领域的应用",
                "Spring Boot微服务架构设计",
                "Vue.js前端开发实战教程",
                "MySQL数据库性能优化指南",
                "Redis缓存技术详解",
                "算法与数据结构学习笔记",
                "Web开发最佳实践",
                "云计算与大数据技术",
                "移动应用开发入门",
                "区块链技术原理解析",
                "网络安全防护策略",
                "敏捷开发方法论",
                "DevOps实践经验分享",
                "容器化技术Docker入门"
        );
    }

    /**
     * 生成文章内容（包含各种关键词）
     */
    @Provide
    Arbitrary<String> articleContents() {
        return Arbitraries.of(
                "本文介绍了Java编程语言的核心技术和最佳实践，包括JVM原理、并发编程、性能优化等内容。",
                "人工智能AI技术正在改变我们的生活，本文探讨了机器学习、深度学习在实际项目中的应用。",
                "Spring框架是Java开发的首选框架，本教程详细讲解了Spring Boot的使用方法和架构设计。",
                "Vue.js是一个渐进式JavaScript框架，本文分享了Vue组件开发、状态管理、路由配置等前端技术。",
                "数据库MySQL是最流行的关系型数据库，本文介绍了索引优化、查询优化、事务处理等技术要点。",
                "Redis是高性能的缓存系统，本文讲解了Redis的数据结构、持久化机制、集群部署等内容。",
                "算法是编程的基础，本文总结了常用的排序算法、搜索算法、动态规划等算法思想。",
                "Web开发涉及前端和后端技术，本文介绍了HTML、CSS、JavaScript以及RESTful API设计。",
                "云计算和大数据技术正在推动数字化转型，本文探讨了云原生架构和数据分析技术。",
                "移动应用开发需要掌握Android和iOS平台的开发技术，本文分享了跨平台开发框架的使用经验。",
                "区块链技术是分布式账本技术，本文解析了区块链的工作原理、共识机制、智能合约等概念。",
                "网络安全是信息时代的重要课题，本文介绍了常见的安全威胁和防护措施。",
                "敏捷开发强调快速迭代和持续交付，本文分享了Scrum、看板等敏捷方法的实践经验。",
                "DevOps是开发和运维的结合，本文介绍了CI/CD流水线、自动化测试、容器化部署等实践。",
                "Docker容器技术简化了应用部署，本文讲解了Docker镜像、容器、编排等核心概念。"
        );
    }

    /**
     * Feature: personal-blog-system, Property 19: 文章筛选正确性
     * 验证需求: 29.2
     * 
     * 对于任意分类筛选，返回的所有文章都应该属于该分类
     */
    @Property(tries = 100)
    void testCategoryFilteringCorrectness(
            @ForAll("categoryIds") Long categoryId,
            @ForAll("articleListsWithCategories") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 模拟分类存在
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        
        // 过滤出属于指定分类的文章
        List<Article> matchingArticles = allArticles.stream()
                .filter(article -> categoryId.equals(article.getCategoryId()))
                .collect(Collectors.toList());
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(matchingArticles, pageable, matchingArticles.size());
        
        // 模拟repository返回匹配的文章
        when(articleRepository.findApprovedArticlesByCategory(eq(categoryId), any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行分类筛选
        Page<Article> results = searchService.filterByCategory(categoryId, pageable);
        
        // 属性验证：所有返回的文章都应该属于指定分类
        assertNotNull(results, "Filter results should not be null");
        
        for (Article article : results.getContent()) {
            assertEquals(categoryId, article.getCategoryId(),
                    String.format("Article (id=%d, title='%s') should belong to category %d, but belongs to category %d",
                            article.getId(), article.getTitle(), categoryId, article.getCategoryId()));
        }
        
        // 验证返回的文章数量与预期匹配
        assertEquals(matchingArticles.size(), results.getContent().size(),
                String.format("Number of returned articles should match the number of articles in category %d", categoryId));
        
        // 验证所有返回的文章都是已审核状态
        for (Article article : results.getContent()) {
            assertEquals(Article.ReviewStatus.APPROVED, article.getReviewStatus(),
                    "All filtered articles should be approved");
        }
        
        // 验证调用了正确的repository方法
        verify(categoryRepository).existsById(categoryId);
        verify(articleRepository).findApprovedArticlesByCategory(eq(categoryId), any(Pageable.class));
    }

    /**
     * Feature: personal-blog-system, Property 19: 文章筛选正确性（空分类场景）
     * 验证需求: 29.2, 29.3
     * 
     * 当分类ID为null或选择"全部分类"时，应该返回所有已审核的文章
     */
    @Property(tries = 100)
    void testCategoryFilteringWithNullCategory(
            @ForAll("articleListsWithCategories") List<Article> allApprovedArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(allApprovedArticles, pageable, allApprovedArticles.size());
        
        // 模拟repository返回所有已审核文章
        when(articleRepository.findApprovedArticles(any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行分类筛选（null分类）
        Page<Article> results = searchService.filterByCategory(null, pageable);
        
        // 属性验证：应该返回所有已审核的文章
        assertNotNull(results, "Filter results should not be null");
        assertEquals(allApprovedArticles.size(), results.getContent().size(),
                "Null category filter should return all approved articles");
        
        // 验证所有返回的文章都是已审核状态
        for (Article article : results.getContent()) {
            assertEquals(Article.ReviewStatus.APPROVED, article.getReviewStatus(),
                    "All returned articles should be approved");
        }
        
        // 验证调用了正确的repository方法（应该调用findApprovedArticles而不是findApprovedArticlesByCategory）
        verify(articleRepository).findApprovedArticles(any(Pageable.class));
        verify(categoryRepository, never()).existsById(any());
        verify(articleRepository, never()).findApprovedArticlesByCategory(any(), any(Pageable.class));
    }

    /**
     * Feature: personal-blog-system, Property 19: 文章筛选正确性（不存在的分类）
     * 验证需求: 29.2
     * 
     * 当分类不存在时，应该抛出业务异常
     */
    @Property(tries = 100)
    void testCategoryFilteringWithNonExistentCategory(
            @ForAll("categoryIds") Long nonExistentCategoryId) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 模拟分类不存在
        when(categoryRepository.existsById(nonExistentCategoryId)).thenReturn(false);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        
        // 属性验证：应该抛出业务异常
        try {
            searchService.filterByCategory(nonExistentCategoryId, pageable);
            fail("Should throw BusinessException for non-existent category");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("分类不存在"),
                    "Exception message should indicate category not found");
        }
        
        // 验证调用了分类存在性检查
        verify(categoryRepository).existsById(nonExistentCategoryId);
        // 验证没有调用查询方法
        verify(articleRepository, never()).findApprovedArticlesByCategory(any(), any(Pageable.class));
    }

    /**
     * Feature: personal-blog-system, Property 19: 文章筛选正确性（分页正确性）
     * 验证需求: 29.2
     * 
     * 分类筛选应该正确支持分页功能
     */
    @Property(tries = 100)
    void testCategoryFilteringPaginationCorrectness(
            @ForAll("categoryIds") Long categoryId,
            @ForAll("largeArticleListsWithCategories") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 模拟分类存在
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        
        // 过滤出属于指定分类的文章
        List<Article> matchingArticles = allArticles.stream()
                .filter(article -> categoryId.equals(article.getCategoryId()))
                .collect(Collectors.toList());
        
        // 如果没有匹配的文章，跳过此测试
        Assume.that(matchingArticles.size() > 0);
        
        // 测试不同的分页参数
        int pageSize = 5;
        int totalPages = (int) Math.ceil((double) matchingArticles.size() / pageSize);
        
        for (int pageNum = 0; pageNum < Math.min(totalPages, 3); pageNum++) {
            Pageable pageable = PageRequest.of(pageNum, pageSize);
            
            // 计算当前页应该包含的文章
            int start = pageNum * pageSize;
            int end = Math.min(start + pageSize, matchingArticles.size());
            List<Article> pageArticles = matchingArticles.subList(start, end);
            
            Page<Article> expectedPage = new PageImpl<>(pageArticles, pageable, matchingArticles.size());
            
            // 模拟repository返回分页结果
            when(articleRepository.findApprovedArticlesByCategory(eq(categoryId), eq(pageable)))
                    .thenReturn(expectedPage);
            
            // 执行分类筛选
            Page<Article> results = searchService.filterByCategory(categoryId, pageable);
            
            // 属性验证：分页信息应该正确
            assertNotNull(results, "Filter results should not be null");
            assertEquals(pageNum, results.getNumber(), "Page number should match");
            assertEquals(pageSize, results.getSize(), "Page size should match");
            assertEquals(matchingArticles.size(), results.getTotalElements(), 
                    "Total elements should match total matching articles");
            assertEquals(totalPages, results.getTotalPages(), "Total pages should be correct");
            
            // 验证当前页的所有文章都属于指定分类
            for (Article article : results.getContent()) {
                assertEquals(categoryId, article.getCategoryId(),
                        String.format("Article in page %d should belong to category %d", pageNum, categoryId));
            }
        }
    }

    /**
     * Feature: personal-blog-system, Property 19: 文章筛选正确性（组合筛选）
     * 验证需求: 29.2, 29.8
     * 
     * 当同时应用分类筛选和关键词搜索时，返回的文章应该同时满足两个条件
     */
    @Property(tries = 100)
    void testCombinedCategoryAndKeywordFiltering(
            @ForAll("categoryIds") Long categoryId,
            @ForAll("searchKeywords") String keyword,
            @ForAll("articleListsWithCategories") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 模拟分类存在
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        
        // 过滤出同时满足分类和关键词的文章
        List<Article> matchingArticles = allArticles.stream()
                .filter(article -> categoryId.equals(article.getCategoryId()))
                .filter(article -> 
                    article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    article.getContent().toLowerCase().contains(keyword.toLowerCase())
                )
                .collect(Collectors.toList());
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(matchingArticles, pageable, matchingArticles.size());
        
        // 模拟repository返回匹配的文章
        when(articleRepository.searchByKeywordAndCategory(eq(keyword), eq(categoryId), any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行组合搜索
        Page<Article> results = searchService.search(keyword, categoryId, null, pageable);
        
        // 属性验证：所有返回的文章都应该同时满足分类和关键词条件
        assertNotNull(results, "Search results should not be null");
        
        for (Article article : results.getContent()) {
            // 验证分类匹配
            assertEquals(categoryId, article.getCategoryId(),
                    String.format("Article (id=%d) should belong to category %d", article.getId(), categoryId));
            
            // 验证关键词匹配
            boolean containsKeyword = 
                    article.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                    article.getContent().toLowerCase().contains(keyword.toLowerCase());
            
            assertTrue(containsKeyword,
                    String.format("Article (id=%d) should contain keyword '%s'", article.getId(), keyword));
        }
        
        // 验证返回的文章数量与预期匹配
        assertEquals(matchingArticles.size(), results.getContent().size(),
                "Number of returned articles should match the number of articles satisfying both conditions");
        
        // 验证调用了正确的repository方法
        verify(categoryRepository).existsById(categoryId);
        verify(articleRepository).searchByKeywordAndCategory(eq(keyword), eq(categoryId), any(Pageable.class));
    }

    /**
     * 生成分类ID
     */
    @Provide
    Arbitrary<Long> categoryIds() {
        return Arbitraries.longs().between(1L, 20L);
    }

    /**
     * 生成带分类的文章列表
     */
    @Provide
    Arbitrary<List<Article>> articleListsWithCategories() {
        return Arbitraries.integers().between(5, 15).flatMap(size -> {
            List<Arbitrary<Article>> articleArbitraries = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                articleArbitraries.add(articlesWithCategories());
            }
            return Combinators.combine(articleArbitraries).as(articles -> articles);
        });
    }

    /**
     * 生成大量带分类的文章列表（用于分页测试）
     */
    @Provide
    Arbitrary<List<Article>> largeArticleListsWithCategories() {
        return Arbitraries.integers().between(15, 40).flatMap(size -> {
            List<Arbitrary<Article>> articleArbitraries = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                articleArbitraries.add(articlesWithCategories());
            }
            return Combinators.combine(articleArbitraries).as(articles -> articles);
        });
    }

    /**
     * 生成带分类的单个文章
     */
    @Provide
    Arbitrary<Article> articlesWithCategories() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L);
        Arbitrary<String> titles = articleTitles();
        Arbitrary<String> contents = articleContents();
        Arbitrary<Long> userIds = Arbitraries.longs().between(1L, 10000L);
        Arbitrary<Long> categoryIds = categoryIds(); // 使用分类ID生成器
        
        return Combinators.combine(ids, titles, contents, userIds, categoryIds)
                .as((id, title, content, userId, categoryId) -> {
                    Article article = new Article();
                    article.setId(id);
                    article.setTitle(title);
                    article.setContent(content);
                    article.setUserId(userId);
                    article.setCategoryId(categoryId);
                    article.setReviewStatus(Article.ReviewStatus.APPROVED);
                    article.setIsPaid(false);
                    article.setPrice(BigDecimal.ZERO);
                    article.setIsPinned(false);
                    article.setViewCount(0);
                    article.setLikeCount(0);
                    article.setFavoriteCount(0);
                    article.setPurchaseCount(0);
                    article.setCreatedAt(LocalDateTime.now());
                    article.setUpdatedAt(LocalDateTime.now());
                    return article;
                });
    }

    /**
     * Feature: personal-blog-system, Property 20: 文章排序正确性
     * 验证需求: 29.4-29.7
     * 
     * 对于任意排序方式（最新发布、最多浏览、最多收藏、最多购买），返回的文章列表应该按照对应字段降序排列
     */
    @Property(tries = 100)
    void testArticleSortingCorrectness(
            @ForAll("sortTypes") String sortType,
            @ForAll("articleListsForSorting") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 根据排序类型对文章进行排序
        List<Article> sortedArticles = sortArticlesByType(allArticles, sortType);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(sortedArticles, pageable, sortedArticles.size());
        
        // 模拟repository返回排序后的文章（使用any(Pageable.class)因为service会创建新的带排序的Pageable）
        when(articleRepository.findApprovedArticles(any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行排序查询
        Page<Article> results = searchService.getArticlesSorted(sortType, pageable);
        
        // 属性验证：返回的文章应该按照指定字段降序排列
        assertNotNull(results, "Sorted results should not be null");
        
        List<Article> resultList = results.getContent();
        
        // 验证排序正确性
        for (int i = 0; i < resultList.size() - 1; i++) {
            Article current = resultList.get(i);
            Article next = resultList.get(i + 1);
            
            switch (sortType) {
                case "NEWEST":
                    // 验证按发布时间倒序排列
                    assertTrue(
                        current.getPublishedAt().isAfter(next.getPublishedAt()) ||
                        current.getPublishedAt().isEqual(next.getPublishedAt()),
                        String.format("Articles should be sorted by published_at DESC: article[%d].publishedAt=%s should >= article[%d].publishedAt=%s",
                            i, current.getPublishedAt(), i + 1, next.getPublishedAt())
                    );
                    break;
                    
                case "MOST_VIEWED":
                    // 验证按浏览量降序排列
                    assertTrue(
                        current.getViewCount() >= next.getViewCount(),
                        String.format("Articles should be sorted by view_count DESC: article[%d].viewCount=%d should >= article[%d].viewCount=%d",
                            i, current.getViewCount(), i + 1, next.getViewCount())
                    );
                    break;
                    
                case "MOST_FAVORITED":
                    // 验证按收藏数降序排列
                    assertTrue(
                        current.getFavoriteCount() >= next.getFavoriteCount(),
                        String.format("Articles should be sorted by favorite_count DESC: article[%d].favoriteCount=%d should >= article[%d].favoriteCount=%d",
                            i, current.getFavoriteCount(), i + 1, next.getFavoriteCount())
                    );
                    break;
                    
                case "MOST_PURCHASED":
                    // 验证按购买次数降序排列
                    assertTrue(
                        current.getPurchaseCount() >= next.getPurchaseCount(),
                        String.format("Articles should be sorted by purchase_count DESC: article[%d].purchaseCount=%d should >= article[%d].purchaseCount=%d",
                            i, current.getPurchaseCount(), i + 1, next.getPurchaseCount())
                    );
                    break;
                    
                default:
                    fail("Unknown sort type: " + sortType);
            }
        }
        
        // 验证所有返回的文章都是已审核状态
        for (Article article : resultList) {
            assertEquals(Article.ReviewStatus.APPROVED, article.getReviewStatus(),
                    "All sorted articles should be approved");
        }
        
        // 验证调用了正确的repository方法
        verify(articleRepository).findApprovedArticles(any(Pageable.class));
    }

    /**
     * Feature: personal-blog-system, Property 20: 文章排序正确性（组合筛选和排序）
     * 验证需求: 29.4-29.7, 29.8
     * 
     * 当同时应用分类筛选和排序时，应该先筛选后排序
     */
    @Property(tries = 100)
    void testCategoryFilteringWithSorting(
            @ForAll("categoryIds") Long categoryId,
            @ForAll("sortTypes") String sortType,
            @ForAll("articleListsForSorting") List<Article> allArticles) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 模拟分类存在
        when(categoryRepository.existsById(categoryId)).thenReturn(true);
        
        // 先筛选出属于指定分类的文章
        List<Article> filteredArticles = allArticles.stream()
                .filter(article -> categoryId.equals(article.getCategoryId()))
                .collect(Collectors.toList());
        
        // 如果没有匹配的文章，跳过此测试
        Assume.that(filteredArticles.size() > 1);
        
        // 然后对筛选后的文章进行排序
        List<Article> sortedArticles = sortArticlesByType(filteredArticles, sortType);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(sortedArticles, pageable, sortedArticles.size());
        
        // 模拟repository返回筛选并排序后的文章
        when(articleRepository.findApprovedArticlesByCategory(eq(categoryId), any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行组合查询
        Page<Article> results = searchService.getArticlesByCategorySorted(categoryId, sortType, pageable);
        
        // 属性验证：所有返回的文章都应该属于指定分类
        assertNotNull(results, "Results should not be null");
        
        List<Article> resultList = results.getContent();
        
        for (Article article : resultList) {
            assertEquals(categoryId, article.getCategoryId(),
                    String.format("Article (id=%d) should belong to category %d", article.getId(), categoryId));
        }
        
        // 属性验证：返回的文章应该按照指定字段降序排列
        for (int i = 0; i < resultList.size() - 1; i++) {
            Article current = resultList.get(i);
            Article next = resultList.get(i + 1);
            
            switch (sortType) {
                case "NEWEST":
                    assertTrue(
                        current.getPublishedAt().isAfter(next.getPublishedAt()) ||
                        current.getPublishedAt().isEqual(next.getPublishedAt()),
                        String.format("Filtered articles should be sorted by published_at DESC")
                    );
                    break;
                    
                case "MOST_VIEWED":
                    assertTrue(
                        current.getViewCount() >= next.getViewCount(),
                        String.format("Filtered articles should be sorted by view_count DESC")
                    );
                    break;
                    
                case "MOST_FAVORITED":
                    assertTrue(
                        current.getFavoriteCount() >= next.getFavoriteCount(),
                        String.format("Filtered articles should be sorted by favorite_count DESC")
                    );
                    break;
                    
                case "MOST_PURCHASED":
                    assertTrue(
                        current.getPurchaseCount() >= next.getPurchaseCount(),
                        String.format("Filtered articles should be sorted by purchase_count DESC")
                    );
                    break;
            }
        }
        
        // 验证调用了正确的repository方法
        verify(categoryRepository).existsById(categoryId);
        verify(articleRepository).findApprovedArticlesByCategory(eq(categoryId), any(Pageable.class));
    }

    /**
     * Feature: personal-blog-system, Property 20: 文章排序正确性（空列表场景）
     * 验证需求: 29.4-29.7
     * 
     * 当没有文章时，排序应该返回空列表而不是抛出异常
     */
    @Property(tries = 100)
    void testSortingWithEmptyList(@ForAll("sortTypes") String sortType) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 创建空列表
        List<Article> emptyList = new ArrayList<>();
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(emptyList, pageable, 0);
        
        // 模拟repository返回空列表
        when(articleRepository.findApprovedArticles(any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行排序查询
        Page<Article> results = searchService.getArticlesSorted(sortType, pageable);
        
        // 属性验证：应该返回空列表而不是null或抛出异常
        assertNotNull(results, "Results should not be null even when empty");
        assertEquals(0, results.getTotalElements(), "Empty list should have 0 total elements");
        assertEquals(0, results.getContent().size(), "Empty list should have 0 content size");
        assertTrue(results.getContent().isEmpty(), "Content should be empty");
        
        // 验证调用了正确的repository方法
        verify(articleRepository).findApprovedArticles(any(Pageable.class));
    }

    /**
     * Feature: personal-blog-system, Property 20: 文章排序正确性（单个文章场景）
     * 验证需求: 29.4-29.7
     * 
     * 当只有一个文章时，排序应该正常工作
     */
    @Property(tries = 100)
    void testSortingWithSingleArticle(
            @ForAll("sortTypes") String sortType,
            @ForAll("articlesWithCategories") Article singleArticle) {
        
        // 准备mock对象
        ArticleRepository articleRepository = mock(ArticleRepository.class);
        CategoryRepository categoryRepository = mock(CategoryRepository.class);
        TagRepository tagRepository = mock(TagRepository.class);
        
        SearchServiceImpl searchService = new SearchServiceImpl(articleRepository, categoryRepository, tagRepository);
        
        // 创建只包含一个文章的列表
        List<Article> singleList = java.util.Collections.singletonList(singleArticle);
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(0, 10);
        Page<Article> expectedPage = new PageImpl<>(singleList, pageable, 1);
        
        // 模拟repository返回单个文章
        when(articleRepository.findApprovedArticles(any(Pageable.class)))
                .thenReturn(expectedPage);
        
        // 执行排序查询
        Page<Article> results = searchService.getArticlesSorted(sortType, pageable);
        
        // 属性验证：应该返回单个文章
        assertNotNull(results, "Results should not be null");
        assertEquals(1, results.getTotalElements(), "Should have 1 total element");
        assertEquals(1, results.getContent().size(), "Should have 1 content size");
        assertEquals(singleArticle.getId(), results.getContent().get(0).getId(),
                "Returned article should match the input article");
        
        // 验证调用了正确的repository方法
        verify(articleRepository).findApprovedArticles(any(Pageable.class));
    }

    /**
     * 生成排序类型
     */
    @Provide
    Arbitrary<String> sortTypes() {
        return Arbitraries.of("NEWEST", "MOST_VIEWED", "MOST_FAVORITED", "MOST_PURCHASED");
    }

    /**
     * 生成用于排序测试的文章列表
     */
    @Provide
    Arbitrary<List<Article>> articleListsForSorting() {
        return Arbitraries.integers().between(5, 15).flatMap(size -> {
            List<Arbitrary<Article>> articleArbitraries = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                articleArbitraries.add(articlesWithSortingFields());
            }
            return Combinators.combine(articleArbitraries).as(articles -> articles);
        });
    }

    /**
     * 生成带排序字段的单个文章
     */
    @Provide
    Arbitrary<Article> articlesWithSortingFields() {
        Arbitrary<Long> ids = Arbitraries.longs().between(1L, 1000000L);
        Arbitrary<String> titles = articleTitles();
        Arbitrary<String> contents = articleContents();
        Arbitrary<Long> userIds = Arbitraries.longs().between(1L, 10000L);
        Arbitrary<Long> categoryIds = categoryIds();
        Arbitrary<Integer> viewCounts = Arbitraries.integers().between(0, 10000);
        Arbitrary<Integer> favoriteCounts = Arbitraries.integers().between(0, 1000);
        Arbitrary<Integer> purchaseCounts = Arbitraries.integers().between(0, 500);
        Arbitrary<LocalDateTime> publishedAts = Arbitraries.integers().between(1, 365)
                .map(daysAgo -> LocalDateTime.now().minusDays(daysAgo));
        
        // Split into two combines since jqwik only supports up to 8 parameters
        return Combinators.combine(ids, titles, contents, userIds)
                .flatAs((id, title, content, userId) -> 
                    Combinators.combine(categoryIds, viewCounts, favoriteCounts, purchaseCounts, publishedAts)
                        .as((categoryId, viewCount, favoriteCount, purchaseCount, publishedAt) -> {
                            Article article = new Article();
                            article.setId(id);
                            article.setTitle(title);
                            article.setContent(content);
                            article.setUserId(userId);
                            article.setCategoryId(categoryId);
                            article.setReviewStatus(Article.ReviewStatus.APPROVED);
                            article.setIsPaid(false);
                            article.setPrice(BigDecimal.ZERO);
                            article.setIsPinned(false);
                            article.setViewCount(viewCount);
                            article.setLikeCount(0);
                            article.setFavoriteCount(favoriteCount);
                            article.setPurchaseCount(purchaseCount);
                            article.setCreatedAt(LocalDateTime.now());
                            article.setUpdatedAt(LocalDateTime.now());
                            article.setPublishedAt(publishedAt);
                            return article;
                        })
                );
    }

    /**
     * 根据排序类型对文章列表进行排序
     */
    private List<Article> sortArticlesByType(List<Article> articles, String sortType) {
        List<Article> sortedList = new ArrayList<>(articles);
        
        switch (sortType) {
            case "NEWEST":
                sortedList.sort((a1, a2) -> a2.getPublishedAt().compareTo(a1.getPublishedAt()));
                break;
                
            case "MOST_VIEWED":
                sortedList.sort((a1, a2) -> Integer.compare(a2.getViewCount(), a1.getViewCount()));
                break;
                
            case "MOST_FAVORITED":
                sortedList.sort((a1, a2) -> Integer.compare(a2.getFavoriteCount(), a1.getFavoriteCount()));
                break;
                
            case "MOST_PURCHASED":
                sortedList.sort((a1, a2) -> Integer.compare(a2.getPurchaseCount(), a1.getPurchaseCount()));
                break;
                
            default:
                throw new IllegalArgumentException("Unknown sort type: " + sortType);
        }
        
        return sortedList;
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
            // 尝试从父类获取字段
            try {
                java.lang.reflect.Field field = target.getClass().getSuperclass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to set field: " + fieldName, ex);
            }
        }
    }
}
