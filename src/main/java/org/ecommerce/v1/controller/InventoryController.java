package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.ecommerce.v1.dto.JsonResponseDto.SuccessResponse;
import org.ecommerce.v1.dto.RequestDto.CreateInventoryRequest;
import org.ecommerce.v1.dto.RequestDto.UpdateInventoryRequest;
import org.ecommerce.v1.dto.ResponseDto.InventoryDTO;
import org.ecommerce.v1.dto.ResponseDto.PagedResponse;
import org.ecommerce.v1.entity.Inventory;
import org.ecommerce.v1.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@AllArgsConstructor
@Tag(name = "Inventory", description = "Stock per product")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<InventoryDTO>> createInventory(@RequestBody CreateInventoryRequest request) {
        Inventory inventory = inventoryService.createInventory(request.getProductId(), request.getStockQuantity());
        SuccessResponse<InventoryDTO> res = new SuccessResponse<>("Inventory created successfully", convertToDTO(inventory));
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<InventoryDTO>> getInventoryById(@PathVariable Long id) {
        Inventory inventory = inventoryService.getInventoryById(id);
        SuccessResponse<InventoryDTO> res = new SuccessResponse<>("Inventory retrieved successfully", convertToDTO(inventory));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<SuccessResponse<InventoryDTO>> getInventoryByProductId(@PathVariable Long productId) {
        Inventory inventory = inventoryService.getInventoryByProductId(productId);
        SuccessResponse<InventoryDTO> res = new SuccessResponse<>("Inventory retrieved successfully", convertToDTO(inventory));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<PagedResponse<InventoryDTO>>> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "stockQuantity") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Page<Inventory> inventories = inventoryService.getAllInventoriesPaged(page, size, sortBy, sortDir);
        List<InventoryDTO> inventoryDTOs = inventories.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        PagedResponse<InventoryDTO> pagedResponse = new PagedResponse<>(
                inventoryDTOs,
                inventories.getNumber(),
                inventories.getSize(),
                inventories.getTotalElements(),
                inventories.getTotalPages()
        );

        SuccessResponse<PagedResponse<InventoryDTO>> res = new SuccessResponse<>("Inventory fetched successfully", pagedResponse);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<InventoryDTO>> updateInventory(
            @PathVariable Long id,
            @RequestBody UpdateInventoryRequest request
    ) {
        Inventory updated = inventoryService.updateInventoryStock(id, request.getStockQuantity());
        SuccessResponse<InventoryDTO> res = new SuccessResponse<>("Inventory updated successfully", convertToDTO(updated));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PatchMapping("/product/{productId}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<InventoryDTO>> adjustStock(
            @PathVariable Long productId,
            @RequestParam Long delta
    ) {
        Inventory updated = inventoryService.adjustStockByProductId(productId, delta);
        SuccessResponse<InventoryDTO> res = new SuccessResponse<>("Stock adjusted successfully", convertToDTO(updated));
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<String>> deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        SuccessResponse<String> res = new SuccessResponse<>("Inventory deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProduct().getId());
        dto.setProductName(inventory.getProduct().getProductName());
        dto.setStockQuantity(inventory.getStockQuantity());
        return dto;
    }
}
