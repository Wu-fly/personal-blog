package com.blog.service.impl;

import com.blog.entity.SensitiveWord;
import com.blog.exception.BusinessException;
import com.blog.repository.SensitiveWordRepository;
import com.blog.service.SecurityService;
import com.blog.util.AhoCorasick;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 内容安全服务实现
 * 需求: 25.1-25.7, 11.1-11.5
 */
@Slf4j
@Service
public class SecurityServiceImpl implements SecurityService {

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    // AC自动机实例
    private AhoCorasick ahoCorasick;

    // SQL注入检测正则表达式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|EXECUTE|UNION|DECLARE|CAST|CONVERT)\\b)" +
        "|(--)|(/\\*)|" +
        "(;\\s*(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER))" +
        "|(\\bOR\\b\\s+\\d+\\s*=\\s*\\d+)" +
        "|(\\bAND\\b\\s+\\d+\\s*=\\s*\\d+)" +
        "|(\\bOR\\b\\s+'[^']*'\\s*=\\s*'[^']*')" +
        "|(\\bAND\\b\\s+'[^']*'\\s*=\\s*'[^']*')" +
        "|(\\bOR\\b\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*[a-zA-Z_][a-zA-Z0-9_]*)" +
        "|(\\bAND\\b\\s+[a-zA-Z_][a-zA-Z0-9_]*\\s*=\\s*[a-zA-Z_][a-zA-Z0-9_]*)" +
        "|(\\bOR\\b\\s+\\d+\\s*>\\s*\\d+)" +
        "|(\\bAND\\b\\s+\\d+\\s*>\\s*\\d+)" +
        "|(\\bOR\\b\\s+\\d+\\s*<\\s*\\d+)" +
        "|(\\bAND\\b\\s+\\d+\\s*<\\s*\\d+)" +
        "|(\\bOR\\b\\s+1\\s*=\\s*1)" +
        "|(\\bAND\\b\\s+1\\s*=\\s*1)" +
        "|(\\bOR\\b\\s+'1'\\s*=\\s*'1')" +
        "|(\\bAND\\b\\s+'1'\\s*=\\s*'1')" +
        "|(\\bOR\\b\\s+true)" +
        "|(\\bAND\\b\\s+true)" +
        "|(\\bOR\\b\\s+false)" +
        "|(\\bAND\\b\\s+false)" +
        "|('+\\s*(OR|AND)\\s+)" +
        "|(\\bOR\\b\\s+['\"])" +
        "|(\\bAND\\b\\s+['\"])"
    );

    // XSS攻击检测正则表达式
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "(?i)(<script[^>]*>.*?</script>)" +
        "|(<iframe[^>]*>.*?</iframe>)" +
        "|(<object[^>]*>.*?</object>)" +
        "|(<embed[^>]*>)" +
        "|(<applet[^>]*>.*?</applet>)" +
        "|(javascript:)" +
        "|(on\\w+\\s*=)" +
        "|(<img[^>]*\\s+onerror\\s*=)" +
        "|(<img[^>]*\\s+onload\\s*=)" +
        "|(<body[^>]*\\s+onload\\s*=)" +
        "|(<svg[^>]*\\s+onload\\s*=)" +
        "|(eval\\s*\\()" +
        "|(expression\\s*\\()" +
        "|(vbscript:)" +
        "|(data:text/html)"
    );

    /**
     * 初始化敏感词库
     * 在服务启动时加载所有敏感词到AC自动机
     */
    @PostConstruct
    public void init() {
        reloadSensitiveWords();
        log.info("敏感词库初始化完成");
    }

    @Override
    public boolean containsSensitiveWord(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return ahoCorasick.contains(content);
    }

    @Override
    public List<String> findSensitiveWords(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }
        return ahoCorasick.search(content);
    }

    @Override
    public boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        boolean detected = SQL_INJECTION_PATTERN.matcher(input).find();
        
        if (detected) {
            log.warn("检测到SQL注入尝试: {}", input);
        }
        
        return detected;
    }

    @Override
    public boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        
        boolean detected = XSS_PATTERN.matcher(input).find();
        
        if (detected) {
            log.warn("检测到XSS攻击尝试: {}", input);
        }
        
        return detected;
    }

    @Override
    public String escapeXss(String input) {
        if (input == null) {
            return null;
        }
        // 使用Spring的HtmlUtils进行HTML转义
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * 添加敏感词
     * 需求: 25.7
     * 权限: 只有管理员可以添加敏感词
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public SensitiveWord addSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw new BusinessException("敏感词不能为空");
        }

        String trimmedWord = word.trim();

        // 检查是否已存在
        if (sensitiveWordRepository.existsByWord(trimmedWord)) {
            throw new BusinessException("敏感词已存在: " + trimmedWord);
        }

        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(trimmedWord);
        
        SensitiveWord saved = sensitiveWordRepository.save(sensitiveWord);
        
        // 重新加载敏感词库
        reloadSensitiveWords();
        
        log.info("添加敏感词: {}", trimmedWord);
        
        return saved;
    }

    /**
     * 删除敏感词
     * 需求: 25.7
     * 权限: 只有管理员可以删除敏感词
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSensitiveWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw new BusinessException("敏感词不能为空");
        }

        String trimmedWord = word.trim();

        if (!sensitiveWordRepository.existsByWord(trimmedWord)) {
            throw new BusinessException("敏感词不存在: " + trimmedWord);
        }

        sensitiveWordRepository.deleteByWord(trimmedWord);
        
        // 重新加载敏感词库
        reloadSensitiveWords();
        
        log.info("删除敏感词: {}", trimmedWord);
    }

    @Override
    public List<SensitiveWord> getAllSensitiveWords() {
        return sensitiveWordRepository.findAll();
    }

    /**
     * 批量导入敏感词
     * 需求: 25.7
     * 权限: 只有管理员可以批量导入敏感词
     */
    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void importSensitiveWords(List<String> words) {
        if (words == null || words.isEmpty()) {
            return;
        }

        int addedCount = 0;
        int skippedCount = 0;

        for (String word : words) {
            if (word == null || word.trim().isEmpty()) {
                continue;
            }

            String trimmedWord = word.trim();

            // 跳过已存在的敏感词
            if (sensitiveWordRepository.existsByWord(trimmedWord)) {
                skippedCount++;
                continue;
            }

            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(trimmedWord);
            sensitiveWordRepository.save(sensitiveWord);
            addedCount++;
        }

        // 重新加载敏感词库
        reloadSensitiveWords();

        log.info("批量导入敏感词完成: 新增{}个, 跳过{}个", addedCount, skippedCount);
    }

    @Override
    public synchronized void reloadSensitiveWords() {
        // 创建新的AC自动机实例
        AhoCorasick newAhoCorasick = new AhoCorasick();

        // 加载所有敏感词
        List<String> words = sensitiveWordRepository.findAllWords();
        
        for (String word : words) {
            newAhoCorasick.addPattern(word);
        }

        // 构建失败指针
        newAhoCorasick.build();

        // 替换旧的AC自动机实例
        this.ahoCorasick = newAhoCorasick;

        log.info("敏感词库重新加载完成，共加载{}个敏感词", words.size());
    }
}
