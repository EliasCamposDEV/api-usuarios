package com.eliascampos.usermanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "Token JWT para o header Authorization",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Tipo do token; use como `Bearer <accessToken>`", example = "Bearer")
        String tokenType,

        @Schema(description = "Validade do access token em segundos", example = "3600")
        long expiresIn,

        @Schema(description = "Token de vida longa usado para obter um novo access token",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        String refreshToken,

        @Schema(description = "Validade do refresh token em segundos", example = "604800")
        long refreshExpiresIn
) {
}
