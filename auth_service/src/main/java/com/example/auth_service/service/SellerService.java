package com.example.auth_service.service;

import com.example.auth_service.entity.InfoSeller;
import com.example.auth_service.entity.Roles;
import com.example.auth_service.entity.User;
import com.example.auth_service.repository.InfoSellerRepository;
import com.example.auth_service.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {
    private final InfoSellerRepository infoSellers;
    private final UserRepository users;

    public record ApplyRequest(
            @NotBlank @Size(max = 255) String name,
            @NotBlank @Size(max = 500) String address,
            @NotBlank @Pattern(regexp = "^\\+?[0-9 .-]{8,20}$", message = "Số điện thoại không hợp lệ") String phone,
            @NotBlank @Email @Size(max = 255) String email
    ) {}

    public record RejectRequest(@Size(max = 500) String reason) {}

    public record SellerInfo(UUID id, UUID userId, String username, String name, String address, String phone,
                             String email, InfoSeller.Status status, String rejectReason, String reviewedBy,
                             Instant reviewedAt, Instant createdAt, Instant updatedAt) {
        static SellerInfo of(InfoSeller s) {
            return new SellerInfo(s.getId(), s.getUser().getId(), s.getUser().getUsername(), s.getName(), s.getAddress(),
                    s.getPhone(), s.getEmail(), s.getStatus(), s.getRejectReason(), s.getReviewedBy(), s.getReviewedAt(),
                    s.getCreatedAt(), s.getUpdatedAt());
        }
    }

    public record UserSummary(UUID id, String username, String role, SellerInfo seller) {}

    /** User gửi (hoặc gửi lại sau khi bị từ chối) yêu cầu trở thành Seller. */
    @Transactional
    public SellerInfo apply(UUID userId, ApplyRequest request) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tài khoản không tồn tại"));
        if (!Roles.USER.equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Chỉ tài khoản USER mới cần ứng tuyển Seller");
        }
        InfoSeller info = infoSellers.findByUserId(userId).orElse(null);
        if (info != null && info.getStatus() == InfoSeller.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bạn đã có yêu cầu đang chờ duyệt");
        }
        if (info == null) info = InfoSeller.builder().user(user).build();
        info.setName(request.name().trim());
        info.setAddress(request.address().trim());
        info.setPhone(request.phone().trim());
        info.setEmail(request.email().trim());
        info.setStatus(InfoSeller.Status.PENDING);
        info.setRejectReason(null);
        info.setReviewedBy(null);
        info.setReviewedAt(null);
        return SellerInfo.of(infoSellers.saveAndFlush(info));
    }

    @Transactional(readOnly = true)
    public Optional<SellerInfo> mine(UUID userId) {
        return infoSellers.findByUserId(userId).map(SellerInfo::of);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> applications(InfoSeller.Status status, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        var result = (status == null ? infoSellers.findAll(pageable) : infoSellers.findByStatus(status, pageable))
                .map(SellerInfo::of);
        return pageMap(result.getContent(), page, size, result.getTotalElements(), result.getTotalPages(), result.isLast(),
                Map.of("pending", infoSellers.countByStatus(InfoSeller.Status.PENDING)));
    }

    /** Duyệt: gán thông tin người bán cho user và nâng role USER -> SELLER. */
    @Transactional
    public SellerInfo approve(UUID applicationId, String reviewer) {
        InfoSeller info = pending(applicationId);
        info.setStatus(InfoSeller.Status.APPROVED);
        info.setReviewedBy(reviewer);
        info.setReviewedAt(Instant.now());
        info.setRejectReason(null);
        info.getUser().setRole(Roles.SELLER);
        return SellerInfo.of(info);
    }

    @Transactional
    public SellerInfo reject(UUID applicationId, String reviewer, String reason) {
        InfoSeller info = pending(applicationId);
        info.setStatus(InfoSeller.Status.REJECTED);
        info.setReviewedBy(reviewer);
        info.setReviewedAt(Instant.now());
        info.setRejectReason(reason == null || reason.isBlank() ? null : reason.trim());
        return SellerInfo.of(info);
    }

    /** Danh sách người dùng cho trang quản trị, lọc theo vai trò (vd. SELLER) và từ khóa. */
    @Transactional(readOnly = true)
    public Map<String, Object> listUsers(String role, String keyword, int page, int size) {
        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> filters = new ArrayList<>();
            if (role != null && !role.isBlank()) filters.add(cb.equal(root.get("role"), role.trim().toUpperCase(Locale.ROOT)));
            if (keyword != null && !keyword.isBlank())
                filters.add(cb.like(cb.lower(root.get("username")), "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%"));
            return cb.and(filters.toArray(Predicate[]::new));
        };
        var result = users.findAll(spec, PageRequest.of(page, size, Sort.by("username")));
        Map<UUID, SellerInfo> sellers = infoSellers.findByUserIdIn(result.map(User::getId).getContent()).stream()
                .map(SellerInfo::of).collect(Collectors.toMap(SellerInfo::userId, Function.identity()));
        var items = result.getContent().stream()
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getRole(), sellers.get(u.getId()))).toList();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String r : List.of(Roles.USER, Roles.SELLER, Roles.ADMIN)) counts.put(r, users.countByRole(r));
        return pageMap(items, page, size, result.getTotalElements(), result.getTotalPages(), result.isLast(),
                Map.of("roleCounts", counts));
    }

    private InfoSeller pending(UUID applicationId) {
        InfoSeller info = infoSellers.findWithUserById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy yêu cầu"));
        if (info.getStatus() != InfoSeller.Status.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Yêu cầu này đã được xử lý");
        }
        return info;
    }

    private static Map<String, Object> pageMap(List<?> items, int page, int size, long total, int pages, boolean last,
                                               Map<String, Object> extra) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("items", items);
        body.put("page", page);
        body.put("size", size);
        body.put("totalItems", total);
        body.put("totalPages", pages);
        body.put("isLast", last);
        body.putAll(extra);
        return body;
    }
}
