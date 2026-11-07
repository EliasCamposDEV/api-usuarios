package com.eliascampos.usermanagement.user.dto;

import com.eliascampos.usermanagement.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @Schema(example = "Maria Silva")
        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 2, max = 120, message = "O nome deve ter entre 2 e 120 caracteres")
        String name,

        @Schema(example = "maria.silva@email.com")
        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Informe um e-mail valido")
        @Size(max = 160, message = "O e-mail deve ter no maximo 160 caracteres")
        String email,

        @Schema(example = "USER")
        @NotNull(message = "O perfil e obrigatorio")
        UserRole role
) {
}
