package com.eliascampos.usermanagement.user.dto;

import com.eliascampos.usermanagement.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Representação pública de um usuário (sem a senha)")
public record UserResponse(

        @Schema(description = "Id único do usuário", example = "65f1a2b3c4d5e6f7a8b9c0d1")
        String id,

        @Schema(description = "Nome completo", example = "Maria Silva")
        String name,

        @Schema(description = "E-mail único", example = "maria.silva@email.com")
        String email,

        @Schema(description = "Papel do usuário: USER ou ADMIN", example = "USER")
        UserRole role,

        @Schema(description = "Se o usuário pode autenticar", example = "true")
        boolean active,

        @Schema(description = "Data de criação (ISO-8601)", example = "2026-11-20T12:00:00Z")
        Instant createdAt,

        @Schema(description = "Data da última atualização (ISO-8601)", example = "2026-11-20T12:00:00Z")
        Instant updatedAt

) {

    public static UserResponse from(com.eliascampos.usermanagement.user.User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
