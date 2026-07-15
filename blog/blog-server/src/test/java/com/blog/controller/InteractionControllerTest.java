package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.*;
import com.blog.security.CustomUserDetails;
import com.blog.service.InteractionService;
import com.blog.service.WalletService;
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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * InteractionController单元测试
 * 测试互动控制器的所有端点
 */
@WebMvcTest(InteractionController.class)
@AutoConfigureMockMvc(addFilters = false)
class InteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InteractionService interactionService;

    @MockBean
    private WalletService walletService;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new CustomUserDetails(
            1L,
            "13800138000",
            "password",
            "USER",
            "ACTIVE"
        );
    }

    @Test
    @WithMockUser(roles = "USER")
    void testToggleLike_Success() throws Exception {
        // Arrange
        LikeRequest request = new LikeRequest(100L);
        when(interactionService.toggleLike(1L, 100L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/like")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.liked").value(true))
                .andExpect(jsonPath("$.data.message").value("点赞成功"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testToggleLike_Unlike() throws Exception {
        // Arrange
        LikeRequest request = new LikeRequest(100L);
        when(interactionService.toggleLike(1L, 100L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/like")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.liked").value(false))
                .andExpect(jsonPath("$.data.message").value("取消点赞成功"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testToggleLike_InvalidRequest() throws Exception {
        // Arrange - articleId is null
        LikeRequest request = new LikeRequest(null);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/like")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testToggleFavorite_Success() throws Exception {
        // Arrange
        FavoriteRequest request = new FavoriteRequest(100L);
        when(interactionService.toggleFavorite(1L, 100L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/favorite")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.favorited").value(true))
                .andExpect(jsonPath("$.data.message").value("收藏成功"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testToggleFavorite_Unfavorite() throws Exception {
        // Arrange
        FavoriteRequest request = new FavoriteRequest(100L);
        when(interactionService.toggleFavorite(1L, 100L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/favorite")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.favorited").value(false))
                .andExpect(jsonPath("$.data.message").value("取消收藏成功"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testToggleFollow_Success() throws Exception {
        // Arrange
        FollowRequest request = new FollowRequest(2L);
        when(interactionService.toggleFollow(1L, 2L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/follow")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.followed").value(true))
                .andExpect(jsonPath("$.data.message").value("关注成功"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testToggleFollow_CannotFollowSelf() throws Exception {
        // Arrange - trying to follow self
        FollowRequest request = new FollowRequest(1L);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/follow")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CANNOT_FOLLOW_SELF"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testReward_Success() throws Exception {
        // Arrange
        RewardRequest request = new RewardRequest(2L, new BigDecimal("10.00"), 100L);
        
        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setType(Transaction.TransactionType.REWARD);
        transaction.setAmount(new BigDecimal("10.00"));
        transaction.setCreatedAt(LocalDateTime.now());
        
        when(walletService.reward(1L, 2L, new BigDecimal("10.00"), 100L))
                .thenReturn(transaction);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/reward")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("打赏成功"))
                .andExpect(jsonPath("$.data.type").value("REWARD"))
                .andExpect(jsonPath("$.data.amount").value(10.00));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testReward_CannotRewardSelf() throws Exception {
        // Arrange - trying to reward self
        RewardRequest request = new RewardRequest(1L, new BigDecimal("10.00"), null);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/reward")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CANNOT_REWARD_SELF"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testReward_InvalidAmount() throws Exception {
        // Arrange - amount is 0
        RewardRequest request = new RewardRequest(2L, BigDecimal.ZERO, null);

        // Act & Assert
        mockMvc.perform(post("/api/interactions/reward")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetFavorites_Success() throws Exception {
        // Arrange
        Article article = new Article();
        article.setId(100L);
        article.setTitle("Test Article");
        
        Favorite favorite1 = new Favorite();
        favorite1.setId(1L);
        favorite1.setArticle(article);
        favorite1.setCreatedAt(LocalDateTime.now());
        
        Favorite favorite2 = new Favorite();
        favorite2.setId(2L);
        favorite2.setArticle(article);
        favorite2.setCreatedAt(LocalDateTime.now());
        
        List<Favorite> favorites = Arrays.asList(favorite1, favorite2);
        Page<Favorite> page = new PageImpl<>(favorites);
        
        when(interactionService.getFavoriteList(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/interactions/favorites")
                .with(user(userDetails))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetFollowing_Success() throws Exception {
        // Arrange
        User followingUser = new User();
        followingUser.setId(2L);
        followingUser.setNickname("Test User");
        
        Follow follow1 = new Follow();
        follow1.setId(1L);
        follow1.setFollowing(followingUser);
        follow1.setCreatedAt(LocalDateTime.now());
        
        Follow follow2 = new Follow();
        follow2.setId(2L);
        follow2.setFollowing(followingUser);
        follow2.setCreatedAt(LocalDateTime.now());
        
        List<Follow> follows = Arrays.asList(follow1, follow2);
        Page<Follow> page = new PageImpl<>(follows);
        
        when(interactionService.getFollowingList(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/interactions/following")
                .with(user(userDetails))
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetLikeStatus_Liked() throws Exception {
        // Arrange
        when(interactionService.isLiked(1L, 100L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/interactions/like/status")
                .with(user(userDetails))
                .param("articleId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.liked").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetLikeStatus_NotLiked() throws Exception {
        // Arrange
        when(interactionService.isLiked(1L, 100L)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/interactions/like/status")
                .with(user(userDetails))
                .param("articleId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.liked").value(false));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetFavoriteStatus_Favorited() throws Exception {
        // Arrange
        when(interactionService.isFavorited(1L, 100L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/interactions/favorite/status")
                .with(user(userDetails))
                .param("articleId", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.favorited").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetFollowStatus_Following() throws Exception {
        // Arrange
        when(interactionService.isFollowing(1L, 2L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/interactions/follow/status")
                .with(user(userDetails))
                .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.following").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetFavorites_WithPagination() throws Exception {
        // Arrange
        List<Favorite> favorites = Arrays.asList(new Favorite(), new Favorite());
        Page<Favorite> page = new PageImpl<>(favorites);
        
        when(interactionService.getFavoriteList(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/interactions/favorites")
                .with(user(userDetails))
                .param("page", "1")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetFollowing_WithPagination() throws Exception {
        // Arrange
        List<Follow> follows = Arrays.asList(new Follow(), new Follow());
        Page<Follow> page = new PageImpl<>(follows);
        
        when(interactionService.getFollowingList(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/interactions/following")
                .with(user(userDetails))
                .param("page", "2")
                .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
