package com.blog.service;

import com.blog.entity.SensitiveWord;

import java.util.List;

/**
 * 内容安全服务接口
 * 需求: 25.1-25.7, 11.1-11.5
 */
public interface SecurityService {

    /**
     * 检测内容是否包含敏感词
     * 需求: 25.1-25.2
     * 
     * @param content 待检测内容
     * @return true表示包含敏感词，false表示不包含
     */
    boolean containsSensitiveWord(String content);

    /**
     * 检测并返回内容中的敏感词列表
     * 需求: 25.1-25.2
     * 
     * @param content 待检测内容
     * @return 检测到的敏感词列表
     */
    List<String> findSensitiveWords(String content);

    /**
     * 检测SQL注入攻击特征
     * 需求: 11.2
     * 
     * @param input 用户输入
     * @return true表示检测到SQL注入特征，false表示安全
     */
    boolean containsSqlInjection(String input);

    /**
     * 检测XSS攻击特征
     * 需求: 11.3
     * 
     * @param input 用户输入
     * @return true表示检测到XSS攻击特征，false表示安全
     */
    boolean containsXss(String input);

    /**
     * 对输入进行XSS转义处理
     * 需求: 11.3
     * 
     * @param input 用户输入
     * @return 转义后的安全内容
     */
    String escapeXss(String input);

    /**
     * 添加敏感词
     * 需求: 25.7
     * 
     * @param word 敏感词
     * @return 添加的敏感词实体
     */
    SensitiveWord addSensitiveWord(String word);

    /**
     * 删除敏感词
     * 需求: 25.7
     * 
     * @param word 敏感词
     */
    void deleteSensitiveWord(String word);

    /**
     * 获取所有敏感词
     * 需求: 25.7
     * 
     * @return 敏感词列表
     */
    List<SensitiveWord> getAllSensitiveWords();

    /**
     * 批量导入敏感词
     * 需求: 25.7
     * 
     * @param words 敏感词列表
     */
    void importSensitiveWords(List<String> words);

    /**
     * 重新加载敏感词库（用于更新AC自动机）
     * 需求: 25.7
     */
    void reloadSensitiveWords();
}
