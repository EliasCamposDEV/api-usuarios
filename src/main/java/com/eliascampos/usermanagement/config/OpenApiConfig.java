package com.eliascampos.usermanagement.config;

import com.eliascampos.usermanagement.user.UserController;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenApiConfig {

    static final String BEARER_SCHEME = "bearerAuth";
    private static final String PROBLEM_JSON = "application/problem+json";

    @Bean
    public OpenAPI livroDaGuildaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Livro da Guilda")
                        .description("""
                                **Demo pública somente leitura.** Use `demo@eliascampos.dev` / `demo-readonly-2026` para explorar.
                                Escritas em `/api/v1/**` retornam `403 READ_ONLY_DEMO`.

                                **Como usar**
                                1. Chame `POST /auth/login` com as credenciais da demo (o corpo já vem preenchido).
                                2. Copie o `accessToken` da resposta.
                                3. Clique em **Authorize** e cole apenas o token (sem o prefixo `Bearer`).
                                4. Expanda um endpoint, ajuste os parâmetros e clique em **Execute**.

                                - Leitura funciona; escrita (POST/PUT/PATCH/DELETE em `/api/v1/**`) retorna `403 READ_ONLY_DEMO`.
                                - A instância gratuita hiberna; o primeiro acesso pode levar 30-60 s.
                                - Em execução local, use o admin criado no primeiro start (veja o README).

                                **Limites:** `/auth/login` bloqueia após 5 tentativas erradas por e-mail + IP em 15 min (429).""")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Elias Campos")
                                .url("https://github.com/EliasCamposDEV"))
                        .license(new License().name("MIT").url("https://opensource.org/licenses/MIT")))
                .externalDocs(new io.swagger.v3.oas.models.ExternalDocumentation()
                        .description("Código-fonte e documentação")
                        .url("https://github.com/EliasCamposDEV/livro-da-guilda"))
                .servers(List.of(new Server()
                        .url("/")
                        .description("Demo pública ou execução local")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token devolvido por `POST /auth/login`. "
                                + "Copie apenas o token, sem o prefixo `Bearer`. Válido por 1 hora.")));
    }

    /**
     * O Spring Data gera page/size/sort em inglês. Como o resto da documentação
     * está em português, ajusta as descrições geradas para o endpoint de listagem.
     */
    @Bean
    public OperationCustomizer pageableOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (handlerMethod.getBeanType() == UserController.class
                    && "findAll".equals(handlerMethod.getMethod().getName())
                    && operation.getParameters() != null) {
                operation.getParameters().forEach(parameter -> {
                    switch (parameter.getName()) {
                        case "page" -> parameter.setDescription("Número da página, começando em 0");
                        case "size" -> parameter.setDescription("Quantidade de itens por página (máximo 50)");
                        case "sort" -> parameter.setDescription(
                                "Ordenação no formato campo,asc|desc. Ex.: createdAt,DESC");
                        default -> {
                            // Mantém a descrição dos demais parâmetros.
                        }
                    }
                });
            }
            return operation;
        };
    }

    /**
     * O schema de erro é único, mas cada falha tem um corpo diferente. Este
     * customizer adiciona exemplos reais por status para o visitante ver o que a
     * API devolve em validação, falta de token, demo somente leitura etc.
     */
    @Bean
    public OperationCustomizer errorExamplesOperationCustomizer() {
        return (operation, handlerMethod) -> {
            if (operation.getResponses() == null) {
                return operation;
            }

            String methodName = handlerMethod.getMethod().getName();
            String path = examplePath(methodName);
            boolean demoWrite = operation.getRequestBody() != null || "delete".equals(methodName);

            operation.getResponses().forEach((status, response) -> {
                MediaType problem = problemMediaType(response);
                if (problem == null) {
                    return;
                }

                switch (status) {
                    case "400" -> problem.addExamples("validacao", problemExample(
                            "Dados inválidos", 400, "Dados invalidos",
                            "Um ou mais campos estao invalidos", "VALIDATION_ERROR", path,
                            Map.of("email", "Informe um e-mail valido")));
                    case "401" -> addUnauthorizedExample(problem, methodName, path);
                    case "403" -> addForbiddenExamples(problem, path, demoWrite);
                    case "404" -> problem.addExamples("naoEncontrado", problemExample(
                            "Recurso não encontrado", 404, "Recurso nao encontrado",
                            "Usuario nao encontrado para o id informado", "RESOURCE_NOT_FOUND", path, null));
                    case "409" -> problem.addExamples("emailJaCadastrado", problemExample(
                            "E-mail já cadastrado", 409, "Conflito de dados",
                            "Ja existe um usuario cadastrado com o e-mail informado",
                            "EMAIL_ALREADY_EXISTS", path, null));
                    case "429" -> problem.addExamples("muitasTentativas", problemExample(
                            "Muitas tentativas", 429, "Muitas tentativas de login",
                            "Muitas tentativas de login. Tente novamente mais tarde.",
                            "TOO_MANY_ATTEMPTS", path, null));
                    default -> {
                        // Status sem exemplo específico.
                    }
                }
            });

            return operation;
        };
    }

    private static void addUnauthorizedExample(MediaType problem, String methodName, String path) {
        switch (methodName) {
            case "login" -> problem.addExamples("credenciaisInvalidas", problemExample(
                    "Credenciais inválidas", 401, "Credenciais invalidas",
                    "E-mail ou senha invalidos", "INVALID_CREDENTIALS", path, null));
            case "refresh" -> problem.addExamples("refreshInvalido", problemExample(
                    "Refresh token inválido", 401, "Refresh token invalido",
                    "O refresh token e invalido ou expirou", "INVALID_REFRESH_TOKEN", path, null));
            default -> problem.addExamples("naoAutenticado", problemExample(
                    "Token ausente ou inválido", 401, "Nao autenticado",
                    "Informe um token Bearer valido", "UNAUTHORIZED", path, null));
        }
    }

    private static void addForbiddenExamples(MediaType problem, String path, boolean demoWrite) {
        if (demoWrite) {
            problem.addExamples("demoSomenteLeitura", problemExample(
                    "Demo somente leitura", 403, "Demo somente leitura",
                    "Esta demo e somente leitura: consulte os dados com GET, mas nao altere nada.",
                    "READ_ONLY_DEMO", path, null));
        }
        problem.addExamples("semPermissao", problemExample(
                "Sem permissão", 403, "Acesso negado",
                "O token nao tem permissao para este recurso", "ACCESS_DENIED", path, null));
    }

    private static MediaType problemMediaType(ApiResponse response) {
        if (response.getContent() == null) {
            return null;
        }
        return response.getContent().get(PROBLEM_JSON);
    }

    private static Example problemExample(String summary, int status, String title, String detail,
                                          String code, String instance, Map<String, String> errors) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", "about:blank");
        value.put("title", title);
        value.put("status", status);
        value.put("detail", detail);
        value.put("instance", instance);
        value.put("code", code);
        value.put("timestamp", "2026-11-21T00:00:00Z");
        if (errors != null && !errors.isEmpty()) {
            value.put("errors", errors);
        }
        return new Example().summary(summary).value(value);
    }

    private static String examplePath(String methodName) {
        return switch (methodName) {
            case "login" -> "/auth/login";
            case "refresh" -> "/auth/refresh";
            case "logout" -> "/auth/logout";
            case "changePassword" -> "/api/v1/me/password";
            case "findById", "update", "delete" -> "/api/v1/users/{id}";
            case "changeStatus" -> "/api/v1/users/{id}/status";
            default -> "/api/v1/users";
        };
    }
}
