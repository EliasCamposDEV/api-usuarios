package com.eliascampos.usermanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponse(
        @Schema(description = "Token JWT para o header Authorization") String accessToken,
        @Schema(example = "Bearer") String tokenType,
        @Schema(description = "Validade do token em segundos", example = "3600") long expiresIn
) {
}
