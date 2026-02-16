package org.ecommerce.v1.dto.RequestDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ecommerce.v1.entity.Role;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterRequest {
    @Size(min = 5, message = "UserName should be more than 5 characters")
    @NotBlank(message = "Username is required")
    private String username;
    @Email(message = "Email must be valid")
    @NotBlank (message = "Email is required")
    private String email;
    @NotBlank (message = "Password field is required")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,}$",
            message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;
    @NotNull(message = "Role is required")
    private Role role;

    public RegisterRequest(String username, Role role) {
        this.username = username;
        this.role = role;

    }
}
