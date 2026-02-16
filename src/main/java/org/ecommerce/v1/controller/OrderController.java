package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.ecommerce.v1.dto.JsonResponseDto.SuccessResponse;
import org.ecommerce.v1.dto.RequestDto.CreateOrderRequest;
import org.ecommerce.v1.dto.RequestDto.UpdateOrderRequest;
import org.ecommerce.v1.dto.ResponseDto.OrderDTO;
import org.ecommerce.v1.dto.ResponseDto.OrderItemDTO;
import org.ecommerce.v1.dto.ResponseDto.PagedResponse;
import org.ecommerce.v1.entity.Order;
import org.ecommerce.v1.entity.OrderItem;
import org.ecommerce.v1.entity.OrderStatus;
import org.ecommerce.v1.service.OrderItemService;
import org.ecommerce.v1.service.OrderService;
import org.ecommerce.v1.service.SecurityService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@Tag(name = "Orders", description = "Order create, read, update, delete")
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final SecurityService securityService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER', 'STAFF')")
    public ResponseEntity<SuccessResponse<OrderDTO>> createOrder(@RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request.getUserId());
        SuccessResponse<OrderDTO> res = new SuccessResponse<>("Order created successfully", convertToDTO(order));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or @securityService.isOrderOwner(#id)")
    public ResponseEntity<SuccessResponse<OrderDTO>> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        SuccessResponse<OrderDTO> res = new SuccessResponse<>("Order retrieved successfully", convertToDTO(order));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<SuccessResponse<PagedResponse<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus status,
            Authentication authentication
    ) {
        Long effectiveUserId = securityService.getCurrentUserId();
        boolean adminOrStaff = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_STAFF"));
        if (!adminOrStaff && effectiveUserId != null) {
            userId = effectiveUserId;
        }
        Page<Order> orders = orderService.getAllOrdersPaged(page, size, sortBy, sortDir, userId, status);
        List<OrderDTO> orderDTOs = orders.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PagedResponse<OrderDTO> pagedResponse = new PagedResponse<>(
                orderDTOs,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages()
        );

        SuccessResponse<PagedResponse<OrderDTO>> res = new SuccessResponse<>("Orders fetched successfully", pagedResponse);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ResponseEntity<SuccessResponse<OrderDTO>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderRequest request
    ) {
        Order updated = orderService.updateOrderStatus(id, request.getStatus());
        SuccessResponse<OrderDTO> res = new SuccessResponse<>("Order status updated successfully", convertToDTO(updated));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<String>> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        SuccessResponse<String> res = new SuccessResponse<>("Order deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemDTO> items = orderItemService.getOrderItemsByOrderId(order.getId()).stream()
                .map(this::convertItemToDTO)
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }

    private OrderItemDTO convertItemToDTO(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setOrderId(item.getOrder().getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getProductName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        return dto;
    }
}
