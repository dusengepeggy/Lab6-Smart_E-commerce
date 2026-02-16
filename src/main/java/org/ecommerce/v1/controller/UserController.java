package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.ecommerce.v1.dto.JsonResponseDto.SuccessResponse;
import org.ecommerce.v1.dto.RequestDto.RegisterRequest;
import org.ecommerce.v1.dto.RequestDto.UpdateUserRequest;
import org.ecommerce.v1.dto.ResponseDto.PagedResponse;
import org.ecommerce.v1.dto.ResponseDto.UserDTO;
import org.ecommerce.v1.entity.Role;
import org.ecommerce.v1.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Tag(name = "Users", description = "User registration and management")
public class UserController {
    private final UserService userService;

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Fetches a paginated list of all registered users"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Users fetched successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SuccessResponse<PagedResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String usernameFilter,
            @RequestParam(required = false) String emailFilter,
            @RequestParam(required = false) Role roleFilter
    ) {
        Page<UserDTO> users = userService.getUsersPaged(page, size, sortBy, sortDir, usernameFilter, emailFilter, roleFilter);

        PagedResponse<UserDTO> pagedResponse = new PagedResponse<>(
                users.getContent(),
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );

        SuccessResponse<PagedResponse<UserDTO>> res = new SuccessResponse<>("Users fetched successfully", pagedResponse);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<UserDTO>> register(@Valid @RequestBody RegisterRequest user) {
        UserDTO registeredUser = userService.register(user);
        SuccessResponse<UserDTO> res = new SuccessResponse<>("User registered successfully", registeredUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<SuccessResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        SuccessResponse<String> res = new SuccessResponse<>("User account deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        SuccessResponse<UserDTO> res = new SuccessResponse<>("User retrieved successfully", user);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SuccessResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        UserDTO user = userService.updateUser(id, request);
        SuccessResponse<UserDTO> res = new SuccessResponse<>("User account updated successfully", user);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }
}
