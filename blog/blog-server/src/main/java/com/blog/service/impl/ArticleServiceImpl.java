package com.blog.service.impl;

import com.blog.config.CacheConfig;
import com.blog.entity.Article;
import com.blog.entity.ArticleTag;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CarouselConfigRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.PurchaseRepository;
import com.blog.repository.UserRepository;
import com.blog.service.ArticleService;
import com.blog.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章管理服务实现
 * 需求: 2.1-2.8, 3.1-3.4, 27.1-27.6
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CarouselConfigRepository carouselConfigRepository;
    private final CacheService cacheService;
    private final PurchaseRepository purchaseRepository;
    private final com.blog.repository.ArticleTagRepository articleTagRepository;
    private final com.blog.repository.TagRepository tagRepository;

    /**
     * 创建文章
     * 需求: 2.1
     * - 保存文章到数据库
     * - 设置审核状态为待审核
     * - 验证标题和内容不为空
     * - 验证付费文章价格
     * 
     * 权限: 需求 39.2 - 只有博主和管理员可以创建文章
     */
    @Override
    @Transactional
    public Article createArticle(Article article) {
        log.info("Creating article with title: {}", article.getTitle());
        
        // 验证标题和内容不为空 (需求: 2.5)
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            throw new BusinessException("INVALID_INPUT", "文章标题不能为空");
        }
        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            throw new BusinessException("INVALID_INPUT", "文章内容不能为空");
        }
        
        // 验证用户是否存在且是博主
        User user = userRepository.findById(article.getUserId())
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        if (user.getRole() != User.UserRole.BLOGGER && user.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException("PERMISSION_DENIED", "只有博主才能发布文章");
        }
        
        // 验证付费文章价格 (需求: 2.7)
        if (article.getIsPaid() != null && article.getIsPaid()) {
            if (article.getPrice() == null || article.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("INVALID_PRICE", "付费文章价格必须大于0");
            }
        }
        
        // 设置默认封面图片 (需求: 2.6)
        if (article.getCoverImage() == null || article.getCoverImage().trim().isEmpty()) {
            article.setCoverImage("/default-cover.jpg");
        }
        
        // 设置审核状态为待审核 (需求: 2.1)
        article.setReviewStatus(Article.ReviewStatus.PENDING);
        
        // 初始化计数器
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setPurchaseCount(0);
        article.setIsPinned(false);
        
        Article savedArticle = articleRepository.save(article);
        
        // Evict hot articles cache after creating new article
        cacheService.evictHotArticlesCache();
        
        log.info("Article created successfully with id: {}", savedArticle.getId());
        
        return savedArticle;
    }
    
    /**
     * 保存文章标签关联
     */
    @Transactional
    public void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        
        log.info("Saving tags for article: {}, tagIds: {}", articleId, tagIds);
        
        // 删除旧的标签关联
        articleTagRepository.deleteByArticleId(articleId);
        
        // 创建新的标签关联
        for (Long tagId : tagIds) {
            ArticleTag articleTag = new ArticleTag();
            articleTag.setArticleId(articleId);
            articleTag.setTagId(tagId);
            articleTagRepository.save(articleTag);
        }
        
        log.info("Tags saved successfully for article: {}", articleId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.blog.entity.Tag> getArticleTags(Long articleId) {
        log.info("Getting tags for article: {}", articleId);
        List<ArticleTag> articleTags = articleTagRepository.findByArticleId(articleId);
        List<com.blog.entity.Tag> tags = new java.util.ArrayList<>();
        
        for (ArticleTag articleTag : articleTags) {
            if (articleTag.getTag() != null) {
                // 触发懒加载
                articleTag.getTag().getName();
                tags.add(articleTag.getTag());
            }
        }
        
        return tags;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Article> getArticlesByTag(Long tagId, Pageable pageable) {
        log.info("Getting articles by tag: {}, page: {}", tagId, pageable.getPageNumber());
        
        // 获取该标签下的所有文章ID
        List<ArticleTag> articleTags = articleTagRepository.findByTagId(tagId);
        List<Long> articleIds = articleTags.stream()
                .map(ArticleTag::getArticleId)
                .collect(java.util.stream.Collectors.toList());
        
        if (articleIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // 查询这些文章（只返回审核通过的）
        List<Article> allArticles = articleRepository.findAllById(articleIds).stream()
                .filter(article -> article.getReviewStatus() == Article.ReviewStatus.APPROVED)
                .collect(java.util.stream.Collectors.toList());
        
        // 主动加载关联对象以避免LazyInitializationException
        for (Article article : allArticles) {
            if (article.getUser() != null) {
                article.getUser().getNickname(); // 触发懒加载
            }
            if (article.getCategory() != null) {
                article.getCategory().getName(); // 触发懒加载
            }
        }
        
        // 手动分页
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allArticles.size());
        List<Article> pageContent = allArticles.subList(start, end);
        
        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allArticles.size());
    }

    @Override
    @Transactional
    public List<Long> getOrCreateTagIds(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        List<Long> tagIds = new java.util.ArrayList<>();
        
        for (String tagName : tagNames) {
            if (tagName == null || tagName.trim().isEmpty()) {
                continue;
            }
            
            // 查找或创建标签
            com.blog.entity.Tag tag = tagRepository.findByName(tagName.trim())
                    .orElseGet(() -> {
                        com.blog.entity.Tag newTag = new com.blog.entity.Tag();
                        newTag.setName(tagName.trim());
                        return tagRepository.save(newTag);
                    });
            
            tagIds.add(tag.getId());
        }
        
        log.info("Converted tag names {} to IDs {}", tagNames, tagIds);
        return tagIds;
    }

    /**
     * 更新文章
     * 需求: 2.2
     * - 更新文章内容
     * - 重置审核状态为待审核
     * - 保留修改历史（通过updated_at字段）
     * 
     * 权限: 需求 39.2 - 只有博主和管理员可以更新文章
     */
    @Override
    @Transactional
    public Article updateArticle(Long id, Article article) {
        log.info("Updating article with id: {}", id);
        
        // 查找文章
        Article existingArticle = articleRepository.findById(id)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 验证权限：只有文章作者可以编辑
        if (!existingArticle.getUserId().equals(article.getUserId())) {
            throw new BusinessException("PERMISSION_DENIED", "只能编辑自己的文章");
        }
        
        // 验证标题和内容不为空 (需求: 2.5)
        if (article.getTitle() == null || article.getTitle().trim().isEmpty()) {
            throw new BusinessException("INVALID_INPUT", "文章标题不能为空");
        }
        if (article.getContent() == null || article.getContent().trim().isEmpty()) {
            throw new BusinessException("INVALID_INPUT", "文章内容不能为空");
        }
        
        // 验证付费文章价格 (需求: 2.7)
        if (article.getIsPaid() != null && article.getIsPaid()) {
            if (article.getPrice() == null || article.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new BusinessException("INVALID_PRICE", "付费文章价格必须大于0");
            }
        }
        
        // 更新文章内容
        existingArticle.setTitle(article.getTitle());
        existingArticle.setContent(article.getContent());
        existingArticle.setSummary(article.getSummary());
        existingArticle.setCoverImage(article.getCoverImage());
        existingArticle.setCategoryId(article.getCategoryId());
        existingArticle.setIsPaid(article.getIsPaid());
        existingArticle.setPrice(article.getPrice());
        
        // 重置审核状态为待审核 (需求: 2.2)
        existingArticle.setReviewStatus(Article.ReviewStatus.PENDING);
        existingArticle.setReviewComment(null);
        
        Article updatedArticle = articleRepository.save(existingArticle);
        
        // 主动加载关联对象以避免LazyInitializationException
        if (updatedArticle.getUser() != null) {
            updatedArticle.getUser().getNickname(); // 触发懒加载
        }
        if (updatedArticle.getCategory() != null) {
            updatedArticle.getCategory().getName(); // 触发懒加载
        }
        
        // Evict caches after updating article
        cacheService.evictArticleDetailCache(id);
        cacheService.evictHotArticlesCache();
        
        log.info("Article updated successfully with id: {}", updatedArticle.getId());
        
        return updatedArticle;
    }

    /**
     * 删除文章
     * 需求: 2.3
     * - 从数据库中移除文章
     * - 级联删除相关评论（通过数据库外键约束）
     * 
     * 权限: 需求 39.2 - 只有博主和管理员可以删除文章
     */
    @Override
    @Transactional
    public void deleteArticle(Long id, Long userId) {
        log.info("Deleting article with id: {}", id);
        
        // 查找文章
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 验证权限：只有文章作者可以删除
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("PERMISSION_DENIED", "只能删除自己的文章");
        }
        
        // 删除文章（级联删除评论和关联关系通过数据库外键约束实现）
        articleRepository.delete(article);
        
        // Evict caches after deleting article
        cacheService.evictArticleDetailCache(id);
        cacheService.evictHotArticlesCache();
        
        log.info("Article deleted successfully with id: {}", id);
    }

    /**
     * 查询文章列表
     * 需求: 2.4, 3.1, 29.1-29.9
     * - 支持分类筛选
     * - 支持用户筛选
     * - 支持关键词搜索
     * - 支持排序（最新发布、最多浏览、最多收藏、最多购买）
     * - 支持分页
     * - 只返回审核通过的文章
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Article> getArticles(Long categoryId, Long userId, String keyword, Pageable pageable) {
        log.info("Querying articles with categoryId: {}, userId: {}, keyword: {}", categoryId, userId, keyword);
        
        Page<Article> articles;
        
        // 关键词搜索 (需求: 4.1-4.6)
        if (keyword != null && !keyword.trim().isEmpty()) {
            articles = articleRepository.searchByKeyword(keyword.trim(), pageable);
        }
        // 分类筛选 (需求: 29.1-29.3)
        else if (categoryId != null) {
            articles = articleRepository.findApprovedArticlesByCategory(categoryId, pageable);
        }
        // 用户筛选（个人空间）(需求: 20.1-20.8)
        else if (userId != null) {
            articles = articleRepository.findApprovedArticlesByUser(userId, pageable);
        }
        // 默认查询所有审核通过的文章
        else {
            articles = articleRepository.findApprovedArticles(pageable);
        }
        
        // 手动加载关联对象以避免懒加载问题
        articles.forEach(article -> {
            if (article.getUser() != null) {
                article.getUser().getNickname(); // 触发加载
            }
            if (article.getCategory() != null) {
                article.getCategory().getName(); // 触发加载
            }
        });
        
        log.info("Found {} articles", articles.getTotalElements());
        return articles;
    }

    /**
     * 查询文章详情
     * 需求: 3.2, 3.3
     * - 返回完整的文章内容和作者信息
     * - 增加浏览计数
     * - 验证文章是否审核通过
     */
    @Override
    @Transactional(readOnly = true)
    // 暂时禁用缓存，因为Article实体包含Hibernate代理无法序列化
    // @Cacheable(value = CacheConfig.ARTICLE_DETAIL_CACHE, key = "#id", unless = "#result == null")
    public Article getArticleDetail(Long id, Long userId, Boolean forEdit) {
        log.info("Getting article detail with id: {}, forEdit: {}", id, forEdit);
        
        // 查找文章
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 主动加载user和category关系以避免LazyInitializationException
        if (article.getUser() != null) {
            article.getUser().getNickname(); // 触发懒加载
        }
        if (article.getCategory() != null) {
            article.getCategory().getName(); // 触发懒加载
        }
        
        // 验证文章是否审核通过 (需求: 2.8)
        // 只有文章作者和管理员可以查看未审核的文章
        if (article.getReviewStatus() != Article.ReviewStatus.APPROVED) {
            if (userId == null || !userId.equals(article.getUserId())) {
                User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
                if (user == null || user.getRole() != User.UserRole.ADMIN) {
                    throw new BusinessException("ARTICLE_NOT_APPROVED", "文章未审核通过");
                }
            }
        }
        
        log.info("Article detail retrieved successfully with id: {}", id);
        return article;
    }

    /**
     * 置顶文章
     * 需求: 27.1-27.6
     * - 设置文章置顶状态
     * - 记录置顶时间
     * - 限制每个博主最多置顶5篇文章
     * 
     * 权限: 需求 39.2 - 只有博主和管理员可以置顶文章
     */
    @Override
    @Transactional
    public Article pinArticle(Long id, Long userId, boolean isPinned) {
        log.info("Pinning article with id: {}, isPinned: {}", id, isPinned);
        
        // 查找文章
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 验证权限：只有文章作者可以置顶
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("PERMISSION_DENIED", "只能置顶自己的文章");
        }
        
        // 如果要置顶，检查置顶文章数量限制 (需求: 27.6)
        if (isPinned && !article.getIsPinned()) {
            List<Article> pinnedArticles = articleRepository.findPinnedArticlesByUser(userId);
            if (pinnedArticles.size() >= 5) {
                throw new BusinessException("PIN_LIMIT_EXCEEDED", "最多只能置顶5篇文章");
            }
        }
        
        // 更新置顶状态
        article.setIsPinned(isPinned);
        if (isPinned) {
            article.setPinnedAt(LocalDateTime.now());
        } else {
            article.setPinnedAt(null);
        }
        
        Article updatedArticle = articleRepository.save(article);
        log.info("Article pinned successfully with id: {}", id);
        
        return updatedArticle;
    }

    /**
     * 查询用户的置顶文章
     * 需求: 27.3
     * - 返回用户的所有置顶文章
     * - 按置顶时间倒序排列
     */
    @Override
    @Transactional(readOnly = true)
    public List<Article> getPinnedArticles(Long userId) {
        log.info("Getting pinned articles for user: {}", userId);
        return articleRepository.findPinnedArticlesByUser(userId);
    }

    /**
     * 用户端查询文章详情
     * 需求: 2.8
     * - 只返回审核通过的文章
     * - 增加浏览计数
     * - 未审核或审核拒绝的文章不可见
     */
    @Transactional
    public Article getArticleByIdForUser(Long id) {
        log.info("Getting article by id for user: {}", id);
        
        // 查找文章
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在或未通过审核"));
        
        // 验证文章是否审核通过 (需求: 2.8)
        if (article.getReviewStatus() != Article.ReviewStatus.APPROVED) {
            throw new BusinessException("ARTICLE_NOT_APPROVED", "文章不存在或未通过审核");
        }
        
        // 增加浏览计数 (需求: 3.3)
        article.setViewCount(article.getViewCount() + 1);
        Article savedArticle = articleRepository.save(article);
        
        log.info("Article retrieved successfully for user with id: {}", id);
        return savedArticle;
    }

    /**
     * 博主端查询文章详情
     * 需求: 2.8
     * - 博主可以查看自己的所有文章（包括未审核的）
     * - 博主不能查看其他博主的未审核文章
     */
    @Transactional(readOnly = true)
    public Article getArticleByIdForBlogger(Long id, Long bloggerId) {
        log.info("Getting article by id for blogger: {}, articleId: {}", bloggerId, id);
        
        // 查找文章
        Article article = articleRepository.findById(id)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 验证权限：只能查看自己的文章
        if (!article.getUserId().equals(bloggerId)) {
            throw new BusinessException("PERMISSION_DENIED", "只能查看自己的文章");
        }
        
        log.info("Article retrieved successfully for blogger with id: {}", id);
        return article;
    }

    /**
     * 获取用户的所有文章（包括所有审核状态）
     * 用于个人中心"我的文章"页面
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Article> getUserArticles(Long userId, Pageable pageable) {
        log.info("Getting all articles for user: userId={}", userId);
        return articleRepository.findByUserId(userId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<com.blog.entity.CarouselConfig> getCarouselConfigs() {
        log.info("Getting carousel configs");
        return carouselConfigRepository.findAllByOrderByDisplayOrderAsc();
    }

    @Override
    @Transactional
    public void incrementViewCount(Long articleId) {
        log.info("Incrementing view count for article: {}", articleId);
        articleRepository.incrementViewCount(articleId);
    }
}
