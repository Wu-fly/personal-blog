package com.blog.service;

import com.blog.entity.Article;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CommentRepository;
import com.blog.repository.UserRepository;
import com.blog.service.impl.ArticleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ArticleService单元测试
 */
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ArticleServiceImpl articleService;

    private User blogger;
    private Article article;

    @BeforeEach
    void setUp() {
        // 创建测试博主
        blogger = new User();
        blogger.setId(1L);
        blogger.setPhone("13800138000");
        blogger.setEmail("blogger@example.com");
        blogger.setNickname("测试博主");
        blogger.setRole(User.UserRole.BLOGGER);
        blogger.setStatus(User.UserStatus.ACTIVE);

        // 创建测试文章
        article = new Article();
        article.setId(1L);
        article.setUserId(1L);
        article.setTitle("测试文章");
        article.setContent("这是测试文章内容");
        article.setSummary("测试摘要");
        article.setCategoryId(1L);
        article.setIsPaid(false);
        article.setPrice(BigDecimal.ZERO);
        article.setReviewStatus(Article.ReviewStatus.PENDING);
    }

    @Test
    void testCreateArticle_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(blogger));
        when(articleRepository.save(any(Article.class))).thenReturn(article);

        // When
        Article result = articleService.createArticle(article);

        // Then
        assertNotNull(result);
        assertEquals(Article.ReviewStatus.PENDING, result.getReviewStatus());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void testCreateArticle_EmptyTitle_ThrowsException() {
        // Given
        article.setTitle("");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.createArticle(article);
        });
        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("文章标题不能为空", exception.getMessage());
    }

    @Test
    void testCreateArticle_EmptyContent_ThrowsException() {
        // Given
        article.setContent("");

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.createArticle(article);
        });
        assertEquals("INVALID_INPUT", exception.getErrorCode());
        assertEquals("文章内容不能为空", exception.getMessage());
    }

    @Test
    void testCreateArticle_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.createArticle(article);
        });
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void testCreateArticle_NotBlogger_ThrowsException() {
        // Given
        blogger.setRole(User.UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(blogger));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.createArticle(article);
        });
        assertEquals("PERMISSION_DENIED", exception.getErrorCode());
    }

    @Test
    void testCreateArticle_PaidArticleWithInvalidPrice_ThrowsException() {
        // Given
        article.setIsPaid(true);
        article.setPrice(BigDecimal.ZERO);
        when(userRepository.findById(1L)).thenReturn(Optional.of(blogger));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.createArticle(article);
        });
        assertEquals("INVALID_PRICE", exception.getErrorCode());
    }

    @Test
    void testCreateArticle_DefaultCoverImage() {
        // Given
        article.setCoverImage(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(blogger));
        when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> {
            Article saved = invocation.getArgument(0);
            assertEquals("/default-cover.jpg", saved.getCoverImage());
            return saved;
        });

        // When
        articleService.createArticle(article);

        // Then
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void testUpdateArticle_Success() {
        // Given
        Article updatedArticle = new Article();
        updatedArticle.setUserId(1L);
        updatedArticle.setTitle("更新后的标题");
        updatedArticle.setContent("更新后的内容");
        updatedArticle.setIsPaid(false);

        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.save(any(Article.class))).thenReturn(article);

        // When
        Article result = articleService.updateArticle(1L, updatedArticle);

        // Then
        assertNotNull(result);
        assertEquals(Article.ReviewStatus.PENDING, result.getReviewStatus());
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void testUpdateArticle_NotFound_ThrowsException() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.updateArticle(1L, article);
        });
        assertEquals("ARTICLE_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void testUpdateArticle_NotOwner_ThrowsException() {
        // Given
        Article updatedArticle = new Article();
        updatedArticle.setUserId(2L); // Different user
        updatedArticle.setTitle("更新后的标题");
        updatedArticle.setContent("更新后的内容");

        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.updateArticle(1L, updatedArticle);
        });
        assertEquals("PERMISSION_DENIED", exception.getErrorCode());
    }

    @Test
    void testDeleteArticle_Success() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When
        articleService.deleteArticle(1L, 1L);

        // Then
        verify(articleRepository).delete(article);
    }

    @Test
    void testDeleteArticle_NotFound_ThrowsException() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.deleteArticle(1L, 1L);
        });
        assertEquals("ARTICLE_NOT_FOUND", exception.getErrorCode());
    }

    @Test
    void testDeleteArticle_NotOwner_ThrowsException() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.deleteArticle(1L, 2L); // Different user
        });
        assertEquals("PERMISSION_DENIED", exception.getErrorCode());
    }

    @Test
    void testGetArticles_WithKeyword() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> page = new PageImpl<>(articles, pageable, 1);
        when(articleRepository.searchByKeyword(eq("测试"), any(Pageable.class))).thenReturn(page);

        // When
        Page<Article> result = articleService.getArticles(null, null, "测试", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(articleRepository).searchByKeyword(eq("测试"), any(Pageable.class));
    }

    @Test
    void testGetArticles_WithCategory() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> page = new PageImpl<>(articles, pageable, 1);
        when(articleRepository.findApprovedArticlesByCategory(eq(1L), any(Pageable.class))).thenReturn(page);

        // When
        Page<Article> result = articleService.getArticles(1L, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(articleRepository).findApprovedArticlesByCategory(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetArticles_WithUser() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Article> articles = Arrays.asList(article);
        Page<Article> page = new PageImpl<>(articles, pageable, 1);
        when(articleRepository.findApprovedArticlesByUser(eq(1L), any(Pageable.class))).thenReturn(page);

        // When
        Page<Article> result = articleService.getArticles(null, 1L, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(articleRepository).findApprovedArticlesByUser(eq(1L), any(Pageable.class));
    }

    @Test
    void testGetArticleDetail_Success() {
        // Given
        article.setReviewStatus(Article.ReviewStatus.APPROVED);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When
        Article result = articleService.getArticleDetail(1L, 1L);

        // Then
        assertNotNull(result);
        verify(articleRepository).incrementViewCount(1L);
    }

    @Test
    void testGetArticleDetail_NotApproved_Owner_Success() {
        // Given
        article.setReviewStatus(Article.ReviewStatus.PENDING);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When
        Article result = articleService.getArticleDetail(1L, 1L);

        // Then
        assertNotNull(result);
        verify(articleRepository).incrementViewCount(1L);
    }

    @Test
    void testGetArticleDetail_NotApproved_NotOwner_ThrowsException() {
        // Given
        article.setReviewStatus(Article.ReviewStatus.PENDING);
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setRole(User.UserRole.USER);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.getArticleDetail(1L, 2L);
        });
        assertEquals("ARTICLE_NOT_APPROVED", exception.getErrorCode());
    }

    @Test
    void testPinArticle_Success() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.findPinnedArticlesByUser(1L)).thenReturn(Arrays.asList());
        when(articleRepository.save(any(Article.class))).thenReturn(article);

        // When
        Article result = articleService.pinArticle(1L, 1L, true);

        // Then
        assertNotNull(result);
        verify(articleRepository).save(any(Article.class));
    }

    @Test
    void testPinArticle_ExceedLimit_ThrowsException() {
        // Given
        List<Article> pinnedArticles = Arrays.asList(
            new Article(), new Article(), new Article(), new Article(), new Article()
        );
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));
        when(articleRepository.findPinnedArticlesByUser(1L)).thenReturn(pinnedArticles);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.pinArticle(1L, 1L, true);
        });
        assertEquals("PIN_LIMIT_EXCEEDED", exception.getErrorCode());
    }

    @Test
    void testPinArticle_NotOwner_ThrowsException() {
        // Given
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            articleService.pinArticle(1L, 2L, true);
        });
        assertEquals("PERMISSION_DENIED", exception.getErrorCode());
    }

    @Test
    void testGetPinnedArticles() {
        // Given
        List<Article> pinnedArticles = Arrays.asList(article);
        when(articleRepository.findPinnedArticlesByUser(1L)).thenReturn(pinnedArticles);

        // When
        List<Article> result = articleService.getPinnedArticles(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(articleRepository).findPinnedArticlesByUser(1L);
    }
}
