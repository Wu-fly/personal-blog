package com.blog.service;

import com.blog.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 评论管理服务接口
 * 需求: 6.1-6.5, 7.1-7.4
 */
public interface CommentService {

    /**
     * 发表评论
     * 需求: 6.1
     * - 保存评论到数据库
     * - 关联到对应文章
     * - 验证评论内容不为空
     * 
     * @param comment 评论对象
     * @return 创建的评论
     */
    Comment createComment(Comment comment);

    /**
     * 回复评论
     * 需求: 6.2
     * - 保存回复评论
     * - 建立父子评论关系
     * - 验证父评论是否存在
     * 
     * @param comment 回复评论对象（包含parentId）
     * @return 创建的回复评论
     */
    Comment replyComment(Comment comment);

    /**
     * 删除评论
     * 需求: 7.3
     * - 删除评论
     * - 级联删除所有子回复
     * - 验证权限（博主或评论作者）
     * 
     * @param id 评论ID
     * @param userId 当前用户ID（用于权限验证）
     */
    void deleteComment(Long id, Long userId);

    /**
     * 查询文章的评论列表
     * 需求: 6.4
     * - 返回文章的所有评论
     * - 按时间倒序排列
     * - 支持分页
     * 
     * @param articleId 文章ID
     * @param pageable 分页参数
     * @return 评论分页列表
     */
    Page<Comment> getCommentsByArticle(Long articleId, Pageable pageable);

    /**
     * 查询评论的所有回复
     * 需求: 6.2
     * - 返回评论的所有子回复
     * - 按时间正序排列
     * 
     * @param parentId 父评论ID
     * @return 回复列表
     */
    List<Comment> getRepliesByComment(Long parentId);

    /**
     * 审核评论
     * 需求: 7.2
     * - 更新评论审核状态
     * - 验证权限（博主）
     * 
     * @param id 评论ID
     * @param userId 当前用户ID（用于权限验证）
     * @param status 审核状态
     * @return 更新后的评论
     */
    Comment approveComment(Long id, Long userId, Comment.CommentStatus status);

    /**
     * 查询待审核评论列表
     * 需求: 7.1
     * - 返回所有待审核的评论
     * - 支持分页
     * 
     * @param pageable 分页参数
     * @return 待审核评论分页列表
     */
    Page<Comment> getPendingComments(Pageable pageable);
}
