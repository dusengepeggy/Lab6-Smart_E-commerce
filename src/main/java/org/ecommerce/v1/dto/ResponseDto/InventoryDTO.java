package org.ecommerce.v1.dto.ResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long stockQuantity;
}
