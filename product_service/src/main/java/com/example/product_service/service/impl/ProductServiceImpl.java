package com.example.product_service.service.impl;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.response.*;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.ProductStatus;
import com.example.product_service.exception.*;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.*;
import com.example.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    @Override @Transactional
    public ProductResponse addProduct(CreateProductRequest request) {
        Product product = productMapper.addToEntity(request);
        product.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục")));
        return productMapper.toDTO(productRepository.save(product));
    }
    @Override @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProduct(int page, int size, String sortBy, Sort.Direction direction) {
        return search(null, null, null, PageRequest.of(page, size, Sort.by(direction, sortBy)));
    }
    @Override @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Pageable pageable) {
        return search(keyword, null, null, pageable);
    }
    @Override @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, UUID categoryId, ProductStatus status, Pageable pageable) {
        Set<String> allowed = Set.of("name", "sku", "price", "createdAt", "updatedAt");
        for (Sort.Order order : pageable.getSort())
            if (!allowed.contains(order.getProperty())) throw new IllegalArgumentException("Trường sắp xếp không hợp lệ");
        Specification<Product> specification = (root, query, cb) -> {
            var filters = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (keyword != null && !keyword.isBlank()) {
                String term = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                filters.add(cb.or(cb.like(cb.lower(root.get("name")), term), cb.like(cb.lower(root.get("sku")), term)));
            }
            if (categoryId != null) filters.add(cb.equal(root.get("category").get("id"), categoryId));
            if (status != null) filters.add(cb.equal(root.get("status"), status));
            return cb.and(filters.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        var page = productRepository.findAll(specification, pageable).map(productMapper::toDTO);
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isLast());
    }
    @Override @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) { return productMapper.toDTO(find(id)); }
    @Override @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        return productRepository.findBySku(sku.trim().toUpperCase(Locale.ROOT)).map(productMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }
    @Override @Transactional
    public ProductResponse editProduct(UpdateProductRequest request, UUID id) {
        Product product = find(id);
        if (product.getStatus() == ProductStatus.ARCHIVED) throw new InvalidProductStateException("Sản phẩm đã được lưu trữ");
        var category = categoryRepository.findById(request.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        productMapper.updateToEntity(request, product);
        product.setCategory(category);
        return productMapper.toDTO(product);
    }
    @Transactional
    public ProductResponse setImage(UUID id, String url, String publicId) {
        Product product = find(id);
        product.setImageUrl(url);
        product.setImagePublicId(publicId);
        return productMapper.toDTO(product);
    }
    @Transactional(readOnly = true)
    public Map<String, Object> stats() {
        return Map.of("total", productRepository.count(), "active", productRepository.countByStatus(ProductStatus.ACTIVE), "categories", categoryRepository.count());
    }
    private Product find(UUID id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));
    }
}
