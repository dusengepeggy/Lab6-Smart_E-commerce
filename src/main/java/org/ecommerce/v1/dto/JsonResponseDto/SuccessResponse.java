package org.ecommerce.v1.dto.JsonResponseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse<T> {
    private String message;
    private T data;

    public SuccessResponse(String message) {
        this.message = message;
    }
}
