package com.blog.controller;

import com.blog.annotation.RateLimit;
import com.blog.dto.ApiResponse;
import com.blog.dto.ArticleRequest;
import com.blog.dto.ArticleResponse;
import com.blog.dto.PinArticleRequest;
import com.blog.entity.Article;
import com.blog.security.CustomUserDetails;
import com.blog.service.ArticleService;
import com.blog.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 文章控制�?
 * 处理文章的创建、更新、删除、查询、置顶等操作
 * 需�? 2.1-2.8, 3.1-3.4, 4.1-4.6, 27.1-27.6, 29.1-29.9
 */
@Slf4j
@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
@Validated
public class ArticleController {

    private final ArticleService articleService;
    private final SearchService searchService;
    private final com.blog.repository.PurchaseRepository purchaseRepository;
    private final com.blog.service.BrowseHistoryService browseHistoryService;

    /**
     * 创建文章
     * POST /api/articles
     * 需�? 2.1
     * 权限: 所有登录用户（普通用户创建的文章需要审核）
     * 
     * @param request 文章创建请求
     * @param userDetails 当前登录用户
     * @return 创建的文�?
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ArticleResponse>> createArticle(
            @Valid @RequestBody ArticleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("创建文章请求: userId={}, title={}", userDetails.getId(), request.getTitle());
        
        try {
            // 验证付费文章价格
            Boolean isPaid = request.getIsPaid() != null ? request.getIsPaid() : false;
            if (isPaid && (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("付费文章价格必须大于0"));
            }
            
            // 构建Article实体
            Article article = new Article();
            article.setUserId(userDetails.getId());
            article.setTitle(request.getTitle());
            article.setContent(request.getContent());
            article.setSummary(request.getSummary());
            article.setCoverImage(request.getCoverImage());
            article.setCategoryId(request.getCategoryId());
            article.setIsPaid(isPaid);
            // 非付费文章价格设为null
            article.setPrice(isPaid ? request.getPrice() : null);
            
            Article createdArticle = articleService.createArticle(article);
            
            // 处理标签：如果有标签名称，转换为ID并保存
            List<Long> tagIds = request.getTagIds();
            if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
                tagIds = articleService.getOrCreateTagIds(request.getTagNames());
            }
            
            // 保存文章标签
            if (tagIds != null && !tagIds.isEmpty()) {
                articleService.saveArticleTags(createdArticle.getId(), tagIds);
            }
            
            log.info("文章创建成功: articleId={}, userId={}", createdArticle.getId(), userDetails.getId());
            
            ArticleResponse response = ArticleResponse.fromEntity(createdArticle);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("文章创建成功", response));
        } catch (Exception e) {
            log.error("文章创建失败: userId={}, error={}", userDetails.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 更新文章
     * PUT /api/articles/{id}
     * 需�? 2.2
     * 权限: 博主（仅能更新自己的文章�?
     * 
     * @param id 文章ID
     * @param request 文章更新请求
     * @param userDetails 当前登录用户
     * @return 更新后的文章
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponse>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("更新文章请求: articleId={}, userId={}", id, userDetails.getId());
        
        try {
            // 构建Article实体
            Article article = new Article();
            article.setUserId(userDetails.getId()); // 从认证信息中获取userId
            article.setTitle(request.getTitle());
            article.setContent(request.getContent());
            article.setSummary(request.getSummary());
            article.setCoverImage(request.getCoverImage());
            article.setCategoryId(request.getCategoryId());
            article.setIsPaid(request.getIsPaid() != null ? request.getIsPaid() : false);
            article.setPrice(request.getPrice());
            
            Article updatedArticle = articleService.updateArticle(id, article);
            
            // 处理标签：如果有标签名称，转换为ID并保存
            List<Long> tagIds = request.getTagIds();
            if (request.getTagNames() != null && !request.getTagNames().isEmpty()) {
                tagIds = articleService.getOrCreateTagIds(request.getTagNames());
            }
            
            // 更新文章标签
            if (tagIds != null) {
                articleService.saveArticleTags(id, tagIds);
            }
            
            log.info("文章更新成功: articleId={}, userId={}", id, userDetails.getId());
            
            ArticleResponse response = ArticleResponse.fromEntity(updatedArticle);
            return ResponseEntity.ok(ApiResponse.success("文章更新成功", response));
        } catch (Exception e) {
            log.error("文章更新失败: articleId={}, userId={}, error={}", id, userDetails.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 删除文章
     * DELETE /api/articles/{id}
     * 需�? 2.3
     * 权限: 博主（仅能删除自己的文章�?
     * 
     * @param id 文章ID
     * @param userDetails 当前登录用户
     * @return 成功消息
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("删除文章请求: articleId={}, userId={}", id, userDetails.getId());
        
        try {
            articleService.deleteArticle(id, userDetails.getId());
            log.info("文章删除成功: articleId={}, userId={}", id, userDetails.getId());
            
            return ResponseEntity.ok(ApiResponse.success("文章删除成功", null));
        } catch (Exception e) {
            log.error("文章删除失败: articleId={}, userId={}, error={}", id, userDetails.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 获取文章列表（支持筛选、排序、分页）
     * GET /api/articles
     * 需�? 2.4, 3.1, 29.1-29.9
     * 权限: 公开访问
     * 
     * @param categoryId 分类ID（可选）
     * @param userId 用户ID（可选，用于查询特定用户的文章）
     * @param keyword 搜索关键词（可选）
     * @param sortBy 排序字段（createdAt, viewCount, favoriteCount, purchaseCount�?
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章分页列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getArticles(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("获取文章列表请求: categoryId={}, userId={}, tagId={}, keyword={}, sortBy={}, page={}, size={}", 
                categoryId, userId, tagId, keyword, sortBy, page, size);
        
        try {
            // 构建排序参数
            Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Article> articlePage;
            if (tagId != null) {
                // 按标签筛选
                articlePage = articleService.getArticlesByTag(tagId, pageable);
            } else {
                articlePage = articleService.getArticles(categoryId, userId, keyword, pageable);
            }
            
            // 转换为响应DTO并加载标签
            List<ArticleResponse> articleResponses = new java.util.ArrayList<>();
            for (Article article : articlePage.getContent()) {
                ArticleResponse response = ArticleResponse.fromEntity(article);
                
                // 加载文章标签
                java.util.List<com.blog.entity.Tag> tags = articleService.getArticleTags(article.getId());
                if (tags != null && !tags.isEmpty()) {
                    java.util.List<ArticleResponse.TagResponse> tagResponses = new java.util.ArrayList<>();
                    for (com.blog.entity.Tag tag : tags) {
                        tagResponses.add(new ArticleResponse.TagResponse(tag.getId(), tag.getName()));
                    }
                    response.setTags(tagResponses);
                }
                
                articleResponses.add(response);
            }
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("content", articleResponses);
            data.put("totalElements", articlePage.getTotalElements());
            data.put("totalPages", articlePage.getTotalPages());
            data.put("currentPage", articlePage.getNumber());
            data.put("pageSize", articlePage.getSize());
            data.put("hasNext", articlePage.hasNext());
            data.put("hasPrevious", articlePage.hasPrevious());
            
            log.info("获取文章列表成功: totalElements={}, totalPages={}", 
                    articlePage.getTotalElements(), articlePage.getTotalPages());
            
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("获取文章列表失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 获取文章详情
     * GET /api/articles/{id}
     * 需�? 3.2, 3.3
     * 权限: 公开访问（但会根据用户权限返回不同内容）
     * 
     * @param id 文章ID
     * @param forEdit 是否用于编辑（可选，默认false）
     * @param userDetails 当前登录用户（可选）
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArticleResponse>> getArticleDetail(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") Boolean forEdit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails != null ? userDetails.getId() : null;
        log.info("获取文章详情请求: articleId={}, userId={}, forEdit={}", id, userId, forEdit);
        
        try {
            Article article = articleService.getArticleDetail(id, userId, forEdit);
            
            // 增加浏览计数 (需求: 3.3) - 编辑模式不增加浏览计数
            if (!forEdit) {
                try {
                    articleService.incrementViewCount(id);
                } catch (Exception e) {
                    log.warn("增加浏览计数失败: articleId={}, error={}", id, e.getMessage());
                    // 浏览计数失败不影响文章详情的返回
                }
            }
            
            log.info("获取文章详情成功: articleId={}, viewCount={}", id, article.getViewCount());
            
            // 处理付费文章内容截断逻辑
            boolean shouldTruncate = false;
            if (article.getIsPaid() != null && article.getIsPaid()) {
                boolean isAuthor = userId != null && userId.equals(article.getUserId());
                boolean hasPurchased = userId != null && purchaseRepository.existsByUserIdAndArticleId(userId, id);
                
                // 只有在不是作者且未购买的情况下才截断内容
                shouldTruncate = !isAuthor && !hasPurchased;
                
                log.info("Content truncation check: articleId={}, userId={}, isAuthor={}, hasPurchased={}, shouldTruncate={}", 
                        id, userId, isAuthor, hasPurchased, shouldTruncate);
            }
            
            ArticleResponse response = ArticleResponse.fromEntity(article, shouldTruncate);
            
            // 加载文章标签
            java.util.List<com.blog.entity.Tag> tags = articleService.getArticleTags(id);
            if (tags != null && !tags.isEmpty()) {
                java.util.List<ArticleResponse.TagResponse> tagResponses = new java.util.ArrayList<>();
                for (com.blog.entity.Tag tag : tags) {
                    tagResponses.add(new ArticleResponse.TagResponse(tag.getId(), tag.getName()));
                }
                response.setTags(tagResponses);
            }
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("获取文章详情失败: articleId={}, error={}", id, e.getMessage());
            throw e;
        }
    }

    /**
     * 置顶文章
     * POST /api/articles/{id}/pin
     * 需�? 27.1-27.6
     * 权限: 博主（仅能置顶自己的文章�?
     * 
     * @param id 文章ID
     * @param request 置顶请求
     * @param userDetails 当前登录用户
     * @return 更新后的文章
     */
    @PostMapping("/{id}/pin")
    @PreAuthorize("hasAnyRole('BLOGGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ArticleResponse>> pinArticle(
            @PathVariable Long id,
            @Valid @RequestBody PinArticleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("置顶文章请求: articleId={}, userId={}, isPinned={}", 
                id, userDetails.getId(), request.getIsPinned());
        
        try {
            Article article = articleService.pinArticle(id, userDetails.getId(), request.getIsPinned());
            log.info("文章置顶操作成功: articleId={}, isPinned={}", id, article.getIsPinned());
            
            ArticleResponse response = ArticleResponse.fromEntity(article);
            String message = request.getIsPinned() ? "文章置顶成功" : "取消置顶成功";
            return ResponseEntity.ok(ApiResponse.success(message, response));
        } catch (Exception e) {
            log.error("文章置顶操作失败: articleId={}, userId={}, error={}", 
                    id, userDetails.getId(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * 获取当前用户的文章列�?
     * @param page 页码
     * @param size 每页大小
     * @param userDetails 当前登录用户
     * @return 文章分页列表
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("获取用户文章列表: userId={}, page={}, size={}", userDetails.getId(), page, size);
        
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            // 获取用户的所有文章（包括所有审核状态）
            Page<Article> articles = articleService.getUserArticles(userDetails.getId(), pageable);
            
            // 转换为响应DTO并加载标签
            List<ArticleResponse> articleResponses = new java.util.ArrayList<>();
            for (Article article : articles.getContent()) {
                ArticleResponse response = ArticleResponse.fromEntity(article);
                
                // 加载文章标签
                java.util.List<com.blog.entity.Tag> tags = articleService.getArticleTags(article.getId());
                if (tags != null && !tags.isEmpty()) {
                    java.util.List<ArticleResponse.TagResponse> tagResponses = new java.util.ArrayList<>();
                    for (com.blog.entity.Tag tag : tags) {
                        tagResponses.add(new ArticleResponse.TagResponse(tag.getId(), tag.getName()));
                    }
                    response.setTags(tagResponses);
                }
                
                articleResponses.add(response);
            }
            
            // 构建响应数据，与getArticles方法保持一致
            Map<String, Object> data = new HashMap<>();
            data.put("content", articleResponses);
            data.put("totalElements", articles.getTotalElements());
            data.put("totalPages", articles.getTotalPages());
            data.put("currentPage", articles.getNumber());
            data.put("pageSize", articles.getSize());
            data.put("hasNext", articles.hasNext());
            data.put("hasPrevious", articles.hasPrevious());
            
            log.info("获取用户文章列表成功: userId={}, totalElements={}", 
                    userDetails.getId(), articles.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("获取用户文章列表失败: userId={}, error={}", userDetails.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * 搜索文章
     * GET /api/articles/search
     * 需�? 4.1-4.6
     * 权限: 公开访问
     * 
     * @param keyword 搜索关键�?
     * @param categoryId 分类ID（可选）
     * @param tagId 标签ID（可选）
     * @param sortBy 排序字段（createdAt, viewCount, likeCount, favoriteCount�?
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 文章分页列表
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("搜索文章请求: keyword={}, categoryId={}, tagId={}, sortBy={}, page={}, size={}", 
                keyword, categoryId, tagId, sortBy, page, size);
        
        try {
            // 构建排序参数
            Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Article> articlePage = searchService.search(keyword, categoryId, tagId, pageable);
            
            // 转换为响应DTO
            Page<ArticleResponse> responsePage = articlePage.map(ArticleResponse::fromEntity);
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("content", responsePage.getContent());
            data.put("totalElements", responsePage.getTotalElements());
            data.put("totalPages", responsePage.getTotalPages());
            data.put("currentPage", responsePage.getNumber());
            data.put("pageSize", responsePage.getSize());
            data.put("hasNext", responsePage.hasNext());
            data.put("hasPrevious", responsePage.hasPrevious());
            
            log.info("搜索文章成功: keyword={}, totalElements={}", keyword, responsePage.getTotalElements());
            
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (Exception e) {
            log.error("搜索文章失败: keyword={}, error={}", keyword, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取轮播图配置（公开接口）
     * GET /api/carousel
     *
     * @return 轮播图配置列表
     */
    @GetMapping("/carousel")
    public ResponseEntity<ApiResponse<java.util.List<com.blog.entity.CarouselConfig>>> getCarouselConfigs() {
        log.info("获取轮播图配置");
        try {
            java.util.List<com.blog.entity.CarouselConfig> configs = articleService.getCarouselConfigs();
            log.info("获取轮播图配置成功: count={}", configs.size());
            return ResponseEntity.ok(ApiResponse.success(configs));
        } catch (Exception e) {
            log.error("获取轮播图配置失败: error={}", e.getMessage());
            throw e;
        }
    }

    /**
     * 记录浏览历史
     * POST /articles/{id}/browse
     * 需求: 22.1, 22.2
     * 权限: 登录用户（可选）
     * 
     * @param id 文章ID
     * @param userDetails 当前登录用户（可选）
     * @return 成功消息
     */
    @PostMapping("/{id}/browse")
    public ResponseEntity<ApiResponse<String>> recordBrowseHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        // 只有登录用户才记录浏览历史
        if (userDetails != null) {
            log.info("Recording browse history for user {} on article {}", userDetails.getId(), id);
            browseHistoryService.recordBrowseHistory(userDetails.getId(), id);
            return ResponseEntity.ok(ApiResponse.success("Browse history recorded"));
        }
        
        return ResponseEntity.ok(ApiResponse.success("Not logged in, browse history not recorded"));
    }
}
