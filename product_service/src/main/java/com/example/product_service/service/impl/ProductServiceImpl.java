package com.example.product_service.service.impl;
import com.example.product_service.dto.request.*;
import com.example.product_service.dto.response.*;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.ProductStatus;
import com.example.product_service.exception.*;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.*;
import com.example.product_service.service.ProductService;
import com.example.product_service.client.ShopClient;
import com.example.product_service.security.CurrentUser.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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
    private final ShopClient shopClient;
    @Override @Transactional
    public ProductResponse addProduct(CreateProductRequest request, Principal poster) {
        Product product = productMapper.addToEntity(request);
        product.setCategory(categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục")));
        product.setPoster(poster.username());
        assignShop(product, request.shopId(), poster);
        return productMapper.toDTO(productRepository.save(product));
    }
    /** SELLER chỉ được đăng vào gian hàng của chính mình; ADMIN được chọn mọi gian hàng. */
    private void assignShop(Product product, UUID shopId, Principal actor) {
        var shop = shopClient.getShop(shopId);
        if (!actor.isAdmin() && !actor.username().equals(shop.ownerUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ được đăng sản phẩm vào gian hàng của mình");
        product.setShopId(shop.id());
        product.setShopName(shop.name());
    }
    /** ADMIN sửa mọi sản phẩm; SELLER chỉ sửa sản phẩm do mình đăng. */
    @Transactional(readOnly = true)
    public void requireCanModify(UUID id, Principal actor) {
        if (!actor.isAdmin() && !actor.username().equals(find(id).getPoster()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ được sửa sản phẩm do mình đăng");
    }
    @Override @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getAllProduct(int page, int size, String sortBy, Sort.Direction direction) {
        return search(null, null, null, null, null, PageRequest.of(page, size, Sort.by(direction, sortBy)));
    }
    @Override @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Pageable pageable) {
        return search(keyword, null, null, null, null, pageable);
    }
    @Override @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, UUID categoryId, ProductStatus status, Pageable pageable) {
        return search(keyword, categoryId, status, null, null, pageable);
    }
    @Override @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, UUID categoryId, ProductStatus status, String poster, UUID shopId, Pageable pageable) {
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
            if (poster != null && !poster.isBlank()) filters.add(cb.equal(root.get("poster"), poster.trim()));
            if (shopId != null) filters.add(cb.equal(root.get("shopId"), shopId));
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
    public ProductResponse editProduct(UpdateProductRequest request, UUID id, Principal actor) {
        Product product = find(id);
        if (!actor.isAdmin() && !actor.username().equals(product.getPoster()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ được sửa sản phẩm do mình đăng");
        if (product.getStatus() == ProductStatus.ARCHIVED) throw new InvalidProductStateException("Sản phẩm đã được lưu trữ");
        var category = categoryRepository.findById(request.categoryId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));
        productMapper.updateToEntity(request, product);
        product.setCategory(category);
        if (request.shopId() != null && !request.shopId().equals(product.getShopId())) assignShop(product, request.shopId(), actor);
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
