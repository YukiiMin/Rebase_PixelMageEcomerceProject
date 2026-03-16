package com.example.PixelMageEcomerceProject.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.PixelMageEcomerceProject.dto.request.AccountRequestDTO;
import com.example.PixelMageEcomerceProject.dto.request.LoginRequestDTO;
import com.example.PixelMageEcomerceProject.dto.response.ResponseBase;
import com.example.PixelMageEcomerceProject.entity.Account;
import com.example.PixelMageEcomerceProject.entity.AuthProvider;
import com.example.PixelMageEcomerceProject.security.service.AuthenticationService;
import com.example.PixelMageEcomerceProject.service.interfaces.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing user accounts")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

        private final AccountService accountService;
        private final AuthenticationService authenticationService;

        /**
         * Create a new account
         */
        @PostMapping("/registration")
        @Operation(summary = "Create a new account", description = "Create a new user account with email, password, name, phone number and role")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Account created successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Email already exists or invalid data", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Account>> createAccount(
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Account details to create", required = true, content = @Content(schema = @Schema(implementation = AccountRequestDTO.class))) @RequestBody AccountRequestDTO account) {
                try {
                        Account createdAccount = accountService.createAccount(account);
                        return ResponseBase.created(createdAccount, "Account created successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to create account: " + e.getMessage());
                }
        }

        /**
         * Get all accounts
         */
        @GetMapping("/list")
        @Operation(summary = "Get all accounts", description = "Retrieve a list of all user accounts in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<List<Account>>> getAllAccounts() {
                List<Account> accounts = accountService.getAllAccounts();
                return ResponseBase.ok(accounts, "Accounts retrieved successfully");
        }

        /**
         * Get account by ID
         */
        @GetMapping("/{id}")
        @Operation(summary = "Get account by ID", description = "Retrieve account details by customer ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Account>> getAccountById(
                        @Parameter(description = "Customer ID", required = true) @PathVariable Integer id) {
                return accountService.getAccountById(id)
                                .map(account -> ResponseBase.ok(account, "Account retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Account not found with id: " + id));
        }

        /**
         * Get account by email
         */
        @GetMapping("/email/{email}")
        @Operation(summary = "Get account by email", description = "Retrieve account details by email address")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account retrieved successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Account>> getAccountByEmail(
                        @Parameter(description = "Email address", required = true) @PathVariable String email) {
                return accountService.getAccountByEmail(email)
                                .map(account -> ResponseBase.ok(account, "Account retrieved successfully"))
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Account not found with email: " + email));
        }

        /**
         * Update account
         */
        @PutMapping("/{id}")
        @Operation(summary = "Update account", description = "Update existing account information")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Account updated successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "400", description = "Bad request - Invalid data or email already exists", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Account>> updateAccount(
                        @Parameter(description = "Customer ID", required = true) @PathVariable Integer id,
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated account details", required = true, content = @Content(schema = @Schema(implementation = Account.class))) @RequestBody Account account) {
                try {
                        Account updatedAccount = accountService.updateAccount(id, account);
                        return ResponseBase.ok(updatedAccount, "Account updated successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.BAD_REQUEST,
                                        "Failed to update account: " + e.getMessage());
                }
        }

        /**
         * Delete account
         */
        @DeleteMapping("/{id}")
        @Operation(summary = "Delete account", description = "Delete an account by customer ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Account deleted successfully", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Void>> deleteAccount(
                        @Parameter(description = "Customer ID", required = true) @PathVariable Integer id) {
                try {
                        accountService.deleteAccount(id);
                        return ResponseBase.success("Account deleted successfully");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.NOT_FOUND, "Failed to delete account: " + e.getMessage());
                }
        }

        /**
         * Check if email exists
         */
        @GetMapping("/exists/{email}")
        @Operation(summary = "Check if email exists", description = "Check if an email address is already registered in the system")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email check completed", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Boolean>> checkEmailExists(
                        @Parameter(description = "Email address to check", required = true) @PathVariable String email) {
                boolean exists = accountService.existsByEmail(email);
                return ResponseBase.ok(exists, "Email check completed");
        }

        @PostMapping("/login")
        public ResponseEntity<ResponseBase<Map<String, Object>>> loginAccount(
                        @RequestBody LoginRequestDTO loginRequestDTO) {
                try {
                        Map<String, Object> loginResponse = accountService.loginAccount(loginRequestDTO);
                        return ResponseBase.ok(loginResponse, "Login successful");
                } catch (RuntimeException e) {
                        return ResponseBase.error(HttpStatus.UNAUTHORIZED, "Login failed: " + e.getMessage());
                }
        }

        // === OAuth2 Authentication Endpoints ===

        /**
         * Initiate Google OAuth2 authentication
         */
        @GetMapping("/auth/google")
        @Operation(summary = "Initiate Google OAuth2 authentication", description = "Redirects user to Google OAuth2 authorization page for social login")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "302", description = "Redirect to Google OAuth2 authorization"),
                        @ApiResponse(responseCode = "500", description = "OAuth2 initialization failed")
        })
        public ResponseEntity<Object> initiateGoogleLogin() {
                // This endpoint will trigger Spring Security's OAuth2 flow automatically
                // Redirect to Google OAuth2 authorization endpoint
                String googleAuthUrl = "/oauth2/authorization/google";

                return ResponseEntity.status(HttpStatus.FOUND)
                                .header("Location", googleAuthUrl)
                                .build();
        }

        /**
         * Check OAuth2 account linking capability
         */
        @GetMapping("/auth/can-link")
        @Operation(summary = "Check if OAuth2 account can be linked", description = "Verify if an email can be used for OAuth2 account creation or linking")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Link capability check completed", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Map<String, Object>>> canLinkOAuth2Account(
                        @Parameter(description = "Email address to check", required = true) @RequestParam String email) {

                boolean canCreate = authenticationService.canCreateOAuth2Account(email);
                String message = canCreate
                                ? "Email can be used for OAuth2 authentication"
                                : "Email already linked to another OAuth2 provider";

                Map<String, Object> result = Map.of(
                                "email", email,
                                "canLink", canCreate);
                return ResponseBase.ok(result, message);
        }

        /**
         * Get account authentication provider information
         */
        @GetMapping("/auth/provider/{email}")
        @Operation(summary = "Get account authentication provider", description = "Retrieve authentication provider information for an account")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Provider information retrieved", content = @Content(schema = @Schema(implementation = ResponseBase.class))),
                        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Map<String, Object>>> getAccountProvider(
                        @Parameter(description = "Email address", required = true) @PathVariable String email) {

                return accountService.getAccountByEmail(email)
                                .map(account -> {
                                        Map<String, Object> providerInfo = Map.of(
                                                        "email", account.getEmail(),
                                                        "authProvider", account.getAuthProvider(),
                                                        "hasLocalPassword", account.getPassword() != null,
                                                        "isOAuth2Account",
                                                        account.getAuthProvider() != AuthProvider.LOCAL);

                                        return ResponseBase.ok(providerInfo, "Provider information retrieved");
                                })
                                .orElseGet(() -> ResponseBase.error(HttpStatus.NOT_FOUND,
                                                "Account not found with email: " + email));
        }

        /**
         * OAuth2 logout endpoint
         */
        @PostMapping("/auth/logout")
        @Operation(summary = "OAuth2 logout", description = "Logout user from OAuth2 session (local JWT invalidation)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content(schema = @Schema(implementation = ResponseBase.class)))
        })
        public ResponseEntity<ResponseBase<Map<String, Object>>> oAuth2Logout() {
                // For JWT-based authentication, logout is typically handled on the client side
                // by removing the token. This endpoint confirms successful logout.
                Map<String, Object> result = Map.of(
                                "action", "remove_jwt_token",
                                "timestamp", System.currentTimeMillis());
                return ResponseBase.ok(result, "Logout successful. Please remove JWT token from client storage.");
        }
}
