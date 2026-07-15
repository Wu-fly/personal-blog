package com.blog.service.impl;

import com.blog.entity.Favorite;
import com.blog.entity.Follow;
import com.blog.entity.Like;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.FavoriteRepository;
import com.blog.repository.FollowRepository;
import com.blog.repository.LikeRepository;
import com.blog.repository.PurchaseRepository;
import com.blog.repository.UserRepository;
import com.blog.service.InteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Interaction Service Implementation
 * Handles user interactions: like, favorite, follow
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {
    
    private final LikeRepository likeRepository;
    private final FavoriteRepository favoriteRepository;
    private final FollowRepository followRepository;
    private final PurchaseRepository purchaseRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    
    /**
     * Toggle like on article
     * 权限: 需求 38.4 - 需要登录才能点赞
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public boolean toggleLike(Long userId, Long articleId) {
        log.info("Toggling like for user {} on article {}", userId, articleId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        
        // Validate article exists
        if (!articleRepository.existsById(articleId)) {
            throw new BusinessException("文章不存在");
        }
        
        // Check if already liked
        boolean alreadyLiked = likeRepository.existsByUserIdAndArticleId(userId, articleId);
        
        if (alreadyLiked) {
            // Unlike: remove like and decrement count
            likeRepository.deleteByUserIdAndArticleId(userId, articleId);
            articleRepository.decrementLikeCount(articleId);
            log.info("User {} unliked article {}", userId, articleId);
            return false;
        } else {
            // Like: add like and increment count
            Like like = new Like();
            like.setUserId(userId);
            like.setArticleId(articleId);
            likeRepository.save(like);
            articleRepository.incrementLikeCount(articleId);
            log.info("User {} liked article {}", userId, articleId);
            return true;
        }
    }
    
    /**
     * Toggle favorite on article
     * 权限: 需求 38.5 - 需要登录才能收藏
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public boolean toggleFavorite(Long userId, Long articleId) {
        log.info("Toggling favorite for user {} on article {}", userId, articleId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        
        // Validate article exists
        if (!articleRepository.existsById(articleId)) {
            throw new BusinessException("文章不存在");
        }
        
        // Check if already favorited
        boolean alreadyFavorited = favoriteRepository.existsByUserIdAndArticleId(userId, articleId);
        
        if (alreadyFavorited) {
            // Unfavorite: remove favorite and decrement count
            favoriteRepository.deleteByUserIdAndArticleId(userId, articleId);
            articleRepository.decrementFavoriteCount(articleId);
            log.info("User {} unfavorited article {}", userId, articleId);
            return false;
        } else {
            // Favorite: add favorite and increment count
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setArticleId(articleId);
            favoriteRepository.save(favorite);
            articleRepository.incrementFavoriteCount(articleId);
            log.info("User {} favorited article {}", userId, articleId);
            return true;
        }
    }
    
    /**
     * Toggle follow on user
     * 权限: 需求 38.7 - 需要登录才能关注博主
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public boolean toggleFollow(Long followerId, Long followingId) {
        log.info("Toggling follow for user {} on user {}", followerId, followingId);
        
        // Validate follower exists
        if (!userRepository.existsById(followerId)) {
            throw new BusinessException("关注者不存在");
        }
        
        // Validate following user exists
        if (!userRepository.existsById(followingId)) {
            throw new BusinessException("被关注用户不存在");
        }
        
        // Cannot follow self
        if (followerId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }
        
        // Check if already following
        boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
        
        if (alreadyFollowing) {
            // Unfollow: remove follow relationship
            followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
            log.info("User {} unfollowed user {}", followerId, followingId);
            return false;
        } else {
            // Follow: add follow relationship
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFollowingId(followingId);
            followRepository.save(follow);
            log.info("User {} followed user {}", followerId, followingId);
            return true;
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Favorite> getFavoriteList(Long userId, Pageable pageable) {
        log.info("Getting favorite list for user {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Like> getLikeList(Long userId, Pageable pageable) {
        log.info("Getting like list for user {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        
        return likeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<com.blog.entity.Purchase> getPurchaseList(Long userId, Pageable pageable) {
        log.info("Getting purchase list for user {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        
        return purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Follow> getFollowingList(Long userId, Pageable pageable) {
        log.info("Getting following list for user {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
        
        return followRepository.findByFollowerId(userId, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long articleId) {
        return likeRepository.existsByUserIdAndArticleId(userId, articleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(Long userId, Long articleId) {
        return favoriteRepository.existsByUserIdAndArticleId(userId, articleId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }
}
