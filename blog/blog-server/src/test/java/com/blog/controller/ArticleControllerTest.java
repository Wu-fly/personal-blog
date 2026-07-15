package com.blog.controller;

import com.blog.dto.ArticleRequest;
import com.blog.dto.PinArticleRequest;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.entity.User;
import com.blog.security.CustomUserDetails;
import com.blog.service.ArticleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ArticleController单元测试
 * 需求: 2.1-2.8, 3.1-3.4, 4.1-4.6, 27.1-27.6, 29.1-29.9
 */
@WebMvcTest(ArticleController.class)
@AutoConfigureMockMvc(addFilters = false)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleService articleService;

    private CustomUserDetails bloggerUser;
    private Article testArticle;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setPhone("13800138000");
        testUser.setEmail("test@example.com");
        testUser.setNickname("测试博主");
        testUser.setAvatar("avatar.jpg");
        testUser.setRole(User.UserRole.BLOGGER);

        bloggerUser = new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE");

        // 创建测试分类
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("科技");

        // 创建测试文章
        testArticle = new Article();
        testArticle.setId(1L);
        testArticle.setUserId(1L);
        testArticle.setTitle("测试文章");
        testArticle.setContent("这是测试文章内容");
        testArticle.setSummary("测试摘要");
        testArticle.setCoverImage("cover.jpg");
        testArticle.setCategoryId(1L);
        testArticle.setIsPaid(false);
        testArticle.setPrice(BigDecimal.ZERO);
        testArticle.setIsPinned(false);
        testArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        testArticle.setViewCount(0);
        testArticle.setLikeCount(0);
        testArticle.setFavoriteCount(0);
        testArticle.setPurchaseCount(0);
        testArticle.setCreatedAt(LocalDateTime.now());
        testArticle.setUpdatedAt(LocalDateTime.now());
        testArticle.setUser(testUser);
        testArticle.setCategory(testCategory);
    }

    /**
     * 测试创建文章 - 成功
     * 需求: 2.1
     */
    @Test
    @WithMockUser(roles = "BLOGGER")
    void testCreateArticle_Success() throws Exception {
        // 准备请求数据
        ArticleRequest request = new ArticleRequest();
        request.setTitle("新文章");
        request.setContent("新文章内容");
        request.setSummary("新文章摘要");
        request.setCoverImage("new-cover.jpg");
        request.setCategoryId(1L);
        request.setIsPaid(false);

        // Mock服务层
        when(articleService.createArticle(any(Article.class))).thenReturn(testArticle);

        // 执行请求
        mockMvc.perform(post("/api/articles")
                .with(user(bloggerUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文章创建成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试文章"));

        // 验证服务层调用
        verify(articleService, times(1)).createArticle(any(Article.class));
    }

    /**
     * 测试创建文章 - 标题为空
     * 需求: 2.5
     */
    @Test
    @WithMockUser(roles = "BLOGGER")
    void testCreateArticle_EmptyTitle() throws Exception {
        // 准备请求数据（标题为空）
        ArticleRequest request = new ArticleRequest();
        request.setTitle("");
        request.setContent("新文章内容");

        // 执行请求
        mockMvc.perform(post("/api/articles")
                .with(user(bloggerUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 验证服务层未被调用
        verify(articleService, never()).createArticle(any(Article.class));
    }

    /**
     * 测试创建文章 - 内容为空
     * 需求: 2.5
     */
    @Test
    @WithMockUser(roles = "BLOGGER")
    void testCreateArticle_EmptyContent() throws Exception {
        // 准备请求数据（内容为空）
        ArticleRequest request = new ArticleRequest();
        request.setTitle("新文章");
        request.setContent("");

        // 执行请求
        mockMvc.perform(post("/api/articles")
                .with(user(bloggerUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 验证服务层未被调用
        verify(articleService, never()).createArticle(any(Article.class));
    }

    /**
     * 测试更新文章 - 成功
     * 需求: 2.2
     */
    @Test
    @WithMockUser(roles = "BLOGGER")
    void testUpdateArticle_Success() throws Exception {
        // 准备请求数据
        ArticleRequest request = new ArticleRequest();
        request.setTitle("更新后的文章");
        request.setContent("更新后的内容");
        request.setSummary("更新后的摘要");

        // Mock服务层
        Article updatedArticle = new Article();
        updatedArticle.setId(1L);
        updatedArticle.setTitle("更新后的文章");
        updatedArticle.setContent("更新后的内容");
        updatedArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        updatedArticle.setUser(testUser);
        updatedArticle.setCategory(testCategory);

        when(articleService.updateArticle(eq(1L), any(Article.class))).thenReturn(updatedArticle);

        // 执行请求
        mockMvc.perform(put("/api/articles/1")
                .with(user(bloggerUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文章更新成功"))
                .andExpect(jsonPath("$.data.title").value("更新后的文章"));

        // 验证服务层调用
        verify(articleService, times(1)).updateArticle(eq(1L), any(Article.class));
    }

    /**
     * 测试删除文章 - 成功
     * 需求: 2.3
     */
    @Test
    @WithMockUser(roles = "BLOGGER")
    void testDeleteArticle_Success() throws Exception {
        // Mock服务层
        doNothing().when(articleService).deleteArticle(1L, 1L);

        // 执行请求
        mockMvc.perform(delete("/api/articles/1")
                .with(user(bloggerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文章删除成功"));

        // 验证服务层调用
        verify(articleService, times(1)).deleteArticle(1L, 1L);
    }

    /**
     * 测试获取文章列表 - 成功
     * 需求: 3.1, 29.1-29.9
     */
    @Test
    void testGetArticles_Success() throws Exception {
        // 准备测试数据
        List<Article> articles = Arrays.asList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles);

        // Mock服务层
        when(articleService.getArticles(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(articlePage);

        // 执行请求
        mockMvc.perform(get("/api/articles")
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "createdAt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0));

        // 验证服务层调用
        verify(articleService, times(1)).getArticles(isNull(), isNull(), isNull(), any(Pageable.class));
    }

    /**
     * 测试获取文章列表 - 按分类筛选
     * 需求: 29.2
     */
    @Test
    void testGetArticles_FilterByCategory() throws Exception {
        // 准备测试数据
        List<Article> articles = Arrays.asList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles);

        // Mock服务层
        when(articleService.getArticles(eq(1L), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(articlePage);

        // 执行请求
        mockMvc.perform(get("/api/articles")
                .param("categoryId", "1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        // 验证服务层调用
        verify(articleService, times(1)).getArticles(eq(1L), isNull(), isNull(), any(Pageable.class));
    }

    /**
     * 测试获取文章列表 - 按浏览量排序
     * 需求: 29.5
     */
    @Test
    void testGetArticles_SortByViewCount() throws Exception {
        // 准备测试数据
        List<Article> articles = Arrays.asList(testArticle);
        Page<Article> articlePage = new PageImpl<>(articles);

        // Mock服务层
        when(articleService.getArticles(isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(articlePage);

        // 执行请求
        mockMvc.perform(get("/api/articles")
                .param("sortBy", "viewCount")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证服务层调用
        verify(articleService, times(1)).getArticles(isNull(), isNull(), isNull(), any(Pageable.class));
    }

    /**
     * 测试获取文章详情 - 成功
     * 需求: 3.2, 3.3
     */
    @Test
    void testGetArticleDetail_Success() throws Exception {
        // Mock服务层
        when(articleService.getArticleDetail(1L, null)).thenReturn(testArticle);

        // 执行请求
        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("测试文章"))
                .andExpect(jsonPath("$.data.author.nickname").value("测试博主"));

        // 验证服务层调用
        verify(articleService, times(1)).getArticleDetail(1L, null);
    }

    /**
     * 测试置顶文章 - 成功
     * 需求: 27.1
     */
    @Test
    @WithMockUser(roles = "BLOGGER")
    void testPinArticle_Success() throws Exception {
        // 准备请求数据
        PinArticleRequest request = new PinArticleRequest(true);

        // Mock服务层
        Article pinnedArticle = new Article();
        pinnedArticle.setId(1L);
        pinnedArticle.setTitle("测试文章");
        pinnedArticle.setIsPinned(true);
        pinnedArticle.setPinnedAt(LocalDateTime.now());
        pinnedArticle.setUser(testUser);
        pinnedArticle.setCategory(testCategory);

        when(articleService.pinArticle(1L, 1L, true)).thenReturn(pinnedArticle);

        // 执行请求
        mockMvc.perform(post("/api/articles/1/pin")
                .with(user(bloggerUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("文章置顶成功"))
                .andExpect(jsonPath("$.data.isPinned").value(true));

        // 验证服务层调用
        verify(articleService, times(1)).pinArticle(1L, 1L, true);
    }

    /**
     * 测试取消置顶文章 - 成功
     * 需求: 27.2
     */
    @Test
    @WithMockUser(roles = "BLOGGER")
    void testUnpinArticle_Success() throws Exception {
        // 准备请求数据
        PinArticleRequest request = new PinArticleRequest(false);

        // Mock服务层
        Article unpinnedArticle = new Article();
        unpinnedArticle.setId(1L);
        unpinnedArticle.setTitle("测试文章");
        unpinnedArticle.setIsPinned(false);
        unpinnedArticle.setUser(testUser);
        unpinnedArticle.setCategory(testCategory);

        when(articleService.pinArticle(1L, 1L, false)).thenReturn(unpinnedArticle);

        // 执行请求
        mockMvc.perform(post("/api/articles/1/pin")
                .with(user(bloggerUser))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("取消置顶成功"))
                .andExpect(jsonPath("$.data.isPinned").value(false));

        // 验证服务层调用
        verify(articleService, times(1)).pinArticle(1L, 1L, false);
    }
}
