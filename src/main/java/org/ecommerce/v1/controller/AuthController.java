package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ecommerce.v1.dto.JsonResponseDto.SuccessResponse;
import org.ecommerce.v1.dto.RequestDto.LoginRequest;
import org.ecommerce.v1.dto.ResponseDto.LoginResponse;
import org.ecommerce.v1.security.CustomUserDetails;
import org.ecommerce.v1.security.JwtUtil;
import org.ecommerce.v1.security.TokenBlacklist;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "JWT login and logout")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenBlacklist tokenBlacklist;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticate with username and password; returns a signed JWT with subject, roles, and expiry.")
    @ApiResponse(responseCode = "200", description = "Login successful, JWT returned")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());
        String token = jwtUtil.generateToken(userDetails.getUsername(), roles);
        LoginResponse response = new LoginResponse(
                token, "Bearer", userDetails.getUsername(), roles, userDetails.getUserId());
        return ResponseEntity.ok(new SuccessResponse<>("Login successful", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revoke the current JWT (add to blacklist). Send Authorization: Bearer <token>.")
    @ApiResponse(responseCode = "200", description = "Token revoked")
    public ResponseEntity<SuccessResponse<String>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklist.revoke(authHeader.substring(7));
        }
        return ResponseEntity.ok(new SuccessResponse<>("Logged out successfully"));
    }
}
