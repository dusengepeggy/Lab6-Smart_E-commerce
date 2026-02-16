package org.ecommerce.v1.dto.RequestDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCategoryRequest {

    @Size(min = 2, max = 50, message = "Category name must be between 2 and 50 characters")
    private String categoryName;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}
