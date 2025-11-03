package br.com.vanessa_mudanca.cliente_core.application.dto.output;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Record genérico para resposta paginada.
 * Segue o padrão de paginação do Spring Data.
 *
 * @param <T> Tipo do conteúdo da página
 */
@Schema(description = "Resposta paginada genérica")
public record PageResponse<T>(
        @Schema(description = "Lista de elementos da página atual")
        List<T> content,

        @Schema(description = "Número da página atual (zero-indexed)", example = "0")
        int pageNumber,

        @Schema(description = "Tamanho da página", example = "20")
        int pageSize,

        @Schema(description = "Total de elementos em todas as páginas", example = "100")
        long totalElements,

        @Schema(description = "Total de páginas disponíveis", example = "5")
        int totalPages,

        @Schema(description = "Indica se é a primeira página", example = "true")
        boolean first,

        @Schema(description = "Indica se é a última página", example = "false")
        boolean last,

        @Schema(description = "Indica se a página está vazia", example = "false")
        boolean empty
) {
    /**
     * Cria um PageResponse a partir de uma Page do Spring Data.
     */
    public static <T> PageResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}
