package org.ecommerce.v1.controller;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.dto.RequestDto.CreateReviewRequest;
import org.ecommerce.v1.dto.RequestDto.UpdateReviewRequest;
import org.ecommerce.v1.dto.ResponseDto.*;
import org.ecommerce.v1.entity.*;
import org.ecommerce.v1.service.*;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class GraphQLController {

    private final ProductService productService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ReviewService reviewService;
    private final SecurityService securityService;

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ProductDTO> products(
            @Argument Integer page,
            @Argument Integer size,
            @Argument Long categoryId,
            @Argument String q,
            @Argument Double minPrice,
            @Argument Double maxPrice,
            @Argument String sortBy,
            @Argument String sortDir
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String sort = sortBy != null ? sortBy : "productName";
        String direction = sortDir != null ? sortDir : "asc";
        BigDecimal min = minPrice != null ? BigDecimal.valueOf(minPrice) : null;
        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;

        Page<ProductDTO> result = productService.getProducts(pageNum, pageSize, sort, direction, categoryId, q, min, max);
        return new PagedResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ProductDetailDTO product(@Argument Long id) {
        return productService.getProductById(id);
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public PagedResponse<OrderDTO> orders(
            @Argument Integer page,
            @Argument Integer size,
            @Argument Long userId,
            @Argument String status,
            @Argument String sortBy,
            @Argument String sortDir
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String sort = sortBy != null ? sortBy : "createdAt";
        String direction = sortDir != null ? sortDir : "desc";
        OrderStatus orderStatus = status != null ? OrderStatus.valueOf(status) : null;
        if (!securityService.isAdmin()) {
            userId = securityService.getCurrentUserId();
        }

        Page<Order> result = orderService.getAllOrdersPaged(pageNum, pageSize, sort, direction, userId, orderStatus);
        List<OrderDTO> orderDTOs = result.getContent().stream()
                .map(this::convertOrderToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                orderDTOs,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOrderOwner(#id)")
    public OrderDTO order(@Argument Long id) {
        Order order = orderService.getOrderById(id);
        return convertOrderToDTO(order);
    }

    @SchemaMapping(typeName = "Order", field = "items")
    public List<OrderItemDTO> orderItems(OrderDTO order) {
        return orderItemService.getOrderItemsByOrderId(order.getId()).stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public PagedResponse<ReviewDTO> reviews(
            @Argument Integer page,
            @Argument Integer size,
            @Argument Long productId,
            @Argument Long userId,
            @Argument String sortBy,
            @Argument String sortDir
    ) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 20;
        String sort = sortBy != null ? sortBy : "createdAt";
        String direction = sortDir != null ? sortDir : "desc";

        Page<Review> result = reviewService.getAllReviewsPaged(pageNum, pageSize, sort, direction, productId, userId);
        List<ReviewDTO> reviewDTOs = result.getContent().stream()
                .map(this::convertReviewToDTO)
                .collect(Collectors.toList());

        return new PagedResponse<>(
                reviewDTOs,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public ReviewDTO review(@Argument Long id) {
        Review review = reviewService.getReviewById(id);
        return convertReviewToDTO(review);
    }


    @MutationMapping
    @PreAuthorize("isAuthenticated()")
    public ReviewDTO createReview(
            @Argument Long userId,
            @Argument Long productId,
            @Argument Integer rating,
            @Argument String comment
    ) {
        Long effectiveUserId = securityService.getCurrentUserId();
        if (!securityService.isAdmin()) {
            userId = effectiveUserId;
        }
        CreateReviewRequest request = new CreateReviewRequest(userId, productId, rating, comment);
        Review review = reviewService.createReview(request);
        return convertReviewToDTO(review);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isReviewOwner(#id)")
    public ReviewDTO updateReview(
            @Argument Long id,
            @Argument Integer rating,
            @Argument String comment
    ) {
        UpdateReviewRequest request = new UpdateReviewRequest(rating, comment);
        Review review = reviewService.updateReview(id, request);
        return convertReviewToDTO(review);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN') or @securityService.isReviewOwner(#id)")
    public boolean deleteReview(@Argument Long id) {
        reviewService.deleteReview(id);
        return true;
    }

    private OrderDTO convertOrderToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUser().getId());
        dto.setUsername(order.getUser().getUsername());
        dto.setStatus(order.getStatus().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem orderItem) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(orderItem.getId());
        dto.setOrderId(orderItem.getOrder().getId());
        dto.setProductId(orderItem.getProduct().getId());
        dto.setProductName(orderItem.getProduct().getProductName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getUnitPrice());
        return dto;
    }

    private ReviewDTO convertReviewToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setProductId(review.getProduct().getId());
        dto.setProductName(review.getProduct().getProductName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }

}
