package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.entity.Inventory;
import org.ecommerce.v1.entity.Product;
import org.ecommerce.v1.repository.InventoryRepository;
import org.ecommerce.v1.repository.ProductRepository;
import org.ecommerce.v1.utils.exceptions.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public Inventory createInventory(Long productId, Long stockQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product with ID " + productId + " not found"));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setStockQuantity(stockQuantity);

        return inventoryRepository.save(inventory);
    }

    @Transactional(readOnly = true)
    public Inventory getInventoryById(Long inventoryId) {
        return inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new NotFoundException("Inventory with ID " + inventoryId + " not found"));
    }

    @Transactional(readOnly = true)
    public Inventory getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Inventory for product ID " + productId + " not found"));
    }

    @Transactional(readOnly = true)
    public Page<Inventory> getAllInventoriesPaged(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return inventoryRepository.findAll(pageable);
    }

    public Inventory updateInventoryStock(Long inventoryId, Long stockQuantity) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new NotFoundException("Inventory with ID " + inventoryId + " not found"));

        inventory.setStockQuantity(stockQuantity);
        return inventory;
    }

    public Inventory adjustStockByProductId(Long productId, Long quantityDelta) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Inventory for product ID " + productId + " not found"));

        Long newQuantity = inventory.getStockQuantity() + quantityDelta;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        inventory.setStockQuantity(newQuantity);
        return inventory;
    }

    public void deleteInventory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new NotFoundException("Inventory with ID " + inventoryId + " not found"));
        inventoryRepository.delete(inventory);
    }
}
