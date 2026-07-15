package com.blog.controller;

import com.blog.dto.ApproveCommentRequest;
import com.blog.dto.CommentRequest;
import com.blog.entity.Comment;
import com.blog.entity.User;
import com.blog.security.CustomUserDetails;
import com.blog.security.JwtUtil;
import com.blog.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CommentController单元测试
 */
@WebMvcTest(controllers = CommentController.class)
@ContextConfiguration(classes = {CommentController.class, CommentControllerTest.TestSecurityConfig.class})
class CommentControllerTest {

    @Configuration
    @EnableWebSecurity
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/api/comments/article/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/comments/*/replies").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/comments/*/approve").hasAnyRole("BLOGGER", "ADMIN")
                .anyRequest().authenticated();
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtUtil jwtUtil;

    private Comment testComment;
    private User testUser;
    private CustomUserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setNickname("测试用户");
        testUser.setAvatar("avatar.jpg");

        // 创建测试评论
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setArticleId(1L);
        testComment.setUserId(1L);
        testComment.setContent("这是一条测试评论");
        testComment.setStatus(Comment.CommentStatus.APPROVED);
        testComment.setCreatedAt(LocalDateTime.now());
        testComment.setUser(testUser);

        // 创建 CustomUserDetails
        mockUserDetails = new CustomUserDetails(1L, "13800138000", "password", "BLOGGER", "ACTIVE");

