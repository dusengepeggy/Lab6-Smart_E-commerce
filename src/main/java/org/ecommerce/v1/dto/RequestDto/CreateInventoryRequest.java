package org.ecommerce.v1.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateInventoryRequest {
    private Long productId;
    private Long stockQuantity;
}
