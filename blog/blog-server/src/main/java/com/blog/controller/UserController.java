package com.blog.controller;

import com.blog.dto.*;
import com.blog.entity.BloggerApplication;
import com.blog.entity.SpaceSetting;
import com.blog.security.CustomUserDetails;
import com.blog.service.BloggerService;
import com.blog.service.UserService;
import com.blog.service.impl.BloggerServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * User Controller
 * Handles user profile, blogger application, and personal space
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final BloggerService bloggerService;
    private final com.blog.service.BrowseHistoryService browseHistoryService;

    /**
     * Get current user profile
     * GET /users/profile
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            log.error("User details is null, user not authenticated");
            return ResponseEntity.status(401)
                    .body(ApiResponse.error("Not authenticated"));
        }
        
        log.info("Getting profile for user: {}", userDetails.getId());
        
        UserProfileResponse profile = userService.getUserProfile(userDetails.getId());
        
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Update current user profile
     * PUT /users/profile
     */
    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        log.info("Updating profile for user: {}", userDetails.getId());
        
        UserProfileResponse profile = userService.updateUserProfile(
                userDetails.getId(),
                request.getNickname(),
                request.getAvatar(),
                request.getBio()
        );
        
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    /**
     * Apply for blogger
     * POST /users/apply-blogger
     */
    @PostMapping("/apply-blogger")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<BloggerApplication>> applyForBlogger(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BloggerApplicationRequest request) {
        log.info("User {} applying for blogger", userDetails.getId());
        
        String field = null;
        if (request.getCustomField() != null && !request.getCustomField().trim().isEmpty()) {
            field = request.getCustomField().trim();
        } else if (request.getFields() != null && !request.getFields().trim().isEmpty()) {
            field = request.getFields();
        }
        
        BloggerApplication application = ((BloggerServiceImpl) bloggerService).applyForBlogger(
                userDetails.getId(),
                request.getNickname(),
                request.getBio(),
                field
        );
        
        return ResponseEntity.ok(ApiResponse.success("Application submitted", application));
    }

    /**
     * Get blogger application status
     * GET /users/blogger-application
     */
    @GetMapping("/blogger-application")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBloggerApplicationStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Getting blogger application status for user: {}", userDetails.getId());
        
        BloggerApplication application = bloggerService.getApplicationStatus(userDetails.getId())
                .orElse(null);
        
        if (application == null) {
            return ResponseEntity.ok(ApiResponse.success("No application found", null));
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", application.getId());
        result.put("userId", application.getUserId());
        result.put("nickname", application.getNickname());
        result.put("bio", application.getBio());
        result.put("status", application.getStatus().name());
        result.put("reviewComment", application.getReviewComment());
        result.put("createdAt", application.getCreatedAt());
        result.put("reviewedAt", application.getReviewedAt());
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * Cancel blogger application
     * DELETE /users/blogger-application
     */
    @DeleteMapping("/blogger-application")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> cancelBloggerApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("User {} canceling blogger application", userDetails.getId());
        
        bloggerService.cancelApplication(userDetails.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Application canceled", "SUCCESS"));
    }

    /**
     * Get user personal space
     * GET /users/{id}/space
     */
    @GetMapping("/{id}/space")
    public ResponseEntity<ApiResponse<PersonalSpaceResponse>> getPersonalSpace(
            @PathVariable Long id) {
        log.info("Getting personal space for user: {}", id);
        
        PersonalSpaceResponse space = userService.getPersonalSpace(id);
        
        return ResponseEntity.ok(ApiResponse.success(space));
    }

    /**
     * Update personal space settings
     * PUT /users/space/settings
     */
    @PutMapping("/space/settings")
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SpaceSetting>> updateSpaceSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SpaceSettingsRequest request) {
        log.info("Updating space settings for user: {}", userDetails.getId());
        
        SpaceSetting.LayoutStyle layoutStyle = null;
        if (request.getLayoutStyle() != null) {
            layoutStyle = SpaceSetting.LayoutStyle.valueOf(request.getLayoutStyle());
        }
        
        SpaceSetting settings = bloggerService.saveSpaceSettings(
                userDetails.getId(),
                request.getThemeColor(),
                request.getBackgroundImage(),
                layoutStyle
        );
        
        return ResponseEntity.ok(ApiResponse.success("Settings updated", settings));
    }

    /**
     * Get personal space settings
     * GET /users/space/settings
     */
    @GetMapping("/space/settings")
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SpaceSetting>> getSpaceSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Getting space settings for user: {}", userDetails.getId());
        
        SpaceSetting settings = bloggerService.getSpaceSettings(userDetails.getId())
                .orElse(null);
        
        return ResponseEntity.ok(ApiResponse.success(settings));
    }

    /**
     * Reset personal space settings to default
     * POST /users/space/settings/reset
     */
    @PostMapping("/space/settings/reset")
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<SpaceSetting>> resetSpaceSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Resetting space settings for user: {}", userDetails.getId());
        
        SpaceSetting settings = bloggerService.resetSpaceSettings(userDetails.getId());
        
        return ResponseEntity.ok(ApiResponse.success("Settings reset to default", settings));
    }

    /**
     * Get browse history
     * GET /users/browse-history
     */
    @GetMapping("/browse-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<com.blog.entity.BrowseHistory>>> getBrowseHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        log.info("Getting browse history for user: {}", userDetails.getId());
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                page, size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "updatedAt"));
        org.springframework.data.domain.Page<com.blog.entity.BrowseHistory> history = browseHistoryService.getBrowseHistory(userDetails.getId(), pageable);
        
        return ResponseEntity.ok(ApiResponse.success(history));
    }

    /**
     * Delete browse history record
     * DELETE /users/browse-history/{articleId}
     */
    @DeleteMapping("/browse-history/{articleId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> deleteBrowseHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long articleId) {
        log.info("Deleting browse history for user: {}, article: {}", userDetails.getId(), articleId);
        
        browseHistoryService.deleteBrowseHistory(userDetails.getId(), articleId);
        
        return ResponseEntity.ok(ApiResponse.success("Browse history deleted"));
    }

    /**
     * Clear all browse history
     * DELETE /users/browse-history
     */
    @DeleteMapping("/browse-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> clearBrowseHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("Clearing all browse history for user: {}", userDetails.getId());
        
        browseHistoryService.deleteAllBrowseHistory(userDetails.getId());
        
        return ResponseEntity.ok(ApiResponse.success("All browse history cleared"));
    }
}
