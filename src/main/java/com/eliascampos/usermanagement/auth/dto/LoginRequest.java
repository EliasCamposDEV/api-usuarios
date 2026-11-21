package com.eliascampos.usermanagement.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "E-mail de acesso. Na demo pública use demo@eliascampos.dev",
                example = "demo@eliascampos.dev")
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Informe um e-mail valido")
        String email,

        @Schema(description = "Senha de acesso. Na demo pública use demo-readonly-2026",
                example = "demo-readonly-2026")
        @NotBlank(message = "A senha e obrigatoria")
        String password
) {
}