        // Mock JWT工具
        when(jwtUtil.getUserIdFromToken(anyString())).thenReturn(1L);
    }

    @Test
    @WithMockUser
    void testCreateComment_Success() throws Exception {
        // 准备请求数据
        CommentRequest request = new CommentRequest();
        request.setArticleId(1L);
        request.setContent("这是一条新评论");

        // Mock服务层
        when(commentService.createComment(any(Comment.class))).thenReturn(testComment);

        // 执行请求
        mockMvc.perform(post("/api/comments")
                .with(csrf())
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("评论发表成功"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("这是一条测试评论"));

        // 验证服务层调用
        verify(commentService, times(1)).createComment(any(Comment.class));
    }

    @Test
    @WithMockUser
    void testCreateComment_EmptyContent() throws Exception {
        // 准备请求数据（空内容）
        CommentRequest request = new CommentRequest();
        request.setArticleId(1L);
        request.setContent("");

        // 执行请求
        mockMvc.perform(post("/api/comments")
                .with(csrf())
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // 验证服务层未被调用
        verify(commentService, never()).createComment(any(Comment.class));
    }

    @Test
    void testGetArticleComments_Success() throws Exception {
        // 准备测试数据
        List<Comment> comments = Arrays.asList(testComment);
        Page<Comment> commentPage = new PageImpl<>(comments);

        // Mock服务层
        when(commentService.getCommentsByArticle(eq(1L), any(Pageable.class)))
                .thenReturn(commentPage);

        // 执行请求（公开接口，无需认证）
        mockMvc.perform(get("/api/comments/article/1")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.comments").isArray())
                .andExpect(jsonPath("$.data.comments[0].id").value(1))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(0));

        // 验证服务层调用
        verify(commentService, times(1)).getCommentsByArticle(eq(1L), any(Pageable.class));
    }

    @Test
    @WithMockUser
    void testReplyComment_Success() throws Exception {
        // 准备请求数据
        CommentRequest request = new CommentRequest();
        request.setArticleId(1L);
        request.setContent("这是一条回复");

        // 创建回复评论
        Comment reply = new Comment();
        reply.setId(2L);
        reply.setArticleId(1L);
        reply.setUserId(1L);
        reply.setParentId(1L);
        reply.setContent("这是一条回复");
        reply.setStatus(Comment.CommentStatus.APPROVED);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUser(testUser);

        // Mock服务层
        when(commentService.replyComment(any(Comment.class))).thenReturn(reply);

        // 执行请求
        mockMvc.perform(post("/api/comments/1/reply")
                .with(csrf())
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("回复成功"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.parentId").value(1));

        // 验证服务层调用
        verify(commentService, times(1)).replyComment(any(Comment.class));
    }

    @Test
    @WithMockUser
    void testDeleteComment_Success() throws Exception {
        // Mock服务层（void方法）
        doNothing().when(commentService).deleteComment(eq(1L), eq(1L));

        // 执行请求
        mockMvc.perform(delete("/api/comments/1")
                .with(csrf())
                .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("评论删除成功"));

        // 验证服务层调用
        verify(commentService, times(1)).deleteComment(eq(1L), eq(1L));
    }

    @Test
    @WithMockUser(roles = "BLOGGER")
    void testApproveComment_Success() throws Exception {
        // 准备请求数据
        ApproveCommentRequest request = new ApproveCommentRequest();
        request.setStatus("APPROVED");

        // Mock服务层
        when(commentService.approveComment(eq(1L), eq(1L), eq(Comment.CommentStatus.APPROVED)))
                .thenReturn(testComment);

        // 执行请求
        mockMvc.perform(put("/api/comments/1/approve")
                .with(csrf())
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("评论审核成功"))
                .andExpect(jsonPath("$.data.id").value(1));

        // 验证服务层调用
        verify(commentService, times(1))
                .approveComment(eq(1L), eq(1L), eq(Comment.CommentStatus.APPROVED));
    }

    @Test
    @WithMockUser(roles = "BLOGGER")
    void testApproveComment_InvalidStatus() throws Exception {
        // 准备请求数据（无效状态）
        ApproveCommentRequest request = new ApproveCommentRequest();
        request.setStatus("INVALID_STATUS");

        // 执行请求
        mockMvc.perform(put("/api/comments/1/approve")
                .with(csrf())
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_STATUS"));

        // 验证服务层未被调用
        verify(commentService, never()).approveComment(anyLong(), anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "BLOGGER")
    void testGetPendingComments_Success() throws Exception {
        // 准备测试数据
        Comment pendingComment = new Comment();
        pendingComment.setId(2L);
        pendingComment.setArticleId(1L);
        pendingComment.setUserId(2L);
        pendingComment.setContent("待审核评论");
        pendingComment.setStatus(Comment.CommentStatus.PENDING);
        pendingComment.setCreatedAt(LocalDateTime.now());
        pendingComment.setUser(testUser);

        List<Comment> comments = Arrays.asList(pendingComment);
        Page<Comment> commentPage = new PageImpl<>(comments);

        // Mock服务层
        when(commentService.getPendingComments(any(Pageable.class)))
                .thenReturn(commentPage);

        // 执行请求
        mockMvc.perform(get("/api/comments/pending")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.comments").isArray())
                .andExpect(jsonPath("$.data.comments[0].id").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // 验证服务层调用
        verify(commentService, times(1)).getPendingComments(any(Pageable.class));
    }

    @Test
    void testGetCommentReplies_Success() throws Exception {
        // 准备测试数据
        Comment reply1 = new Comment();
        reply1.setId(2L);
        reply1.setArticleId(1L);
        reply1.setUserId(2L);
        reply1.setParentId(1L);
        reply1.setContent("回复1");
        reply1.setStatus(Comment.CommentStatus.APPROVED);
        reply1.setCreatedAt(LocalDateTime.now());
        reply1.setUser(testUser);

        Comment reply2 = new Comment();
        reply2.setId(3L);
        reply2.setArticleId(1L);
        reply2.setUserId(3L);
        reply2.setParentId(1L);
        reply2.setContent("回复2");
        reply2.setStatus(Comment.CommentStatus.APPROVED);
        reply2.setCreatedAt(LocalDateTime.now());
        reply2.setUser(testUser);

        List<Comment> replies = Arrays.asList(reply1, reply2);

        // Mock服务层
        when(commentService.getRepliesByComment(eq(1L))).thenReturn(replies);

        // 执行请求（公开接口，无需认证）
        mockMvc.perform(get("/api/comments/1/replies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].parentId").value(1))
                .andExpect(jsonPath("$.data[1].parentId").value(1));

        // 验证服务层调用
        verify(commentService, times(1)).getRepliesByComment(eq(1L));
    }

    @Test
    void testCreateComment_Unauthorized() throws Exception {
        // 准备请求数据
        CommentRequest request = new CommentRequest();
        request.setArticleId(1L);
        request.setContent("未授权的评论");

        // 执行请求（未登录，应该返回403 Forbidden）
        mockMvc.perform(post("/api/comments")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 验证服务层未被调用
        verify(commentService, never()).createComment(any(Comment.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testApproveComment_Forbidden() throws Exception {
        // 准备请求数据
        ApproveCommentRequest request = new ApproveCommentRequest();
        request.setStatus("APPROVED");

        // 执行请求（普通用户无权限，应该在权限检查时就被拒绝，不会调用服务层）
        mockMvc.perform(put("/api/comments/1/approve")
                .with(csrf())
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        // 验证服务层未被调用（因为权限检查失败）
        verify(commentService, never()).approveComment(anyLong(), anyLong(), any());
    }
}
