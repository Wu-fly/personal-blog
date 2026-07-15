package com.blog.service;

import com.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 文章搜索服务接口
 * 需求: 4.1-4.6
 */
public interface SearchService {

    /**
     * 关键词搜索文章
     * 需求: 4.3
     * 
     * @param keyword 搜索关键词
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    Page<Article> searchByKeyword(String keyword, Pageable pageable);

    /**
     * 按分类筛选文章
     * 需求: 4.1
     * 
     * @param categoryId 分类ID
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    Page<Article> filterByCategory(Long categoryId, Pageable pageable);

    /**
     * 按标签筛选文章
     * 需求: 4.2
     * 
     * @param tagId 标签ID
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    Page<Article> filterByTag(Long tagId, Pageable pageable);

    /**
     * 综合搜索（支持关键词、分类、标签组合）
     * 需求: 4.1-4.6
     * 
     * @param keyword 搜索关键词（可选）
     * @param categoryId 分类ID（可选）
     * @param tagId 标签ID（可选）
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    Page<Article> search(String keyword, Long categoryId, Long tagId, Pageable pageable);

    /**
     * 获取排序后的文章列表
     * 需求: 29.4-29.7
     * 
     * @param sortType 排序类型（NEWEST, MOST_VIEWED, MOST_FAVORITED, MOST_PURCHASED）
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    Page<Article> getArticlesSorted(String sortType, Pageable pageable);

    /**
     * 获取指定分类的排序后的文章列表
     * 需求: 29.4-29.7, 29.8
     * 
     * @param categoryId 分类ID
     * @param sortType 排序类型（NEWEST, MOST_VIEWED, MOST_FAVORITED, MOST_PURCHASED）
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    Page<Article> getArticlesByCategorySorted(Long categoryId, String sortType, Pageable pageable);
}
