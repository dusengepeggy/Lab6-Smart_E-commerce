package org.ecommerce.v1.dto.RequestDto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewRequest {

    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @Size(min = 5, max = 500, message = "Comment must be between 5 and 500 characters")
    private String comment;
}
