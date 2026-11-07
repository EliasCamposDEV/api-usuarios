package com.eliascampos.usermanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("User Management API")
                .description("API REST para gerenciamento de usuarios com Spring Boot e MongoDB.")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Elias Campos")
                        .url("https://github.com/EliasCamposDEV"))
                .license(new License().name("MIT")));
    }
}
