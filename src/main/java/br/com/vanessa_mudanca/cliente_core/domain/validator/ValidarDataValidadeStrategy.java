package br.com.vanessa_mudanca.cliente_core.domain.validator;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateDocumentoDTO;
import br.com.vanessa_mudanca.cliente_core.domain.exception.DataValidadeInvalidaException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Strategy para validar data de validade de documento.
 *
 * REGRAS:
 * - Data de validade não pode ser muito distante no futuro (> 50 anos)
 * - Data de validade deve ser após data de emissão (se ambas presentes)
 *
 * NOTA: Não validamos se data está no passado, pois pode ser documento expirado legítimo.
 * O status EXPIRADO será calculado automaticamente pela entidade.
 */
@Component
public class ValidarDataValidadeStrategy {

    private static final int MAX_ANOS_VALIDADE = 50;

    public void validar(UpdateDocumentoDTO dto) {
        if (dto.dataValidade() == null) {
            return; // Alguns documentos não têm validade (CPF, CNPJ)
        }

        LocalDate hoje = LocalDate.now();
        LocalDate dataValidade = dto.dataValidade();

        // Validar que não é muito distante no futuro (evita erros de digitação)
        if (dataValidade.isAfter(hoje.plusYears(MAX_ANOS_VALIDADE))) {
            throw new DataValidadeInvalidaException(
                    String.format(
                            "Data de validade não pode ser superior a %d anos. Data informada: %s",
                            MAX_ANOS_VALIDADE,
                            dataValidade
                    )
            );
        }

        // Validar coerência: dataValidade >= dataEmissao
        if (dto.dataEmissao() != null && dataValidade.isBefore(dto.dataEmissao())) {
            throw new DataValidadeInvalidaException(
                    String.format(
                            "Data de validade (%s) não pode ser anterior à data de emissão (%s)",
                            dataValidade,
                            dto.dataEmissao()
                    )
            );
        }
    }
}
