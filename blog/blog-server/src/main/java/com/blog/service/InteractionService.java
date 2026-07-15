package com.blog.service;

import com.blog.entity.Favorite;
import com.blog.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interaction Service Interface
 * Handles user interactions: like, favorite, follow
 */
public interface InteractionService {
    
    /**
     * Toggle like on an article
     * If user already liked, remove like; otherwise add like
     * 
     * @param userId User ID
     * @param articleId Article ID
     * @return true if liked, false if unliked
     */
    boolean toggleLike(Long userId, Long articleId);
    
    /**
     * Toggle favorite on an article
     * If user already favorited, remove favorite; otherwise add favorite
     * 
     * @param userId User ID
     * @param articleId Article ID
     * @return true if favorited, false if unfavorited
     */
    boolean toggleFavorite(Long userId, Long articleId);
    
    /**
     * Toggle follow on a user
     * If user already following, unfollow; otherwise follow
     * 
     * @param followerId Follower user ID
     * @param followingId Following user ID
     * @return true if followed, false if unfollowed
     */
    boolean toggleFollow(Long followerId, Long followingId);
    
    /**
     * Get user's favorite list
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of favorites
     */
    Page<Favorite> getFavoriteList(Long userId, Pageable pageable);
    
    /**
     * Get user's like list
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of likes
     */
    Page<com.blog.entity.Like> getLikeList(Long userId, Pageable pageable);
    
    /**
     * Get user's purchase list
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of purchases
     */
    Page<com.blog.entity.Purchase> getPurchaseList(Long userId, Pageable pageable);
    
    /**
     * Get user's following list
     * 
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of follows
     */
    Page<Follow> getFollowingList(Long userId, Pageable pageable);
    
    /**
     * Check if user liked an article
     * 
     * @param userId User ID
     * @param articleId Article ID
     * @return true if liked, false otherwise
     */
    boolean isLiked(Long userId, Long articleId);
    
    /**
     * Check if user favorited an article
     * 
     * @param userId User ID
     * @param articleId Article ID
     * @return true if favorited, false otherwise
     */
    boolean isFavorited(Long userId, Long articleId);
    
    /**
     * Check if user is following another user
     * 
     * @param followerId Follower user ID
     * @param followingId Following user ID
     * @return true if following, false otherwise
     */
    boolean isFollowing(Long followerId, Long followingId);
}
