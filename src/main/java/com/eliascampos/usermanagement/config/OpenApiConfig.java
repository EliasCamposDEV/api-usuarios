package com.eliascampos.usermanagement.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI livroDaGuildaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Livro da Guilda")
                        .description("""
                                API REST para cadastro de usuarios com autenticacao JWT e papeis (USER/ADMIN).

                                **Como usar**
                                1. Chame `POST /auth/login` com e-mail e senha.
                                2. Copie o `accessToken` da resposta.
                                3. Clique em **Authorize** e cole apenas o token (sem "Bearer").
                                4. Execute os endpoints protegidos.

                                **Demo publica:** https://livro-da-guilda.onrender.com/swagger-ui.html
                                - Login: `demo@eliascampos.dev` / `demo-readonly-2026`
                                - Leitura funciona; escrita (POST/PUT/PATCH/DELETE em `/api/v1/**`) retorna `403 READ_ONLY_DEMO`.
                                - A instancia gratuita hiberna; o primeiro acesso pode levar 30-60 s.

                                **Limites:** `/auth/login` bloqueia apos 5 tentativas erradas por e-mail + IP em 15 min (429).""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Elias Campos")
                                .url("https://github.com/EliasCamposDEV"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                        .description("Codigo-fonte e documentacao")
                        .url("https://github.com/EliasCamposDEV/livro-da-guilda"))
                .servers(List.of(new Server()
                        .url("/")
                        .description("Esta instancia (demo publica ou local)")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token devolvido por `POST /auth/login`. Valido por 1 hora.")));
    }
}
