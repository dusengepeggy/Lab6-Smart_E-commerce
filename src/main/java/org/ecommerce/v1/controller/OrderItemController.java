package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.ecommerce.v1.dto.JsonResponseDto.SuccessResponse;
import org.ecommerce.v1.dto.RequestDto.CreateOrderItemRequest;
import org.ecommerce.v1.dto.RequestDto.UpdateOrderItemRequest;
import org.ecommerce.v1.dto.ResponseDto.OrderItemDTO;
import org.ecommerce.v1.dto.ResponseDto.PagedResponse;
import org.ecommerce.v1.entity.OrderItem;
import org.ecommerce.v1.service.OrderItemService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/order-items")
@AllArgsConstructor
@Tag(name = "Order Items", description = "Items within an order")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<SuccessResponse<OrderItemDTO>> createOrderItem(@RequestBody CreateOrderItemRequest request) {
        OrderItem orderItem = orderItemService.createOrderItem(
                request.getOrderId(),
                request.getProductId(),
                request.getQuantity()
        );
        SuccessResponse<OrderItemDTO> res = new SuccessResponse<>("Order item created successfully", convertToDTO(orderItem));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<OrderItemDTO>> getOrderItemById(@PathVariable Long id) {
        OrderItem item = orderItemService.getOrderItemById(id);
        SuccessResponse<OrderItemDTO> res = new SuccessResponse<>("Order item retrieved successfully", convertToDTO(item));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<SuccessResponse<List<OrderItemDTO>>> getOrderItemsByOrderId(@PathVariable Long orderId) {
        List<OrderItem> items = orderItemService.getOrderItemsByOrderId(orderId);
        List<OrderItemDTO> itemDTOs = items.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        SuccessResponse<List<OrderItemDTO>> res = new SuccessResponse<>("Order items fetched successfully", itemDTOs);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<PagedResponse<OrderItemDTO>>> getAllOrderItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Page<OrderItem> orderItems = orderItemService.getAllOrderItemsPaged(page, size, sortBy, sortDir);
        List<OrderItemDTO> itemDTOs = orderItems.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PagedResponse<OrderItemDTO> pagedResponse = new PagedResponse<>(
                itemDTOs,
                orderItems.getNumber(),
                orderItems.getSize(),
                orderItems.getTotalElements(),
                orderItems.getTotalPages()
        );

        SuccessResponse<PagedResponse<OrderItemDTO>> res = new SuccessResponse<>("Order items fetched successfully", pagedResponse);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<OrderItemDTO>> updateOrderItem(
            @PathVariable Long id,
            @RequestBody UpdateOrderItemRequest request
    ) {
        OrderItem updated = orderItemService.updateOrderItem(id, request.getQuantity());
        SuccessResponse<OrderItemDTO> res = new SuccessResponse<>("Order item updated successfully", convertToDTO(updated));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<String>> deleteOrderItem(@PathVariable Long id) {
        orderItemService.deleteOrderItem(id);
        SuccessResponse<String> res = new SuccessResponse<>("Order item deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping("/order/{orderId}")
    public ResponseEntity<SuccessResponse<String>> deleteOrderItemsByOrderId(@PathVariable Long orderId) {
        orderItemService.deleteOrderItemsByOrderId(orderId);
        SuccessResponse<String> res = new SuccessResponse<>("Order items deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    private OrderItemDTO convertToDTO(OrderItem item) {
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
