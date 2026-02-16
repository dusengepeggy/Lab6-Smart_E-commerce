package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.entity.Inventory;
import org.ecommerce.v1.entity.Order;
import org.ecommerce.v1.entity.OrderItem;
import org.ecommerce.v1.entity.Product;
import org.ecommerce.v1.repository.InventoryRepository;
import org.ecommerce.v1.repository.OrderItemRepository;
import org.ecommerce.v1.repository.OrderRepository;
import org.ecommerce.v1.repository.ProductRepository;
import org.ecommerce.v1.utils.exceptions.InsufficientStockException;
import org.ecommerce.v1.utils.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
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
    private final InventoryRepository inventoryRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderItem createOrderItem(Long orderId, Long productId, Long quantity) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order with ID " + orderId + " not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product with ID " + productId + " not found"));

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Inventory for product ID " + productId + " not found"));

        if (inventory.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '" + product.getProductName() + 
                    "'. Available: " + inventory.getStockQuantity() + ", Requested: " + quantity);
        }

        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(product.getPrice());

        OrderItem savedItem = orderItemRepository.save(orderItem);
        recalculateOrderTotal(order);

        return savedItem;
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

    @Transactional(isolation = Isolation.REPEATABLE_READ, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public OrderItem updateOrderItem(Long orderItemId, Long newQuantity) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item with ID " + orderItemId + " not found"));

        if (newQuantity == null || newQuantity.equals(orderItem.getQuantity())) {
            return orderItem;
        }

        Inventory inventory = inventoryRepository.findByProductId(orderItem.getProduct().getId())
                .orElseThrow(() -> new NotFoundException("Inventory for product not found"));

        Long quantityDifference = newQuantity - orderItem.getQuantity();

        if (quantityDifference > 0 && inventory.getStockQuantity() < quantityDifference) {
            throw new InsufficientStockException(
                    "Insufficient stock for product '" + orderItem.getProduct().getProductName() +
                    "'. Available: " + inventory.getStockQuantity() + ", Additional needed: " + quantityDifference);
        }

        inventory.setStockQuantity(inventory.getStockQuantity() - quantityDifference);
        orderItem.setQuantity(newQuantity);
        recalculateOrderTotal(orderItem.getOrder());

        return orderItem;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteOrderItem(Long orderItemId) {
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new NotFoundException("Order item with ID " + orderItemId + " not found"));

        Inventory inventory = inventoryRepository.findByProductId(orderItem.getProduct().getId())
                .orElse(null);
        
        if (inventory != null) {
            inventory.setStockQuantity(inventory.getStockQuantity() + orderItem.getQuantity());
        }

        Order order = orderItem.getOrder();
        orderItemRepository.delete(orderItem);
        recalculateOrderTotal(order);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteOrderItemsByOrderId(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId())
                    .orElse(null);
            if (inventory != null) {
                inventory.setStockQuantity(inventory.getStockQuantity() + item.getQuantity());
            }
        }
        orderItemRepository.deleteAll(orderItems);
        
        orderRepository.findById(orderId).ifPresent(order -> order.setTotalAmount(BigDecimal.ZERO));
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void restoreStockForOrder(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : orderItems) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId())
                    .orElse(null);
            if (inventory != null) {
                inventory.setStockQuantity(inventory.getStockQuantity() + item.getQuantity());
            }
        }
    }

    private void recalculateOrderTotal(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal total = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
    }
}
