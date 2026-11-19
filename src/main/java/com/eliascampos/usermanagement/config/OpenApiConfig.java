package com.eliascampos.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI livroDaGuildaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Livro da Guilda")
                        .description("Cadastro de usuarios com JWT e papeis, inspirado em RPG. "
                                + "Autentique em /auth/login e clique em Authorize para usar o access token.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Elias Campos")
                                .url("https://github.com/EliasCamposDEV"))
                        .license(new License().name("MIT")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token devolvido por /auth/login")));
    }
}

