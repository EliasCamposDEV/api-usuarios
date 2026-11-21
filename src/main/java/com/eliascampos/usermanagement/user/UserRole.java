package com.eliascampos.usermanagement.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Papel do usuário: USER (uso comum) ou ADMIN (gerencia usuários)")
public enum UserRole {

    @Schema(description = "Usuário comum")
    USER,

    @Schema(description = "Administrador")
    ADMIN
}
