package org.ecommerce.v1.dto.JsonResponseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String timestamp;
    private Map<String, String> validationErrors;

    public ErrorResponse(int status, String error, String message, String timestamp) {
        this(status, error, message, timestamp, null);
    }
}
