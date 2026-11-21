package com.eliascampos.usermanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @Schema(description = "Refresh token recebido no login. Cada uso devolve um novo par e invalida o anterior.",
                example = "eyJhbGciOiJIUzI1NiJ9...")
        @NotBlank(message = "O refresh token e obrigatorio")
        String refreshToken
) {
}
