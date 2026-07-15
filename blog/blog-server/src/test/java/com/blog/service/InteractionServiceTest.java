package com.blog.service;

import com.blog.entity.Article;
import com.blog.entity.Favorite;
import com.blog.entity.Follow;
import com.blog.entity.Like;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.FavoriteRepository;
import com.blog.repository.FollowRepository;
import com.blog.repository.LikeRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.InteractionServiceImpl;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InteractionService
 */
@ExtendWith(MockitoExtension.class)
class InteractionServiceTest {
    
    @Mock
    private LikeRepository likeRepository;
    
    @Mock
    private FavoriteRepository favoriteRepository;
    
    @Mock
    private FollowRepository followRepository;
    
    @Mock
    private ArticleRepository articleRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private InteractionServiceImpl interactionService;
    
    private Long userId;
    private Long articleId;
    private Long followingId;
    
    @BeforeEach
    void setUp() {
        userId = 1L;
        articleId = 100L;
        followingId = 2L;
    }
    
    @Test
    void testToggleLike_AddLike_Success() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(likeRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(false);
        when(likeRepository.save(any(Like.class))).thenReturn(new Like());
        
        // When
        boolean result = interactionService.toggleLike(userId, articleId);
        
        // Then
        assertTrue(result);
        verify(likeRepository).save(any(Like.class));
        verify(articleRepository).incrementLikeCount(articleId);
        verify(likeRepository, never()).deleteByUserIdAndArticleId(anyLong(), anyLong());
    }
    
    @Test
    void testToggleLike_RemoveLike_Success() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(likeRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(true);
        
        // When
        boolean result = interactionService.toggleLike(userId, articleId);
        
        // Then
        assertFalse(result);
        verify(likeRepository).deleteByUserIdAndArticleId(userId, articleId);
        verify(articleRepository).decrementLikeCount(articleId);
        verify(likeRepository, never()).save(any(Like.class));
    }
    
