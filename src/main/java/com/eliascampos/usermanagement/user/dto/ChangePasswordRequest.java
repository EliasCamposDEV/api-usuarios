package com.eliascampos.usermanagement.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Schema(description = "Senha atual do usuário autenticado", example = "senha12345")
        @NotBlank(message = "A senha atual e obrigatoria")
        String currentPassword,

        @Schema(description = "Nova senha, entre 8 e 72 caracteres", example = "novaSenha123")
        @NotBlank(message = "A nova senha e obrigatoria")
        @Size(min = 8, max = 72, message = "A nova senha deve ter entre 8 e 72 caracteres")
        String newPassword
) {
}
