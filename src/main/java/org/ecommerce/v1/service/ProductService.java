package org.ecommerce.v1.service;

import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.utils.exceptions.NotFoundException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ecommerce.v1.dto.RequestDto.AddProductRequest;
import org.ecommerce.v1.dto.ResponseDto.ProductDTO;
import org.ecommerce.v1.dto.ResponseDto.ProductDetailDTO;
import org.ecommerce.v1.entity.Category;
import org.ecommerce.v1.entity.Product;
import org.ecommerce.v1.repository.CategoryRepository;
import org.ecommerce.v1.repository.ProductRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductDTO createProduct(AddProductRequest request) {

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Product product = new Product();
        product.setProductName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);

        productRepository.save(product);

        return convertToDTO(product);
    }

    @Transactional(readOnly = true)
    public ProductDetailDTO getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        return convertToDetailDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getProducts(
            int page,
            int size,
            String sortBy,
            String direction,
            Long categoryId,
            String name,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> products;

        if (categoryId != null) {
            products = productRepository.findByCategoryId(categoryId, pageable);
        } else if (name != null && !name.isBlank()) {
            products = productRepository.findByProductNameContainingIgnoreCase(name, pageable);
        } else if (minPrice != null && maxPrice != null) {
            products = productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(this::convertToDTO);
    }

    public ProductDTO updateProduct(Long productId, AddProductRequest request) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (request.getName() != null) product.setProductName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            product.setCategory(category);
        }

        return convertToDTO(product);
    }

    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        productRepository.delete(product);
    }


    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategoryName(product.getCategory().getCategoryName());
        return dto;
    }

    private ProductDetailDTO convertToDetailDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCategoryName(product.getCategory().getCategoryName());

//        if (product.getInventory() != null) {
//            dto.setStock(product.getInventory().getQuantity());
//        }

        return dto;
    }
}
