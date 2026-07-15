package com.blog.controller;

import com.blog.dto.ApiResponse;
import com.blog.dto.CategoryWithCountResponse;
import com.blog.entity.Article;
import com.blog.entity.Category;
import com.blog.repository.ArticleRepository;
import com.blog.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类控制�?
 * 提供分类列表查询功能
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final ArticleRepository articleRepository;

    /**
     * 获取所有分类列表（包含文章数量�?
     * GET /api/categories
     *
     * @return 分类列表（包含每个分类的文章数量�?
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryWithCountResponse>>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        
        // 为每个分类统计审核通过的文章数�?
        List<CategoryWithCountResponse> categoriesWithCount = categories.stream()
            .map(category -> {
                long count = articleRepository.countByCategoryIdAndReviewStatus(
                    category.getId(), 
                    Article.ReviewStatus.APPROVED
                );
                return new CategoryWithCountResponse(
                    category.getId(),
                    category.getName(),
                    category.getDescription(),
                    count
                );
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(categoriesWithCount));
    }
}

