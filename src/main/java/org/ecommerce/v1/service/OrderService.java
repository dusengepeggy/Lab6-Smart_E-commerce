package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.entity.Order;
import org.ecommerce.v1.entity.OrderStatus;
import org.ecommerce.v1.entity.User;
import org.ecommerce.v1.repository.OrderRepository;
import org.ecommerce.v1.repository.UserRepository;
import org.ecommerce.v1.utils.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public Order createOrder(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.Pending);
        order.setTotalAmount(BigDecimal.ZERO);

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with ID " + orderId + " not found"));
    }

    @Transactional(readOnly = true)
    public Page<Order> getAllOrdersPaged(
            int page,
            int size,
            String sortBy,
            String direction,
            Long userIdFilter,
            OrderStatus statusFilter
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        if (userIdFilter != null && statusFilter != null) {
            return orderRepository.findByUserIdAndStatus(userIdFilter, statusFilter, pageable);
        } else if (userIdFilter != null) {
            return orderRepository.findByUserId(userIdFilter, pageable);
        } else if (statusFilter != null) {
            return orderRepository.findByStatus(statusFilter, pageable);
        }

        return orderRepository.findAll(pageable);
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with ID " + orderId + " not found"));

        order.setStatus(status);
        return order;
    }

    public Order updateOrderTotalAmount(Long orderId, BigDecimal totalAmount) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with ID " + orderId + " not found"));

        order.setTotalAmount(totalAmount);
        return order;
    }

    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with ID " + orderId + " not found"));
        orderRepository.delete(order);
    }
}
