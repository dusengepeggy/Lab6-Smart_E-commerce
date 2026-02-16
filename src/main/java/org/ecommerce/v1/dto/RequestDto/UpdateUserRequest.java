package org.ecommerce.v1.dto.RequestDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ecommerce.v1.entity.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserRequest {

    @Size(min = 5, message = "Username must be at least 5 characters")
    private String username;

    private Role role;
}
