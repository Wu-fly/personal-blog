package com.blog.service;

import com.blog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 文章管理服务接口
 * 需求: 2.1-2.8, 3.1-3.4, 27.1-27.6
 */
public interface ArticleService {

    /**
     * 创建文章
     * 需求: 2.1
     * 
     * @param article 文章对象
     * @return 创建的文章
     */
    Article createArticle(Article article);

    /**
     * 更新文章
     * 需求: 2.2
     * 
     * @param id 文章ID
     * @param article 更新的文章信息
     * @return 更新后的文章
     */
    Article updateArticle(Long id, Article article);

    /**
     * 删除文章
     * 需求: 2.3
     * 
     * @param id 文章ID
     * @param userId 用户ID（用于权限验证）
     */
    void deleteArticle(Long id, Long userId);

    /**
     * 查询文章列表（支持筛选、排序、分页）
     * 需求: 2.4, 3.1, 29.1-29.9
     * 
     * @param categoryId 分类ID（可选）
     * @param userId 用户ID（可选，用于查询特定用户的文章）
     * @param keyword 搜索关键词（可选）
     * @param pageable 分页和排序参数
     * @return 文章分页列表
     */
    Page<Article> getArticles(Long categoryId, Long userId, String keyword, Pageable pageable);

    /**
     * 查询文章详情（增加浏览计数）
     * 需求: 3.2, 3.3
     * 
     * @param id 文章ID
     * @param userId 当前用户ID（可选，用于权限验证）
     * @param forEdit 是否用于编辑（如果是作者编辑，不截断付费内容）
     * @return 文章详情
     */
    Article getArticleDetail(Long id, Long userId, Boolean forEdit);

    /**
     * 置顶文章
     * 需求: 27.1-27.6
     * 
     * @param id 文章ID
     * @param userId 用户ID（用于权限验证）
     * @param isPinned 是否置顶
     * @return 更新后的文章
     */
    Article pinArticle(Long id, Long userId, boolean isPinned);

    /**
     * 查询用户的置顶文章
     * 需求: 27.3
     * 
     * @param userId 用户ID
     * @return 置顶文章列表
     */
    java.util.List<Article> getPinnedArticles(Long userId);

    /**
     * 获取用户的所有文章（包括所有审核状态）
     * 用于个人中心"我的文章"页面
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    Page<Article> getUserArticles(Long userId, Pageable pageable);

    /**
     * 获取轮播图配置列表
     * 
     * @return 轮播图配置列表（按显示顺序排序）
     */
    java.util.List<com.blog.entity.CarouselConfig> getCarouselConfigs();

    /**
     * 增加文章浏览计数
     * 需求: 3.3
     * 
     * @param articleId 文章ID
     */
    void incrementViewCount(Long articleId);

    /**
     * 保存文章标签关联
     * 
     * @param articleId 文章ID
     * @param tagIds 标签ID列表
     */
    void saveArticleTags(Long articleId, java.util.List<Long> tagIds);

    /**
     * 获取文章的标签列表
     * 
     * @param articleId 文章ID
     * @return 标签列表
     */
    java.util.List<com.blog.entity.Tag> getArticleTags(Long articleId);

    /**
     * 根据标签ID获取文章列表
     * 
     * @param tagId 标签ID
     * @param pageable 分页参数
     * @return 文章分页列表
     */
    Page<Article> getArticlesByTag(Long tagId, Pageable pageable);

    /**
     * 根据标签名称获取或创建标签，返回标签ID列表
     * 
     * @param tagNames 标签名称列表
     * @return 标签ID列表
     */
    java.util.List<Long> getOrCreateTagIds(java.util.List<String> tagNames);
}