    @Test
    void testToggleLike_UserNotExists_ThrowsException() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.toggleLike(userId, articleId));
        assertEquals("用户不存在", exception.getMessage());
    }
    
    @Test
    void testToggleLike_ArticleNotExists_ThrowsException() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.toggleLike(userId, articleId));
        assertEquals("文章不存在", exception.getMessage());
    }
    
    @Test
    void testToggleFavorite_AddFavorite_Success() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(new Favorite());
        
        // When
        boolean result = interactionService.toggleFavorite(userId, articleId);
        
        // Then
        assertTrue(result);
        verify(favoriteRepository).save(any(Favorite.class));
        verify(articleRepository).incrementFavoriteCount(articleId);
        verify(favoriteRepository, never()).deleteByUserIdAndArticleId(anyLong(), anyLong());
    }
    
    @Test
    void testToggleFavorite_RemoveFavorite_Success() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(true);
        
        // When
        boolean result = interactionService.toggleFavorite(userId, articleId);
        
        // Then
        assertFalse(result);
        verify(favoriteRepository).deleteByUserIdAndArticleId(userId, articleId);
        verify(articleRepository).decrementFavoriteCount(articleId);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }
    
    @Test
    void testToggleFavorite_UserNotExists_ThrowsException() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.toggleFavorite(userId, articleId));
        assertEquals("用户不存在", exception.getMessage());
    }
    
    @Test
    void testToggleFavorite_ArticleNotExists_ThrowsException() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(articleRepository.existsById(articleId)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.toggleFavorite(userId, articleId));
        assertEquals("文章不存在", exception.getMessage());
    }
    
    @Test
    void testToggleFollow_AddFollow_Success() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(followingId)).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId(userId, followingId)).thenReturn(false);
        when(followRepository.save(any(Follow.class))).thenReturn(new Follow());
        
        // When
        boolean result = interactionService.toggleFollow(userId, followingId);
        
        // Then
        assertTrue(result);
        verify(followRepository).save(any(Follow.class));
        verify(followRepository, never()).deleteByFollowerIdAndFollowingId(anyLong(), anyLong());
    }
    
    @Test
    void testToggleFollow_RemoveFollow_Success() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(followingId)).thenReturn(true);
        when(followRepository.existsByFollowerIdAndFollowingId(userId, followingId)).thenReturn(true);
        
        // When
        boolean result = interactionService.toggleFollow(userId, followingId);
        
        // Then
        assertFalse(result);
        verify(followRepository).deleteByFollowerIdAndFollowingId(userId, followingId);
        verify(followRepository, never()).save(any(Follow.class));
    }
    
    @Test
    void testToggleFollow_FollowerNotExists_ThrowsException() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.toggleFollow(userId, followingId));
        assertEquals("关注者不存在", exception.getMessage());
    }
    
    @Test
    void testToggleFollow_FollowingNotExists_ThrowsException() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.existsById(followingId)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.toggleFollow(userId, followingId));
        assertEquals("被关注用户不存在", exception.getMessage());
    }
    
    @Test
    void testToggleFollow_FollowSelf_ThrowsException() {
        // Given
        when(userRepository.existsById(userId)).thenReturn(true);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.toggleFollow(userId, userId));
        assertEquals("不能关注自己", exception.getMessage());
    }
    
    @Test
    void testGetFavoriteList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Favorite favorite1 = new Favorite();
        favorite1.setUserId(userId);
        favorite1.setArticleId(100L);
        
        Favorite favorite2 = new Favorite();
        favorite2.setUserId(userId);
        favorite2.setArticleId(101L);
        
        List<Favorite> favorites = Arrays.asList(favorite1, favorite2);
        Page<Favorite> favoritePage = new PageImpl<>(favorites, pageable, favorites.size());
        
        when(userRepository.existsById(userId)).thenReturn(true);
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)).thenReturn(favoritePage);
        
        // When
        Page<Favorite> result = interactionService.getFavoriteList(userId, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(favoriteRepository).findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Test
    void testGetFavoriteList_UserNotExists_ThrowsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.getFavoriteList(userId, pageable));
        assertEquals("用户不存在", exception.getMessage());
    }
    
    @Test
    void testGetFollowingList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Follow follow1 = new Follow();
        follow1.setFollowerId(userId);
        follow1.setFollowingId(2L);
        
        Follow follow2 = new Follow();
        follow2.setFollowerId(userId);
        follow2.setFollowingId(3L);
        
        List<Follow> follows = Arrays.asList(follow1, follow2);
        Page<Follow> followPage = new PageImpl<>(follows, pageable, follows.size());
        
        when(userRepository.existsById(userId)).thenReturn(true);
        when(followRepository.findByFollowerId(userId, pageable)).thenReturn(followPage);
        
        // When
        Page<Follow> result = interactionService.getFollowingList(userId, pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(followRepository).findByFollowerId(userId, pageable);
    }
    
    @Test
    void testGetFollowingList_UserNotExists_ThrowsException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.existsById(userId)).thenReturn(false);
        
        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, 
            () -> interactionService.getFollowingList(userId, pageable));
        assertEquals("用户不存在", exception.getMessage());
    }
    
    @Test
    void testIsLiked_True() {
        // Given
        when(likeRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(true);
        
        // When
        boolean result = interactionService.isLiked(userId, articleId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testIsLiked_False() {
        // Given
        when(likeRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(false);
        
        // When
        boolean result = interactionService.isLiked(userId, articleId);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testIsFavorited_True() {
        // Given
        when(favoriteRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(true);
        
        // When
        boolean result = interactionService.isFavorited(userId, articleId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testIsFavorited_False() {
        // Given
        when(favoriteRepository.existsByUserIdAndArticleId(userId, articleId)).thenReturn(false);
        
        // When
        boolean result = interactionService.isFavorited(userId, articleId);
        
        // Then
        assertFalse(result);
    }
    
    @Test
    void testIsFollowing_True() {
        // Given
        when(followRepository.existsByFollowerIdAndFollowingId(userId, followingId)).thenReturn(true);
        
        // When
        boolean result = interactionService.isFollowing(userId, followingId);
        
        // Then
        assertTrue(result);
    }
    
    @Test
    void testIsFollowing_False() {
        // Given
        when(followRepository.existsByFollowerIdAndFollowingId(userId, followingId)).thenReturn(false);
        
        // When
        boolean result = interactionService.isFollowing(userId, followingId);
        
        // Then
        assertFalse(result);
    }
}
