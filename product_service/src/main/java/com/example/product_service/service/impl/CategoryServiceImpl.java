package com.example.product_service.service.impl;

import com.example.product_service.dto.request.CreateCategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.dto.response.PageResponse;
import com.example.product_service.entity.Category;
import com.example.product_service.entity.Product;
import com.example.product_service.mapper.CategoryMapper;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse addCategory(CreateCategoryRequest request) {
        Category category = categoryMapper.addToEntity(request);

        Category savedCategory = categoryRepository.save(category);

        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    @Transactional
    public PageResponse<CategoryResponse> search(String keyword, Pageable pageable) {
        Page<Category> page = (keyword == null || keyword.isBlank())
                ? categoryRepository.findAll(pageable)
                :
                categoryRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable);

        Page<CategoryResponse> categoryResponsePage = page
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getDescription()
                ));

        return new PageResponse<>(
                categoryResponsePage.getContent(),
                categoryResponsePage.getNumber(),
                categoryResponsePage.getSize(),
                categoryResponsePage.getTotalElements(),
                categoryResponsePage.getTotalPages(),
                categoryResponsePage.isLast()
        );
    }
}
