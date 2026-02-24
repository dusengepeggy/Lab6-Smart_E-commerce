package org.ecommerce.v1.dto.ResponseDto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDetailDTO {

    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;

    private Long categoryId;
    private String categoryName;

    private Integer stockQuantity;
}

