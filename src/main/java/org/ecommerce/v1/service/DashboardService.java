package org.ecommerce.v1.service;

import org.ecommerce.v1.config.AsyncConfig;
import org.ecommerce.v1.dto.ResponseDto.DashboardStatsDTO;
import org.ecommerce.v1.repository.InventoryRepository;
import org.ecommerce.v1.repository.OrderRepository;
import org.ecommerce.v1.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class DashboardService {

    private static final long LOW_STOCK_THRESHOLD = 10L;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final Executor dashboardExecutor;

    public DashboardService(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            InventoryRepository inventoryRepository,
                            @Qualifier(AsyncConfig.DASHBOARD_EXECUTOR) Executor dashboardExecutor) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.dashboardExecutor = dashboardExecutor;
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        CompletableFuture<Long> orderCount = CompletableFuture.supplyAsync(orderRepository::count, dashboardExecutor);
        CompletableFuture<Long> productCount = CompletableFuture.supplyAsync(productRepository::count, dashboardExecutor);
        CompletableFuture<Long> inventoryCount = CompletableFuture.supplyAsync(inventoryRepository::count, dashboardExecutor);
        CompletableFuture<Long> lowStockCount = CompletableFuture.supplyAsync(
                () -> inventoryRepository.countByStockQuantityLessThan(LOW_STOCK_THRESHOLD), dashboardExecutor);

        return CompletableFuture.allOf(orderCount, productCount, inventoryCount, lowStockCount)
                .thenApply(v -> DashboardStatsDTO.builder()
                        .orderCount(orderCount.join())
                        .productCount(productCount.join())
                        .inventoryCount(inventoryCount.join())
                        .lowStockCount(lowStockCount.join())
                        .build())
                .join();
    }
}
