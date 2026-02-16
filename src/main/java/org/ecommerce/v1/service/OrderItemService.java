package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.entity.Order;
import org.ecommerce.v1.entity.OrderItem;
import org.ecommerce.v1.entity.Product;
import org.ecommerce.v1.repository.OrderItemRepository;
import org.ecommerce.v1.repository.OrderRepository;
import org.ecommerce.v1.repository.ProductRepository;
import org.ecommerce.v1.utils.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItem createOrderItem(Long orderId, Long productId, Long quantity, BigDecimal unitPrice) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with ID " + orderId + " not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product with ID " + productId + " not found"));

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(unitPrice);

        return orderItemRepository.save(orderItem);
    }

    @Transactional(readOnly = true)
    public OrderItem getOrderItemById(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item with ID " + orderItemId + " not found"));
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByProductId(Long productId) {
        return orderItemRepository.findByProductId(productId);
    }

    @Transactional(readOnly = true)
    public Page<OrderItem> getAllOrderItemsPaged(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return orderItemRepository.findAll(pageable);
    }

    public OrderItem updateOrderItem(Long orderItemId, Long quantity, BigDecimal unitPrice) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item with ID " + orderItemId + " not found"));

        if (quantity != null) {
            orderItem.setQuantity(quantity);
        }
        if (unitPrice != null) {
            orderItem.setUnitPrice(unitPrice);
        }

        return orderItem;
    }

    public void deleteOrderItem(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item with ID " + orderItemId + " not found"));
        orderItemRepository.delete(orderItem);
    }

    public void deleteOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        orderItemRepository.deleteAll(orderItems);
    }
}
