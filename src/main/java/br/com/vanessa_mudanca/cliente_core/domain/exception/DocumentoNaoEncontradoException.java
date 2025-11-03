package br.com.vanessa_mudanca.cliente_core.domain.exception;

/**
 * Exceção lançada quando um documento não é encontrado pelo ID.
 */
public class DocumentoNaoEncontradoException extends BusinessException {

    public DocumentoNaoEncontradoException(Long documentoId) {
        super(String.format("Documento com ID %d não encontrado", documentoId));
    }
}
