package org.example.hotelbookingservice.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.hotelbookingservice.dto.common.ApiResponse;
import org.example.hotelbookingservice.dto.request.auth.EmailRequest;
import org.example.hotelbookingservice.dto.request.auth.LoginRequest;
import org.example.hotelbookingservice.dto.request.auth.RegisterRequest;
import org.example.hotelbookingservice.dto.request.auth.ResetPasswordRequest;
import org.example.hotelbookingservice.dto.request.auth.TokenRequest;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.AuthSessionResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.RegistrationResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.TokenApiResponse;
import org.example.hotelbookingservice.dto.response.auth.AuthResponses.TokenResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "APIs related to user authentication (Registration, Login)")
public interface AuthApi {

    @Operation(summary = "Register a new account", description = "Create a customer account (CUSTOMER).")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Registration successful"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error (invalid email format, weak password...) or email already exists", content = @Content), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "System error", content = @Content) })
    @PostMapping("/register")
    ApiResponse<RegistrationResponse> registerUser(@RequestBody @Valid RegisterRequest request);

    @Operation(summary = "Login", description = "Authenticate user and return JWT Token.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenApiResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "success": true,
                                      "message": "User logged in successfully",
                                      "data": {
                                        "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                        "jti": "7b4d0f08-7a9f-4c92-9c3a-22c6405e68af",
                                        "tokenType": "Bearer"
                                      }
                                    }
                                    """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Incorrect password or account is locked", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Email does not exist", content = @Content)
    })
    @PostMapping("/login")
    ApiResponse<TokenResponse> loginUser(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse response);

    @Operation(summary = "Logout", description = "Clear authentication context on server side. Client needs to delete token in LocalStorage.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = { @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout successful"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Not logged in", content = @Content) })
    @PostMapping("/logout")
    ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "Refresh access token")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Refresh successful",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TokenApiResponse.class),
                    examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "success": true,
                              "message": "Token refreshed successfully",
                              "data": {
                                "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
                                "jti": "7b4d0f08-7a9f-4c92-9c3a-22c6405e68af",
                                "tokenType": "Bearer"
                              }
                            }
                            """)))
    @PostMapping("/refresh")
    ApiResponse<TokenResponse> refresh(HttpServletRequest request, HttpServletResponse response);

    @Operation(summary = "Log out all sessions")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout-all")
    ApiResponse<Void> logoutAll(HttpServletResponse response);

    @Operation(summary = "List active sessions")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/sessions")
    ApiResponse<List<AuthSessionResponse>> sessions();

    @Operation(summary = "Verify email with request body token")
    @PostMapping("/verify-email")
    ApiResponse<Void> verifyEmail(@RequestBody @Valid TokenRequest request);

    @Operation(summary = "Verify email with query token")
    @GetMapping("/verify-email")
    ApiResponse<Void> verifyEmailQuery(@RequestParam String token);

    @Operation(summary = "Resend verification email")
    @PostMapping("/resend")
    ApiResponse<Void> resend(@RequestBody @Valid EmailRequest request);

    @Operation(summary = "Start forgot password flow")
    @PostMapping("/forgot-password")
    ApiResponse<Void> forgotPassword(@RequestBody @Valid EmailRequest request);

    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request);
}
