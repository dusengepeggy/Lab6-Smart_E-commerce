package org.ecommerce.v1.dto.RequestDto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductQueryParams {

    private Integer categoryId;

    @Size(max = 100, message = "Search query must not exceed 100 characters")
    private String q;

    @DecimalMin(value = "0.0", inclusive = true, message = "Minimum price cannot be negative")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Maximum price cannot be negative")
    private BigDecimal maxPrice;

    private Boolean inStock;

    @Min(value = 0, message = "Page number cannot be negative")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private int size = 20;

    @Pattern(regexp = "^(name|price|createdAt)$", message = "Sort by must be one of: name, price, createdAt")
    private String sortBy = "name";

    @Pattern(regexp = "^(asc|desc)$", message = "Sort direction must be 'asc' or 'desc'")
    private String sortDir = "asc";
}
