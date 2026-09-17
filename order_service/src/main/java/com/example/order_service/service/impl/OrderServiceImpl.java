package com.example.order_service.service.impl;

import com.example.order_service.client.ProductCatalogClient;
import com.example.order_service.client.InventoryClient;
import com.example.order_service.client.dto.InventoryReservationResponse;
import com.example.order_service.client.dto.ProductCatalogResponse;
import com.example.order_service.dto.request.CreateOrderItemRequest;
import com.example.order_service.dto.request.CreateOrderRequest;
import com.example.order_service.dto.response.OrderResponse;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import com.example.order_service.enums.OrderStatus;
import com.example.order_service.exception.InvalidOrderStateException;
import com.example.order_service.exception.OrderItemAlreadyExistsException;
import com.example.order_service.exception.ResourceNotFoundException;
import com.example.order_service.mapper.OrderItemMapper;
import com.example.order_service.mapper.OrderMapper;
import com.example.order_service.messaging.InventoryReservedEvent;
import com.example.order_service.messaging.OrderEventPublisher;
import com.example.order_service.repository.OrderRepository;
import com.example.order_service.repository.OrderItemRepository;
import com.example.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.util.Optional;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductCatalogClient productCatalogClient;
    private final InventoryClient inventoryClient;
    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;
    private final OrderEventPublisher orderEventPublisher;

    @Override
    public OrderResponse createOrder(
            String idempotencyKey,
            CreateOrderRequest request
    ) {
        String requestHash = requestHash(request);
        Optional<Order> existing =
                orderRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            if (!existing.get().getRequestHash().equals(requestHash)) {
                throw new com.example.order_service.exception.IdempotencyKeyConflictException(
                        "Idempotency-Key đã được dùng cho một request khác"
                );
            }
            if (existing.get().getStatus() == OrderStatus.PENDING) {
                reserveInventory(existing.get().getId());
            }
            return getOrderById(existing.get().getId());
        }

        Set<UUID> productIds = new HashSet<>();
        java.util.Map<UUID, ProductCatalogResponse> products = new java.util.HashMap<>();
        for (CreateOrderItemRequest itemRequest : request.items()) {
            if (!productIds.add(itemRequest.productId())) {
                throw new OrderItemAlreadyExistsException(
                        "Một sản phẩm chỉ được xuất hiện một lần trong order"
                );
            }
            ProductCatalogResponse product = verifiedProduct(itemRequest.productId(), request.currency());
            products.put(itemRequest.productId(), product);
        }

        Order savedOrder = transactionTemplate().execute(status -> {
            lockIdempotencyKey(idempotencyKey);
            Optional<Order> raced = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (raced.isPresent()) {
                if (!raced.get().getRequestHash().equals(requestHash)) {
                    throw new com.example.order_service.exception.IdempotencyKeyConflictException(
                            "Idempotency-Key đã được dùng cho một request khác"
                    );
                }
                return raced.get();
            }

            Order order = orderMapper.addToEntity(request, idempotencyKey, requestHash);
            for (CreateOrderItemRequest itemRequest : request.items()) {
                order.addItem(orderItemMapper.toEntity(
                        itemRequest,
                        products.get(itemRequest.productId())
                ));
            }
            return orderRepository.saveAndFlush(order);
        });

        if (savedOrder == null) {
            throw new IllegalStateException("Không thể tạo order");
        }
        reserveInventory(savedOrder.getId());
        return getOrderById(savedOrder.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID id) {
        Order order = orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order: " + id));
        return orderMapper.toDTO(order);
    }

    @Override
    @Transactional
    public OrderResponse addItem(
            UUID orderId,
            CreateOrderItemRequest request
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy order: " + orderId
                        )
                );

        ensureOrderIsEditable(order);

        boolean alreadyExists = order.getItems()
                .stream()
                .anyMatch(item ->
                        item.getProductId().equals(request.productId())
                );

        if (alreadyExists) {
            throw new OrderItemAlreadyExistsException(
                    "Sản phẩm đã tồn tại trong order"
            );
        }

        ProductCatalogResponse product = verifiedProduct(request.productId(), order.getCurrency());
        OrderItem item = orderItemMapper.toEntity(request, product);

        order.addItem(item);

        return orderMapper.toDTO(order);
    }

    private void ensureOrderIsEditable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException(
                    "Chỉ được thay đổi item khi order đang PENDING"
            );
        }
    }

    @Override
    @Transactional
    public OrderResponse removeItem(
            UUID orderId,
            UUID orderItemId
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy order: " + orderId
                        )
                );

        ensureOrderIsEditable(order);

        if (order.getItems().size() == 1) {
            throw new InvalidOrderStateException("Order phải có ít nhất một item");
        }

        OrderItem item = order.getItems()
                .stream()
                .filter(existing ->
                        existing.getId().equals(orderItemId)
                )
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy item "
                                        + orderItemId
                                        + " trong order "
                                        + orderId
                        )
                );

        order.removeItem(item);

        return orderMapper.toDTO(order);
    }

    private ProductCatalogResponse verifiedProduct(UUID productId, String orderCurrency) {
        ProductCatalogResponse product = productCatalogClient.getProduct(productId);
        if (!product.active()) {
            throw new InvalidOrderStateException("Sản phẩm " + productId + " không còn được bán");
        }
        if (!product.currency().equalsIgnoreCase(orderCurrency.trim())) {
            throw new InvalidOrderStateException("Các sản phẩm trong order phải cùng currency");
        }
        return product;
    }

    private String requestHash(CreateOrderRequest request) {
        String canonicalItems = request.items().stream()
                .sorted(java.util.Comparator.comparing(item -> item.productId().toString()))
                .map(item -> item.productId() + ":" + item.quantity())
                .collect(java.util.stream.Collectors.joining(";"));
        String canonical = request.userId() + "|"
                + request.currency().trim().toUpperCase(Locale.ROOT) + "|" + canonicalItems;
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                            .digest(canonical.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 không khả dụng", exception);
        }
    }

    private void reserveInventory(UUID orderId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order: " + orderId));
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        try {
            for (OrderItem item : order.getItems()) {
                if (item.getInventoryReservationId() == null) {
                    InventoryReservationResponse reservation = inventoryClient.reserve(
                            orderId, item.getId(), item.getSku(), item.getQuantity()
                    );
                    transactionTemplate().executeWithoutResult(status -> {
                        OrderItem managedItem = orderItemRepository.findById(item.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Không tìm thấy order item: " + item.getId()
                                ));
                        managedItem.setInventoryReservationId(reservation.id());
                    });
                }
            }

            transactionTemplate().executeWithoutResult(status -> {
                Order managedOrder = orderRepository.findById(orderId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order: " + orderId));
                managedOrder.markInventoryReserved();
            });

            Order reservedOrder = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy order: " + orderId
                    ));

            orderEventPublisher.publishInventoryReserved(
                    new InventoryReservedEvent(
                            reservedOrder.getId(),
                            reservedOrder.getUserId(),
                            reservedOrder.getTotalAmount(),
                            reservedOrder.getCurrency(),
                            reservedOrder.getPaymentMethod()
                    )
            );
        } catch (RuntimeException exception) {
            compensateReservations(orderId);
            transactionTemplate().executeWithoutResult(status -> {
                Order managedOrder = orderRepository.findById(orderId)
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy order: " + orderId));
                managedOrder.fail("Không thể giữ đủ tồn kho: " + exception.getMessage());
            });
            throw exception;
        }
    }

    private void compensateReservations(UUID orderId) {
        Order current = orderRepository.findWithItemsById(orderId).orElse(null);
        if (current == null) {
            return;
        }
        for (OrderItem item : current.getItems()) {
            if (item.getInventoryReservationId() != null) {
                try {
                    inventoryClient.transition(item.getInventoryReservationId(), "RELEASED");
                } catch (RuntimeException ignored) {
                    // Reservation expiration remains the final safety net for failed compensation.
                }
            }
        }
    }

    private TransactionTemplate transactionTemplate() {
        return new TransactionTemplate(transactionManager);
    }

    private void lockIdempotencyKey(String key) {
        entityManager.unwrap(Session.class).doWork(connection -> {
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "select pg_advisory_xact_lock(hashtextextended(?, 0))"
            )) {
                statement.setString(1, key);
                statement.execute();
            }
        });
    }

}
