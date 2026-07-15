package com.blog.property;

import com.blog.dto.ArticleRequest;
import com.blog.dto.CommentRequest;
import com.blog.dto.MessageRequest;
import net.jqwik.api.*;
import net.jqwik.api.constraints.LongRange;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 输入验证属性测试
 * 使用jqwik进行基于属性的测试
 * 
 * Feature: personal-blog-system, Property 7: 输入验证拒绝空值
 * 验证需求: 2.5, 6.3, 24.6
 */
class ValidationPropertyTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Feature: personal-blog-system, Property 7: 输入验证拒绝空值 - 文章标题
     * 验证需求: 2.5
     * 
     * 对于任意空标题，系统应该拒绝保存并返回验证错误
     */
    @Property(tries = 100)
    void testArticleTitleCannotBeEmpty(
            @ForAll("emptyOrBlankStrings") String emptyTitle,
            @ForAll("validContents") String content) {
        
        // 创建文章请求对象，标题为空
        ArticleRequest request = new ArticleRequest();
        request.setTitle(emptyTitle);
        request.setContent(content);
        request.setIsPaid(false);
        
        // 执行验证
        Set<ConstraintViolation<ArticleRequest>> violations = validator.validate(request);
        
        // 属性验证：空标题应该产生验证错误
        assertFalse(violations.isEmpty(), 
            "Empty or blank article title should produce validation errors");
        
        // 验证错误消息包含标题相关的提示
        boolean hasTitleError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title") 
                && v.getMessage().contains("不能为空"));
        
        assertTrue(hasTitleError, 
            "Validation error should indicate that title cannot be empty");
    }

    /**
     * Feature: personal-blog-system, Property 7: 输入验证拒绝空值 - 文章内容
     * 验证需求: 2.5
     * 
     * 对于任意空内容，系统应该拒绝保存并返回验证错误
     */
    @Property(tries = 100)
    void testArticleContentCannotBeEmpty(
            @ForAll("validTitles") String title,
            @ForAll("emptyOrBlankStrings") String emptyContent) {
        
        // 创建文章请求对象，内容为空
        ArticleRequest request = new ArticleRequest();
        request.setTitle(title);
        request.setContent(emptyContent);
        request.setIsPaid(false);
        
        // 执行验证
        Set<ConstraintViolation<ArticleRequest>> violations = validator.validate(request);
        
        // 属性验证：空内容应该产生验证错误
        assertFalse(violations.isEmpty(), 
            "Empty or blank article content should produce validation errors");
        
        // 验证错误消息包含内容相关的提示
        boolean hasContentError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content") 
                && v.getMessage().contains("不能为空"));
        
        assertTrue(hasContentError, 
            "Validation error should indicate that content cannot be empty");
    }

    /**
     * Feature: personal-blog-system, Property 7: 输入验证拒绝空值 - 文章标题和内容同时为空
     * 验证需求: 2.5
     * 
     * 对于任意标题和内容都为空的情况，系统应该拒绝保存并返回多个验证错误
     */
    @Property(tries = 100)
    void testArticleTitleAndContentCannotBothBeEmpty(
            @ForAll("emptyOrBlankStrings") String emptyTitle,
            @ForAll("emptyOrBlankStrings") String emptyContent) {
        
        // 创建文章请求对象，标题和内容都为空
        ArticleRequest request = new ArticleRequest();
        request.setTitle(emptyTitle);
        request.setContent(emptyContent);
        request.setIsPaid(false);
        
        // 执行验证
        Set<ConstraintViolation<ArticleRequest>> violations = validator.validate(request);
        
        // 属性验证：标题和内容都为空应该产生至少2个验证错误
        assertTrue(violations.size() >= 2, 
            "Empty title and content should produce at least 2 validation errors, got " + violations.size());
        
        // 验证包含标题和内容的错误
        long titleErrors = violations.stream()
            .filter(v -> v.getPropertyPath().toString().equals("title"))
            .count();
        long contentErrors = violations.stream()
            .filter(v -> v.getPropertyPath().toString().equals("content"))
            .count();
        
        assertTrue(titleErrors >= 1, "Should have at least 1 title validation error");
        assertTrue(contentErrors >= 1, "Should have at least 1 content validation error");
    }

    /**
     * Feature: personal-blog-system, Property 7: 输入验证拒绝空值 - 评论内容
     * 验证需求: 6.3
     * 
     * 对于任意空评论内容，系统应该拒绝提交并返回错误信息
     */
    @Property(tries = 100)
    void testCommentContentCannotBeEmpty(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll("emptyOrBlankStrings") String emptyContent) {
        
        // 创建评论请求对象，内容为空
        CommentRequest request = new CommentRequest();
        request.setArticleId(articleId);
        request.setContent(emptyContent);
        
        // 执行验证
        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);
        
        // 属性验证：空评论内容应该产生验证错误
        assertFalse(violations.isEmpty(), 
            "Empty or blank comment content should produce validation errors");
        
        // 验证错误消息包含内容相关的提示
        boolean hasContentError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content") 
                && v.getMessage().contains("不能为空"));
        
        assertTrue(hasContentError, 
            "Validation error should indicate that comment content cannot be empty");
    }

    /**
     * Feature: personal-blog-system, Property 7: 输入验证拒绝空值 - 评论回复内容
     * 验证需求: 6.3
     * 
     * 对于任意空回复内容，系统应该拒绝提交并返回错误信息
     */
    @Property(tries = 100)
    void testCommentReplyContentCannotBeEmpty(
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long parentId,
            @ForAll("emptyOrBlankStrings") String emptyContent) {
        
        // 创建评论回复请求对象，内容为空
        CommentRequest request = new CommentRequest();
        request.setArticleId(articleId);
        request.setParentId(parentId);
        request.setContent(emptyContent);
        
        // 执行验证
        Set<ConstraintViolation<CommentRequest>> violations = validator.validate(request);
        
        // 属性验证：空回复内容应该产生验证错误
        assertFalse(violations.isEmpty(), 
            "Empty or blank reply content should produce validation errors");
        
        // 验证错误消息包含内容相关的提示
        boolean hasContentError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content") 
                && v.getMessage().contains("不能为空"));
        
        assertTrue(hasContentError, 
            "Validation error should indicate that reply content cannot be empty");
    }

    /**
     * Feature: personal-blog-system, Property 7: 输入验证拒绝空值 - 私信内容
     * 验证需求: 24.6
     * 
     * 对于任意空私信内容，系统应该拒绝发送并提示错误信息
     */
    @Property(tries = 100)
    void testMessageContentCannotBeEmpty(
            @ForAll @LongRange(min = 1, max = 1000000) Long receiverId,
            @ForAll("emptyOrBlankStrings") String emptyContent) {
        
        // 创建私信请求对象，内容为空
        MessageRequest request = new MessageRequest();
        request.setReceiverId(receiverId);
        request.setContent(emptyContent);
        
        // 执行验证
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(request);
        
        // 属性验证：空私信内容应该产生验证错误
        assertFalse(violations.isEmpty(), 
            "Empty or blank message content should produce validation errors");
        
        // 验证错误消息包含内容相关的提示
        boolean hasContentError = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content") 
                && v.getMessage().contains("不能为空"));
        
        assertTrue(hasContentError, 
            "Validation error should indicate that message content cannot be empty");
    }

    /**
     * Feature: personal-blog-system, Property 7: 输入验证拒绝空值 - 有效输入应该通过验证
     * 验证需求: 2.5, 6.3, 24.6
     * 
     * 对于任意有效的非空输入，验证应该通过
     */
    @Property(tries = 100)
    void testValidInputPassesValidation(
            @ForAll("validTitles") String title,
            @ForAll("validContents") String content,
            @ForAll @LongRange(min = 1, max = 1000000) Long articleId,
            @ForAll @LongRange(min = 1, max = 1000000) Long receiverId) {
        
        // 测试有效的文章请求
        ArticleRequest articleRequest = new ArticleRequest();
        articleRequest.setTitle(title);
        articleRequest.setContent(content);
        articleRequest.setIsPaid(false);
        
        Set<ConstraintViolation<ArticleRequest>> articleViolations = validator.validate(articleRequest);
        
        // 属性验证：有效的文章输入不应该产生标题或内容相关的验证错误
        boolean hasArticleTitleOrContentError = articleViolations.stream()
            .anyMatch(v -> {
                String path = v.getPropertyPath().toString();
                return (path.equals("title") || path.equals("content")) 
                    && v.getMessage().contains("不能为空");
            });
        
        assertFalse(hasArticleTitleOrContentError, 
            "Valid article input should not produce title or content validation errors");
        
        // 测试有效的评论请求
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setArticleId(articleId);
        commentRequest.setContent(content);
        
        Set<ConstraintViolation<CommentRequest>> commentViolations = validator.validate(commentRequest);
        
        // 属性验证：有效的评论输入不应该产生内容相关的验证错误
        boolean hasCommentContentError = commentViolations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content") 
                && v.getMessage().contains("不能为空"));
        
        assertFalse(hasCommentContentError, 
            "Valid comment input should not produce content validation errors");
        
        // 测试有效的私信请求
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setReceiverId(receiverId);
        messageRequest.setContent(content);
        
        Set<ConstraintViolation<MessageRequest>> messageViolations = validator.validate(messageRequest);
        
        // 属性验证：有效的私信输入不应该产生内容相关的验证错误
        boolean hasMessageContentError = messageViolations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content") 
                && v.getMessage().contains("不能为空"));
        
        assertFalse(hasMessageContentError, 
            "Valid message input should not produce content validation errors");
    }

    /**
     * Feature: personal-blog-system, Property 7: 输入验证拒绝空值 - 边界情况测试
     * 验证需求: 2.5, 6.3, 24.6
     * 
     * 测试各种边界情况的空值（null, "", "   "）
     */
    @Property(tries = 100)
    void testVariousEmptyValuesBoundaries(
            @ForAll("emptyVariants") String emptyVariant) {
        
        // 测试文章标题
        ArticleRequest articleRequest = new ArticleRequest();
        articleRequest.setTitle(emptyVariant);
        articleRequest.setContent("Valid content");
        articleRequest.setIsPaid(false);
        
        Set<ConstraintViolation<ArticleRequest>> articleViolations = validator.validate(articleRequest);
        
        boolean hasArticleTitleError = articleViolations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("title"));
        
        assertTrue(hasArticleTitleError, 
            String.format("Article title '%s' should produce validation error", 
                emptyVariant == null ? "null" : "'" + emptyVariant + "'"));
        
        // 测试评论内容
        CommentRequest commentRequest = new CommentRequest();
        commentRequest.setArticleId(1L);
        commentRequest.setContent(emptyVariant);
        
        Set<ConstraintViolation<CommentRequest>> commentViolations = validator.validate(commentRequest);
        
        boolean hasCommentContentError = commentViolations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content"));
        
        assertTrue(hasCommentContentError, 
            String.format("Comment content '%s' should produce validation error", 
                emptyVariant == null ? "null" : "'" + emptyVariant + "'"));
        
        // 测试私信内容
        MessageRequest messageRequest = new MessageRequest();
        messageRequest.setReceiverId(1L);
        messageRequest.setContent(emptyVariant);
        
        Set<ConstraintViolation<MessageRequest>> messageViolations = validator.validate(messageRequest);
        
        boolean hasMessageContentError = messageViolations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("content"));
        
        assertTrue(hasMessageContentError, 
            String.format("Message content '%s' should produce validation error", 
                emptyVariant == null ? "null" : "'" + emptyVariant + "'"));
    }

    /**
     * 生成空或空白字符串（null, "", "   ", "\t", "\n"等）
     */
    @Provide
    Arbitrary<String> emptyOrBlankStrings() {
        return Arbitraries.of(
            null,
            "",
            " ",
            "  ",
            "   ",
            "\t",
            "\n",
            "\r",
            " \t ",
            " \n ",
            "    \t\n    "
        );
    }

    /**
     * 生成各种空值变体用于边界测试
     */
    @Provide
    Arbitrary<String> emptyVariants() {
        return Arbitraries.of(
            null,
            "",
            " ",
            "  ",
            "   ",
            "    ",
            "\t",
            "\n",
            "\r",
            "\t\t",
            "\n\n",
            " \t ",
            " \n ",
            "\t\n",
            "  \t\n  "
        );
    }

    /**
     * 生成有效的文章标题（非空，1-200字符）
     */
    @Provide
    Arbitrary<String> validTitles() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars("中文测试标题")
                .ofMinLength(1)
                .ofMaxLength(200)
                .filter(s -> s != null && !s.trim().isEmpty());
    }

    /**
     * 生成有效的内容（非空，1-10000字符）
     */
    @Provide
    Arbitrary<String> validContents() {
        return Arbitraries.strings()
                .alpha()
                .numeric()
                .withChars("中文测试内容。\n")
                .ofMinLength(1)
                .ofMaxLength(10000)
                .filter(s -> s != null && !s.trim().isEmpty());
    }
}
