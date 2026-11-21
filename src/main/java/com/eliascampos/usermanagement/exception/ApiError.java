package com.eliascampos.usermanagement.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

/**
 * Representação documental do erro devolvido pela API. O runtime continua usando
 * ProblemDetail (RFC 9457); este record existe para o Swagger mostrar o formato real,
 * incluindo os campos code e timestamp.
 */
@Schema(name = "ApiError", description = "Erro no formato RFC 9457 com os campos adicionais da API.")
public record ApiError(

        @Schema(description = "URI que identifica o tipo do problema", example = "about:blank")
        URI type,

        @Schema(description = "Título curto do erro", example = "Dados invalidos")
        String title,

        @Schema(description = "Código HTTP", example = "400")
        int status,

        @Schema(description = "Explicação legível do erro", example = "Um ou mais campos estao invalidos")
        String detail,

        @Schema(description = "Caminho da requisição que gerou o erro", example = "/api/v1/users")
        URI instance,

        @Schema(description = "Código estável para tratamento no cliente", example = "VALIDATION_ERROR")
        String code,

        @Schema(description = "Momento em que o erro ocorreu (ISO-8601)", example = "2026-11-21T00:00:00Z")
        Instant timestamp,

        @Schema(description = "Erros por campo; presente apenas em falhas de validação")
        Map<String, String> errors
) {
}
