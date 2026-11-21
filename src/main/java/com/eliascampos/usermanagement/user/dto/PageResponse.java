package com.eliascampos.usermanagement.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Página de resultados com metadados de paginação")
public record PageResponse<T>(

        @Schema(description = "Itens da página atual")
        List<T> content,

        @Schema(description = "Número da página (começa em 0)", example = "0")
        int page,

        @Schema(description = "Tamanho da página", example = "10")
        int size,

        @Schema(description = "Total de registros", example = "7")
        long totalElements,

        @Schema(description = "Total de páginas", example = "1")
        int totalPages

) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
