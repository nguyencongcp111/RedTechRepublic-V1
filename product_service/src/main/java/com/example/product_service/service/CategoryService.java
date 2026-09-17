package com.example.product_service.service;

import com.example.product_service.dto.request.CreateCategoryRequest;
import com.example.product_service.dto.response.CategoryResponse;
import com.example.product_service.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface CategoryService {
    CategoryResponse addCategory(CreateCategoryRequest request);

    PageResponse<CategoryResponse> search(String keyword, Pageable pageable);
}
