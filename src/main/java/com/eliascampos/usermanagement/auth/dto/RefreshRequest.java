package com.eliascampos.usermanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @Schema(description = "Refresh token recebido no login")
        @NotBlank(message = "O refresh token e obrigatorio")
        String refreshToken
) {
}
