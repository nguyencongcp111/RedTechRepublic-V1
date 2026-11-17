package com.example.inventory_service.controller;

import com.example.inventory_service.entity.Shop;
import com.example.inventory_service.exception.ResourceNotFoundException;
import com.example.inventory_service.repository.ShopRepository;
import com.example.inventory_service.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Validated
public class ShopController {
    private final ShopRepository shops;
    private final CurrentUser currentUser;

    public record ShopRequest(
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Size(max = 500) String address,
            @NotBlank @Pattern(regexp = "^\\+?[0-9 .-]{8,20}$", message = "Số điện thoại không hợp lệ") String phone,
            @NotBlank @Email @Size(max = 255) String email
    ) {}

    public record ShopResponse(UUID id, String name, String address, String phone, String email,
                               String ownerUsername, boolean system, Instant createdAt, Instant updatedAt) {
        static ShopResponse of(Shop s) {
            return new ShopResponse(s.getId(), s.getName(), s.getAddress(), s.getPhone(), s.getEmail(),
                    s.getOwnerUsername(), Shop.SYSTEM_SHOP_ID.equals(s.getId()), s.getCreatedAt(), s.getUpdatedAt());
        }
    }

    /** Danh sách công khai (để hiển thị gian hàng); lọc theo từ khóa tên / người tạo. */
    @GetMapping
    @Transactional(readOnly = true)
    public Map<String, Object> list(@RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "0") @Min(0) int page,
                                    @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name"));
        String term = keyword == null ? "" : keyword.trim();
        var result = term.isEmpty() ? shops.findAll(pageable)
                : shops.findByNameContainingIgnoreCaseOrOwnerUsernameContainingIgnoreCase(term, term, pageable);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", result.map(ShopResponse::of).getContent());
        body.put("page", page);
        body.put("size", size);
        body.put("totalItems", result.getTotalElements());
        body.put("totalPages", result.getTotalPages());
        body.put("isLast", result.isLast());
        return body;
    }

    /** Gian hàng của người đang đăng nhập. */
    @GetMapping("/mine")
    @Transactional(readOnly = true)
    public List<ShopResponse> mine(HttpServletRequest http) {
        var principal = currentUser.require(http);
        return shops.findByOwnerUsernameOrderByCreatedAtAsc(principal.username()).stream().map(ShopResponse::of).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ShopResponse get(@PathVariable UUID id) {
        return ShopResponse.of(find(id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ShopResponse> create(@Valid @RequestBody ShopRequest request, HttpServletRequest http) {
        var principal = currentUser.requirePublisher(http);
        Shop shop = Shop.builder().ownerUsername(principal.username()).build();
        apply(shop, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ShopResponse.of(shops.saveAndFlush(shop)));
    }

    @PutMapping("/{id}")
    @Transactional
    public ShopResponse update(@PathVariable UUID id, @Valid @RequestBody ShopRequest request, HttpServletRequest http) {
        var principal = currentUser.requirePublisher(http);
        Shop shop = find(id);
        if (!principal.isAdmin() && !principal.username().equals(shop.getOwnerUsername())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn chỉ được sửa gian hàng của mình");
        }
        apply(shop, request);
        return ShopResponse.of(shops.saveAndFlush(shop));
    }

    private void apply(Shop shop, ShopRequest request) {
        shop.setName(request.name().trim());
        shop.setAddress(request.address().trim());
        shop.setPhone(request.phone().trim());
        shop.setEmail(request.email().trim());
    }

    private Shop find(UUID id) {
        return shops.findById(id).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy gian hàng"));
    }
}
