package com.eliascampos.usermanagement.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Pagina de resultados com metadados de paginacao")
public record PageResponse<T>(

        @Schema(description = "Itens da pagina atual")
        List<T> content,

        @Schema(description = "Numero da pagina (base 0)", example = "0")
        int page,

        @Schema(description = "Tamanho da pagina", example = "10")
        int size,

        @Schema(description = "Total de registros", example = "7")
        long totalElements,

        @Schema(description = "Total de paginas", example = "1")
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
