package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ecommerce.v1.dto.JsonResponseDto.SuccessResponse;
import org.ecommerce.v1.dto.RequestDto.AddProductRequest;
import org.ecommerce.v1.dto.ResponseDto.PagedResponse;
import org.ecommerce.v1.dto.ResponseDto.ProductDTO;
import org.ecommerce.v1.dto.ResponseDto.ProductDetailDTO;
import org.ecommerce.v1.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
@Tag(name = "Products", description = "Product listing, create, update, delete")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<SuccessResponse<PagedResponse<ProductDTO>>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "productName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice
    ) {
        Page<ProductDTO> products = productService.getProducts(page, size, sortBy, sortDir, categoryId, name, minPrice, maxPrice);

        PagedResponse<ProductDTO> pagedResponse = new PagedResponse<>(
                products.getContent(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages()
        );

        SuccessResponse<PagedResponse<ProductDTO>> res = new SuccessResponse<>("Products fetched successfully", pagedResponse);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<ProductDetailDTO>> getProductById(@PathVariable Long id) {
        ProductDetailDTO product = productService.getProductById(id);
        SuccessResponse<ProductDetailDTO> res = new SuccessResponse<>("Product retrieved successfully", product);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<ProductDTO>> createProduct(@Valid @RequestBody AddProductRequest request) {
        ProductDTO product = productService.createProduct(request);
        SuccessResponse<ProductDTO> res = new SuccessResponse<>("Product created successfully", product);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<ProductDTO>> updateProduct(
            @PathVariable Long id,
            @RequestBody AddProductRequest request
    ) {
        ProductDTO updated = productService.updateProduct(id, request);
        SuccessResponse<ProductDTO> res = new SuccessResponse<>("Product updated successfully", updated);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SuccessResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        SuccessResponse<String> res = new SuccessResponse<>("Product deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
