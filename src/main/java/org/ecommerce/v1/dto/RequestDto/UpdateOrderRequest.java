package org.ecommerce.v1.dto.RequestDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ecommerce.v1.entity.OrderStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderRequest {
    private OrderStatus status;
}
