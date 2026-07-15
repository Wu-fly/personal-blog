package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.entity.BrowseHistory;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.BrowseHistoryRepository;
import com.blog.repository.UserRepository;
import com.blog.service.BrowseHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 浏览历史管理服务实现
 * 需求: 22.1-22.5
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrowseHistoryServiceImpl implements BrowseHistoryService {

    private final BrowseHistoryRepository browseHistoryRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    /**
     * 记录浏览历史
     * 需求: 22.1, 22.2
     * - 如果记录不存在，创建新记录
     * - 如果记录已存在，更新时间戳
     * 权限: 需求 38.2 - 需要登录才能记录浏览历史
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public BrowseHistory recordBrowseHistory(Long userId, Long articleId) {
        log.info("Recording browse history for user {} and article {}", userId, articleId);
        
        // 验证用户是否存在
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 验证文章是否存在
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new BusinessException("ARTICLE_NOT_FOUND", "文章不存在"));
        
        // 查找是否已存在浏览记录
        Optional<BrowseHistory> existingHistory = browseHistoryRepository.findByUserIdAndArticleId(userId, articleId);
        
        BrowseHistory browseHistory;
        if (existingHistory.isPresent()) {
            // 如果记录已存在，更新时间戳 (需求: 22.2)
            browseHistory = existingHistory.get();
            // updatedAt 会自动更新（BaseEntity 的 @PreUpdate）
            browseHistory = browseHistoryRepository.save(browseHistory);
            log.info("Updated browse history timestamp for user {} and article {}", userId, articleId);
        } else {
            // 如果记录不存在，创建新记录 (需求: 22.1)
            browseHistory = new BrowseHistory();
            browseHistory.setUserId(userId);
            browseHistory.setArticleId(articleId);
            browseHistory = browseHistoryRepository.save(browseHistory);
            log.info("Created new browse history for user {} and article {}", userId, articleId);
            
            // 检查是否超过100条记录限制 (需求: 22.5)
            long count = browseHistoryRepository.countByUserId(userId);
            if (count > 100) {
                // 删除最旧的记录
                Page<BrowseHistory> oldestRecords = browseHistoryRepository.findByUserIdOrderByUpdatedAtDesc(
                    userId, 
                    PageRequest.of((int) count - 1, 1)
                );
                if (!oldestRecords.isEmpty()) {
                    BrowseHistory oldestRecord = oldestRecords.getContent().get(0);
                    browseHistoryRepository.delete(oldestRecord);
                    log.info("Deleted oldest browse history record for user {} to maintain 100 records limit", userId);
                }
            }
        }
        
        return browseHistory;
    }

    /**
     * 查询浏览历史
     * 需求: 22.3, 22.4, 22.5
     * - 按时间倒序排列
     * - 支持分页
     * - 最多保留100条记录
     * 权限: 需求 38.2 - 需要登录才能查看浏览历史
     */
    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public Page<BrowseHistory> getBrowseHistory(Long userId, Pageable pageable) {
        log.info("Getting browse history for user: {}", userId);
        
        // 验证用户是否存在
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 查询浏览历史，按时间倒序排列 (需求: 22.3)
        Page<BrowseHistory> history = browseHistoryRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable);
        
        log.info("Found {} browse history records for user: {}", history.getTotalElements(), userId);
        return history;
    }

    /**
     * 删除浏览历史
     * 需求: 21.8
     * - 删除指定的浏览历史记录
     * 权限: 需求 38.2 - 需要登录才能删除浏览历史
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void deleteBrowseHistory(Long userId, Long articleId) {
        log.info("Deleting browse history for user {} and article {}", userId, articleId);
        
        // 验证用户是否存在
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 验证浏览历史是否存在
        BrowseHistory history = browseHistoryRepository.findByUserIdAndArticleId(userId, articleId)
            .orElseThrow(() -> new BusinessException("HISTORY_NOT_FOUND", "浏览历史不存在"));
        
        // 删除浏览历史记录 (需求: 21.8)
        browseHistoryRepository.deleteByUserIdAndArticleId(userId, articleId);
        
        log.info("Deleted browse history for user {} and article {} successfully", userId, articleId);
    }

    /**
     * 删除用户的所有浏览历史
     * 需求: 21.8
     * - 删除用户的所有浏览历史记录
     * 权限: 需求 38.2 - 需要登录才能删除浏览历史
     */
    @Override
    @Transactional
    @PreAuthorize("isAuthenticated()")
    public void deleteAllBrowseHistory(Long userId) {
        log.info("Deleting all browse history for user: {}", userId);
        
        // 验证用户是否存在
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
        
        // 删除用户的所有浏览历史记录 (需求: 21.8)
        browseHistoryRepository.deleteByUserId(userId);
        
        log.info("Deleted all browse history for user {} successfully", userId);
    }
}
