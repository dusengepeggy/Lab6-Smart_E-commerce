package org.ecommerce.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api")
@Tag(name = "CSRF Demo", description = "CSRF explanation for documentation")
public class CsrfDemoController {

    @GetMapping("/csrf-demo")
    @Operation(summary = "CSRF explanation",
            description = "Returns an explanation of CSRF and why it is disabled for this JWT-based API. " +
                    "For form-based (cookie-session) apps, you would enable CSRF and include the token in forms or X-CSRF-TOKEN header.")
    public ResponseEntity<Map<String, String>> csrfInfo() {
        return ResponseEntity.ok(Map.of(
                "message", "This API uses stateless JWT in the Authorization header; CSRF protection is disabled.",
                "whenToEnableCsrf", "Enable CSRF when using cookie-based sessions and HTML forms (e.g. form-based login).",
                "howItWorks", "Server would issue a CsrfToken per session; client sends it in header (e.g. X-CSRF-TOKEN) or as a hidden form field."
        ));
    }
}
