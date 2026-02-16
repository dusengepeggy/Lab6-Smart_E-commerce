package org.ecommerce.v1.dto.ResponseDto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private String categoryName;
}
