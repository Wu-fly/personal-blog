package com.blog.service;

import com.blog.entity.BrowseHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 浏览历史管理服务接口
 * 需求: 22.1-22.5
 */
public interface BrowseHistoryService {

    /**
     * 记录浏览历史
     * 需求: 22.1, 22.2
     * - 如果记录不存在，创建新记录
     * - 如果记录已存在，更新时间戳
     * 
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 保存后的浏览历史对象
     */
    BrowseHistory recordBrowseHistory(Long userId, Long articleId);

    /**
     * 查询浏览历史
     * 需求: 22.3, 22.4, 22.5
     * - 按时间倒序排列
     * - 支持分页
     * - 最多保留100条记录
     * 
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 浏览历史分页列表
     */
    Page<BrowseHistory> getBrowseHistory(Long userId, Pageable pageable);

    /**
     * 删除浏览历史
     * 需求: 21.8
     * - 删除指定的浏览历史记录
     * 
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    void deleteBrowseHistory(Long userId, Long articleId);

    /**
     * 删除用户的所有浏览历史
     * 需求: 21.8
     * - 删除用户的所有浏览历史记录
     * 
     * @param userId 用户ID
     */
    void deleteAllBrowseHistory(Long userId);
}
