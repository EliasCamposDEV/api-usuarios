package com.eliascampos.usermanagement.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Papel do usuario: USER (uso comum) ou ADMIN (gerencia usuarios)")
public enum UserRole {

    @Schema(description = "Usuario comum")
    USER,

    @Schema(description = "Administrador")
    ADMIN
}
