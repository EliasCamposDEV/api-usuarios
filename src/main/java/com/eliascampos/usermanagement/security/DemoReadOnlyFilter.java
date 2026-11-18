package com.eliascampos.usermanagement.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.Set;

/**
 * Na demo publica a API e somente leitura: qualquer escrita em /api/v1 e recusada
 * antes de chegar ao controller. Login, refresh, logout e leitura continuam funcionando.
 * Ativado por app.demo.read-only=true (perfil demo); fora dele o filtro nem existe.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
@ConditionalOnProperty(name = "app.demo.read-only", havingValue = "true")
public class DemoReadOnlyFilter extends OncePerRequestFilter {

    static final String CODE = "READ_ONLY_DEMO";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final ObjectMapper objectMapper;

    public DemoReadOnlyFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/v1/") && !SAFE_METHODS.contains(request.getMethod())) {
            writeProblem(request, response);
            return;
        }
        chain.doFilter(request, response);
    }

    private void writeProblem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "Esta demo e somente leitura: consulte os dados com GET, mas nao altere nada.");
        problem.setTitle("Demo somente leitura");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("code", CODE);
        problem.setProperty("timestamp", Instant.now());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
