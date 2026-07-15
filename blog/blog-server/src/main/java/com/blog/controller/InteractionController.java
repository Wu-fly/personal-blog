package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.Favorite;
import com.blog.entity.Follow;
import com.blog.entity.Like;
import com.blog.entity.Transaction;
import com.blog.security.CustomUserDetails;
import com.blog.service.InteractionService;
import com.blog.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.HashMap;
import java.util.Map;

/**
 * 互动控制�?
 * 处理点赞、收藏、关注、打赏等用户互动功能
 * 需�? 5.1-5.5, 16.1-16.5, 17.1-17.5
 */
@Slf4j
@RestController
@RequestMapping("/interactions")
@RequiredArgsConstructor
@Validated
public class InteractionController {

    private final InteractionService interactionService;
    private final WalletService walletService;

    /**
     * 点赞/取消点赞文章
     * POST /api/interactions/like
     * 需�? 5.1, 5.2
     * 权限: 登录用户
     * 
     * @param request 点赞请求
     * @param userDetails 当前登录用户
     * @return 点赞状�?
     */
    @PostMapping("/like")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleLike(
            @Valid @RequestBody LikeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} toggling like on article {}", userDetails.getId(), request.getArticleId());
        
        boolean liked = interactionService.toggleLike(userDetails.getId(), request.getArticleId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("message", liked ? "点赞成功" : "取消点赞成功");
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 收藏/取消收藏文章
     * POST /api/interactions/favorite
     * 需�? 5.3, 5.4
     * 权限: 登录用户
     * 
     * @param request 收藏请求
     * @param userDetails 当前登录用户
     * @return 收藏状�?
     */
    @PostMapping("/favorite")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleFavorite(
            @Valid @RequestBody FavoriteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} toggling favorite on article {}", userDetails.getId(), request.getArticleId());
        
        boolean favorited = interactionService.toggleFavorite(userDetails.getId(), request.getArticleId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        result.put("message", favorited ? "收藏成功" : "取消收藏成功");
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 关注/取消关注用户
     * POST /api/interactions/follow
     * 需�? 16.1, 16.2
     * 权限: 登录用户
     * 
     * @param request 关注请求
     * @param userDetails 当前登录用户
     * @return 关注状�?
     */
    @PostMapping("/follow")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleFollow(
            @Valid @RequestBody FollowRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} toggling follow on user {}", userDetails.getId(), request.getUserId());
        
        // 不能关注自己
        if (userDetails.getId().equals(request.getUserId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CANNOT_FOLLOW_SELF", "不能关注自己"));
        }
        
        boolean followed = interactionService.toggleFollow(userDetails.getId(), request.getUserId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("followed", followed);
        result.put("message", followed ? "关注成功" : "取消关注成功");
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 打赏博主
     * POST /api/interactions/reward
     * 需�? 17.1-17.5
     * 权限: 登录用户
     * 
     * @param request 打赏请求
     * @param userDetails 当前登录用户
     * @return 打赏交易记录
     */
    @PostMapping("/reward")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Transaction>> reward(
            @Valid @RequestBody RewardRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} rewarding user {} with amount {}", 
                userDetails.getId(), request.getBloggerId(), request.getAmount());
        
        // 不能打赏自己
        if (userDetails.getId().equals(request.getBloggerId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("CANNOT_REWARD_SELF", "不能打赏自己"));
        }
        
        Transaction transaction = walletService.reward(
                userDetails.getId(), 
                request.getBloggerId(), 
                request.getAmount(),
                request.getArticleId()
        );
        
        return ResponseEntity.ok(ApiResponse.success("打赏成功", transaction));
    }

    /**
     * 获取收藏列表
     * GET /api/interactions/favorites
     * 需�? 21.3
     * 权限: 登录用户
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param userDetails 当前登录用户
     * @return 收藏列表分页数据
     */
    @GetMapping("/favorites")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<Favorite>>> getFavorites(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} fetching favorites, page: {}, size: {}", userDetails.getId(), page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> favorites = interactionService.getFavoriteList(userDetails.getId(), pageable);
        
        return ResponseEntity.ok(ApiResponse.success(favorites));
    }
    /**
         * 获取点赞列表
         * GET /api/interactions/likes
         * 需求: 16.5
         * 权限: 登录用户
         *
         * @param page 页码（从0开始）
         * @param size 每页大小
         * @param userDetails 当前登录用户
         * @return 点赞列表分页数据
         */
        @GetMapping("/likes")
        @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
        public ResponseEntity<ApiResponse<Page<Like>>> getLikes(
                @RequestParam(defaultValue = "0") @Min(0) int page,
                @RequestParam(defaultValue = "10") @Min(1) int size,
                @AuthenticationPrincipal CustomUserDetails userDetails) {

            log.info("User {} fetching likes, page: {}, size: {}", userDetails.getId(), page, size);

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Like> likes = interactionService.getLikeList(userDetails.getId(), pageable);

            return ResponseEntity.ok(ApiResponse.success(likes));
        }

    /**
     * 获取已购文章列表
     * GET /api/interactions/purchases
     * 需求: 用户中心-已购文章
     * 权限: 登录用户
     * 
     * @param page 页码
     * @param size 每页大小
     * @param userDetails 当前登录用户
     * @return 已购文章列表分页数据
     */
    @GetMapping("/purchases")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<com.blog.entity.Purchase>>> getPurchases(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} fetching purchases, page: {}, size: {}", userDetails.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<com.blog.entity.Purchase> purchases = interactionService.getPurchaseList(userDetails.getId(), pageable);

        return ResponseEntity.ok(ApiResponse.success(purchases));
    }

    /**
     * 获取关注列表
     * GET /api/interactions/following
     * 需�? 16.4
     * 权限: 登录用户
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param userDetails 当前登录用户
     * @return 关注列表分页数据
     */
    @GetMapping("/following")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<Follow>>> getFollowing(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("User {} fetching following list, page: {}, size: {}", userDetails.getId(), page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Follow> following = interactionService.getFollowingList(userDetails.getId(), pageable);
        
        return ResponseEntity.ok(ApiResponse.success(following));
    }

    /**
     * 检查是否已点赞文章
     * GET /api/interactions/like/status
     * 权限: 登录用户
     * 
     * @param articleId 文章ID
     * @param userDetails 当前登录用户
     * @return 点赞状�?
     */
    @GetMapping("/like/status")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getLikeStatus(
            @RequestParam Long articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        boolean liked = interactionService.isLiked(userDetails.getId(), articleId);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("liked", liked);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 检查是否已收藏文章
     * GET /api/interactions/favorite/status
     * 权限: 登录用户
     * 
     * @param articleId 文章ID
     * @param userDetails 当前登录用户
     * @return 收藏状�?
     */
    @GetMapping("/favorite/status")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getFavoriteStatus(
            @RequestParam Long articleId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        boolean favorited = interactionService.isFavorited(userDetails.getId(), articleId);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", favorited);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 检查是否已关注用户
     * GET /api/interactions/follow/status
     * 权限: 登录用户
     * 
     * @param userId 用户ID
     * @param userDetails 当前登录用户
     * @return 关注状�?
     */
    @GetMapping("/follow/status")
    @PreAuthorize("hasAnyRole('USER', 'BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getFollowStatus(
            @RequestParam Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        boolean following = interactionService.isFollowing(userDetails.getId(), userId);
        
        Map<String, Boolean> result = new HashMap<>();
        result.put("following", following);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

