package org.ecommerce.v1.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderItemRequest {
    private Long orderId;
    private Long productId;
    private Long quantity;
}
