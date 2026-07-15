package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CategoryRepository;
import com.blog.repository.TagRepository;
import com.blog.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文章搜索服务实现
 * 需求: 4.1-4.6
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    /**
     * 关键词搜索文章
     * 需求: 4.3
     */
    @Override
    public Page<Article> searchByKeyword(String keyword, Pageable pageable) {
        log.info("Searching articles by keyword: {}", keyword);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("Search keyword is empty, returning all approved articles");
            return articleRepository.findApprovedArticles(pageable);
        }
        
        String trimmedKeyword = keyword.trim();
        Page<Article> results = articleRepository.searchByKeyword(trimmedKeyword, pageable);
        
        log.info("Found {} articles matching keyword: {}", results.getTotalElements(), trimmedKeyword);
        return results;
    }

    /**
     * 按分类筛选文章
     * 需求: 4.1
     */
    @Override
    public Page<Article> filterByCategory(Long categoryId, Pageable pageable) {
        log.info("Filtering articles by category: {}", categoryId);
        
        if (categoryId == null) {
            log.warn("Category ID is null, returning all approved articles");
            return articleRepository.findApprovedArticles(pageable);
        }
        
        // Verify category exists
        if (!categoryRepository.existsById(categoryId)) {
            log.error("Category not found: {}", categoryId);
            throw new BusinessException("分类不存在");
        }
        
        Page<Article> results = articleRepository.findApprovedArticlesByCategory(categoryId, pageable);
        
        log.info("Found {} articles in category: {}", results.getTotalElements(), categoryId);
        return results;
    }

    /**
     * 按标签筛选文章
     * 需求: 4.2
     */
    @Override
    public Page<Article> filterByTag(Long tagId, Pageable pageable) {
        log.info("Filtering articles by tag: {}", tagId);
        
        if (tagId == null) {
            log.warn("Tag ID is null, returning all approved articles");
            return articleRepository.findApprovedArticles(pageable);
        }
        
        // Verify tag exists
        if (!tagRepository.existsById(tagId)) {
            log.error("Tag not found: {}", tagId);
            throw new BusinessException("标签不存在");
        }
        
        Page<Article> results = articleRepository.findApprovedArticlesByTag(tagId, pageable);
        
        log.info("Found {} articles with tag: {}", results.getTotalElements(), tagId);
        return results;
    }

    /**
     * 综合搜索（支持关键词、分类、标签组合）
     * 需求: 4.1-4.6
     */
    @Override
    public Page<Article> search(String keyword, Long categoryId, Long tagId, Pageable pageable) {
        log.info("Comprehensive search - keyword: {}, categoryId: {}, tagId: {}", keyword, categoryId, tagId);
        
        // Validate inputs
        if (categoryId != null && !categoryRepository.existsById(categoryId)) {
            log.error("Category not found: {}", categoryId);
            throw new BusinessException("分类不存在");
        }
        
        if (tagId != null && !tagRepository.existsById(tagId)) {
            log.error("Tag not found: {}", tagId);
            throw new BusinessException("标签不存在");
        }
        
        // Determine search strategy based on provided parameters
        Page<Article> results;
        
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasCategory = categoryId != null;
        boolean hasTag = tagId != null;
        
        if (hasKeyword && hasCategory && hasTag) {
            // All three filters
            results = articleRepository.searchByKeywordAndCategoryAndTag(keyword.trim(), categoryId, tagId, pageable);
            log.info("Search with all filters - found {} articles", results.getTotalElements());
        } else if (hasKeyword && hasCategory) {
            // Keyword + Category
            results = articleRepository.searchByKeywordAndCategory(keyword.trim(), categoryId, pageable);
            log.info("Search with keyword and category - found {} articles", results.getTotalElements());
        } else if (hasKeyword && hasTag) {
            // Keyword + Tag
            results = articleRepository.searchByKeywordAndTag(keyword.trim(), tagId, pageable);
            log.info("Search with keyword and tag - found {} articles", results.getTotalElements());
        } else if (hasCategory && hasTag) {
            // Category + Tag
            results = articleRepository.findApprovedArticlesByCategoryAndTag(categoryId, tagId, pageable);
            log.info("Search with category and tag - found {} articles", results.getTotalElements());
        } else if (hasKeyword) {
            // Keyword only
            results = searchByKeyword(keyword, pageable);
        } else if (hasCategory) {
            // Category only
            results = filterByCategory(categoryId, pageable);
        } else if (hasTag) {
            // Tag only
            results = filterByTag(tagId, pageable);
        } else {
            // No filters - return all approved articles
            results = articleRepository.findApprovedArticles(pageable);
            log.info("No filters provided - returning all approved articles");
        }
        
        return results;
    }

    /**
     * 获取排序后的文章列表
     * 需求: 29.4-29.7
     */
    @Override
    public Page<Article> getArticlesSorted(String sortType, Pageable pageable) {
        log.info("Getting articles sorted by: {}", sortType);
        
        if (sortType == null || sortType.trim().isEmpty()) {
            log.warn("Sort type is empty, using default sorting (NEWEST)");
            sortType = "NEWEST";
        }
        
        // Create pageable with appropriate sorting
        Pageable sortedPageable = createSortedPageable(sortType, pageable);
        
        Page<Article> results = articleRepository.findApprovedArticles(sortedPageable);
        
        log.info("Found {} articles sorted by {}", results.getTotalElements(), sortType);
        return results;
    }

    /**
     * 获取指定分类的排序后的文章列表
     * 需求: 29.4-29.7, 29.8
     */
    @Override
    public Page<Article> getArticlesByCategorySorted(Long categoryId, String sortType, Pageable pageable) {
        log.info("Getting articles by category {} sorted by: {}", categoryId, sortType);
        
        if (categoryId == null) {
            log.warn("Category ID is null, returning sorted articles without category filter");
            return getArticlesSorted(sortType, pageable);
        }
        
        // Verify category exists
        if (!categoryRepository.existsById(categoryId)) {
            log.error("Category not found: {}", categoryId);
            throw new BusinessException("分类不存在");
        }
        
        if (sortType == null || sortType.trim().isEmpty()) {
            log.warn("Sort type is empty, using default sorting (NEWEST)");
            sortType = "NEWEST";
        }
        
        // Create pageable with appropriate sorting
        Pageable sortedPageable = createSortedPageable(sortType, pageable);
        
        Page<Article> results = articleRepository.findApprovedArticlesByCategory(categoryId, sortedPageable);
        
        log.info("Found {} articles in category {} sorted by {}", results.getTotalElements(), categoryId, sortType);
        return results;
    }
    
    /**
     * 创建带排序的Pageable对象
     * 
     * @param sortType 排序类型
     * @param pageable 原始分页参数
     * @return 带排序的Pageable对象
     */
    private Pageable createSortedPageable(String sortType, Pageable pageable) {
        Sort sort;
        
        switch (sortType) {
            case "NEWEST":
                sort = Sort.by(Sort.Direction.DESC, "publishedAt");
                break;
                
            case "MOST_VIEWED":
                sort = Sort.by(Sort.Direction.DESC, "viewCount");
                break;
                
            case "MOST_FAVORITED":
                sort = Sort.by(Sort.Direction.DESC, "favoriteCount");
                break;
                
            case "MOST_PURCHASED":
                sort = Sort.by(Sort.Direction.DESC, "purchaseCount");
                break;
                
            default:
                log.warn("Unknown sort type: {}, using default (NEWEST)", sortType);
                sort = Sort.by(Sort.Direction.DESC, "publishedAt");
        }
        
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
