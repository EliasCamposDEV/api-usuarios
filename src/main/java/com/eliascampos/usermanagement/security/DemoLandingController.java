package com.eliascampos.usermanagement.security;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Na demo publica a raiz nao tem nada para mostrar (a API exige token), entao
 * quem abre o link no navegador cai direto no Swagger UI em vez de ver um 401.
 * Fora do perfil demo este controller nem existe e a raiz continua protegida.
 */
@RestController
@Hidden
@ConditionalOnProperty(name = "app.demo.read-only", havingValue = "true")
public class DemoLandingController {

    @GetMapping("/")
    public ResponseEntity<Void> index() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/swagger-ui.html"))
                .build();
    }
}
