package com.eliascampos.usermanagement.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @Schema(example = "false")
        @NotNull(message = "O campo active e obrigatorio")
        Boolean active
) {
}
