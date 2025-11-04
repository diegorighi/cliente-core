package br.com.vanessa_mudanca.cliente_core.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para cobrir métodos básicos dos Enums (values(), valueOf()).
 * Aumenta cobertura sem duplicar testes complexos.
 */
@DisplayName("Enums - Testes de cobertura básica")
class EnumsTest {

    @Test
    @DisplayName("EstadoEnum deve ter valores corretos")
    void estadoEnumDeveRetornarValores() {
        assertThat(EstadoEnum.values()).isNotEmpty();
        assertThat(EstadoEnum.valueOf("SP")).isEqualTo(EstadoEnum.SP);
        assertThat(EstadoEnum.SP.name()).isEqualTo("SP");
    }

    @Test
    @DisplayName("OrigemLeadEnum deve ter valores corretos")
    void origemLeadEnumDeveRetornarValores() {
        assertThat(OrigemLeadEnum.values()).isNotEmpty();
        assertThat(OrigemLeadEnum.valueOf("INDICACAO")).isEqualTo(OrigemLeadEnum.INDICACAO);
    }

    @Test
    @DisplayName("SexoEnum deve ter valores corretos")
    void sexoEnumDeveRetornarValores() {
        assertThat(SexoEnum.values()).isNotEmpty();
        assertThat(SexoEnum.valueOf("MASCULINO")).isEqualTo(SexoEnum.MASCULINO);
        assertThat(SexoEnum.MASCULINO.name()).isEqualTo("MASCULINO");
    }

    @Test
    @DisplayName("StatusDocumentoEnum deve ter valores corretos")
    void statusDocumentoEnumDeveRetornarValores() {
        assertThat(StatusDocumentoEnum.values()).isNotEmpty();
        assertThat(StatusDocumentoEnum.valueOf("VALIDO")).isEqualTo(StatusDocumentoEnum.VALIDO);
    }

    @Test
    @DisplayName("TipoChavePixEnum deve ter valores corretos")
    void tipoChavePixEnumDeveRetornarValores() {
        assertThat(TipoChavePixEnum.values()).isNotEmpty();
        assertThat(TipoChavePixEnum.valueOf("CPF")).isEqualTo(TipoChavePixEnum.CPF);
    }

    @Test
    @DisplayName("TipoClienteEnum deve ter valores corretos")
    void tipoClienteEnumDeveRetornarValores() {
        assertThat(TipoClienteEnum.values()).isNotEmpty();
        assertThat(TipoClienteEnum.valueOf("CONSIGNANTE")).isEqualTo(TipoClienteEnum.CONSIGNANTE);
        assertThat(TipoClienteEnum.values().length).isGreaterThan(0);
    }

    @Test
    @DisplayName("TipoContatoEnum deve ter valores corretos")
    void tipoContatoEnumDeveRetornarValores() {
        assertThat(TipoContatoEnum.values()).isNotEmpty();
        assertThat(TipoContatoEnum.valueOf("EMAIL")).isEqualTo(TipoContatoEnum.EMAIL);
    }

    @Test
    @DisplayName("TipoDocumentoEnum deve ter valores corretos")
    void tipoDocumentoEnumDeveRetornarValores() {
        assertThat(TipoDocumentoEnum.values()).isNotEmpty();
        assertThat(TipoDocumentoEnum.valueOf("CPF")).isEqualTo(TipoDocumentoEnum.CPF);
    }

    @Test
    @DisplayName("TipoEnderecoEnum deve ter valores corretos")
    void tipoEnderecoEnumDeveRetornarValores() {
        assertThat(TipoEnderecoEnum.values()).isNotEmpty();
        assertThat(TipoEnderecoEnum.valueOf("RESIDENCIAL")).isEqualTo(TipoEnderecoEnum.RESIDENCIAL);
    }

    @Test
    @DisplayName("Todos os enums devem ter pelo menos 1 valor")
    void todosEnumsDevemTerPeloMenosUmValor() {
        assertThat(EstadoEnum.values().length).isGreaterThan(0);
        assertThat(OrigemLeadEnum.values().length).isGreaterThan(0);
        assertThat(SexoEnum.values().length).isGreaterThan(0);
        assertThat(StatusDocumentoEnum.values().length).isGreaterThan(0);
        assertThat(TipoChavePixEnum.values().length).isGreaterThan(0);
        assertThat(TipoClienteEnum.values().length).isGreaterThan(0);
        assertThat(TipoContatoEnum.values().length).isGreaterThan(0);
        assertThat(TipoDocumentoEnum.values().length).isGreaterThan(0);
        assertThat(TipoEnderecoEnum.values().length).isGreaterThan(0);
    }
}
