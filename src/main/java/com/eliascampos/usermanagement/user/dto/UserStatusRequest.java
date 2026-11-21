package com.eliascampos.usermanagement.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @Schema(description = "true ativa o usuário; false o desativa", example = "false")
        @NotNull(message = "O campo active e obrigatorio")
        Boolean active
) {
}
