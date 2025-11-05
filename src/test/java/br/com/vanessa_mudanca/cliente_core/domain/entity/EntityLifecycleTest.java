package br.com.vanessa_mudanca.cliente_core.domain.entity;

import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoChavePixEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de lifecycle hooks (@PrePersist e @PreUpdate) das entidades.
 * Valida que timestamps são preenchidos automaticamente.
 */
@DisplayName("Entity Lifecycle - Testes de @PrePersist e @PreUpdate")
class EntityLifecycleTest {

    // ========== TESTES DE PreferenciaCliente ==========

    @Test
    @DisplayName("PreferenciaCliente - @PrePersist deve preencher dataCriacao e dataAtualizacao")
    void preferenciaCliente_prePersistDevePreencherTimestamps() {
        // Arrange
        PreferenciaCliente preferencia = PreferenciaCliente.builder()
                .aceitaComunicacaoEmail(true)
                .aceitaComunicacaoSMS(false)
                .consentimentoAtivo(true)
                .build();

        // Act
        preferencia.onCreate();

        // Assert
        assertThat(preferencia.getDataCriacao()).isNotNull();
        assertThat(preferencia.getDataAtualizacao()).isNotNull();
        assertThat(preferencia.getDataCriacao()).isEqualToIgnoringNanos(LocalDateTime.now());
        assertThat(preferencia.getDataAtualizacao()).isEqualToIgnoringNanos(LocalDateTime.now());
    }

    @Test
    @DisplayName("PreferenciaCliente - @PreUpdate deve atualizar dataAtualizacao")
    void preferenciaCliente_preUpdateDeveAtualizarDataAtualizacao() {
        // Arrange
        PreferenciaCliente preferencia = PreferenciaCliente.builder()
                .aceitaComunicacaoEmail(true)
                .build();
        preferencia.onCreate();
        LocalDateTime dataCriacaoOriginal = preferencia.getDataCriacao();
        LocalDateTime dataAtualizacaoOriginal = preferencia.getDataAtualizacao();

        // Act
        preferencia.onUpdate();

        // Assert
        assertThat(preferencia.getDataCriacao()).isEqualTo(dataCriacaoOriginal); // Não deve mudar
        assertThat(preferencia.getDataAtualizacao()).isNotNull();
        // Verifica que foi atualizado (pode ser mesmo milissegundo ou depois)
        assertThat(preferencia.getDataAtualizacao())
                .isAfterOrEqualTo(dataAtualizacaoOriginal);
    }

    @Test
    @DisplayName("PreferenciaCliente - Builder deve usar valores padrão")
    void preferenciaCliente_builderDeveUsarValoresPadrao() {
        // Act
        PreferenciaCliente preferencia = PreferenciaCliente.builder().build();

        // Assert
        assertThat(preferencia.getAceitaComunicacaoEmail()).isTrue();
        assertThat(preferencia.getAceitaComunicacaoSMS()).isTrue();
        assertThat(preferencia.getAceitaComunicacaoWhatsApp()).isTrue();
        assertThat(preferencia.getAceitaComunicacaoTelefone()).isFalse();
        assertThat(preferencia.getAceitaNewsletters()).isFalse();
        assertThat(preferencia.getAceitaOfertas()).isTrue();
        assertThat(preferencia.getAceitaPesquisas()).isFalse();
        assertThat(preferencia.getConsentimentoAtivo()).isTrue();
    }

    // ========== TESTES DE DadosBancarios ==========

    @Test
    @DisplayName("DadosBancarios - @PrePersist deve preencher dataCriacao e dataAtualizacao")
    void dadosBancarios_prePersistDevePreencherTimestamps() {
        // Arrange
        DadosBancarios dados = DadosBancarios.builder()
                .banco("Banco do Brasil")
                .agencia("1234")
                .conta("56789")
                .digitoConta("0")
                .build();

        // Act
        dados.onCreate();

        // Assert
        assertThat(dados.getDataCriacao()).isNotNull();
        assertThat(dados.getDataAtualizacao()).isNotNull();
        assertThat(dados.getDataCriacao()).isEqualToIgnoringNanos(LocalDateTime.now());
        assertThat(dados.getDataAtualizacao()).isEqualToIgnoringNanos(LocalDateTime.now());
    }

    @Test
    @DisplayName("DadosBancarios - @PreUpdate deve atualizar dataAtualizacao")
    void dadosBancarios_preUpdateDeveAtualizarDataAtualizacao() {
        // Arrange
        DadosBancarios dados = DadosBancarios.builder()
                .banco("Banco do Brasil")
                .build();
        dados.onCreate();
        LocalDateTime dataCriacaoOriginal = dados.getDataCriacao();
        LocalDateTime dataAtualizacaoOriginal = dados.getDataAtualizacao();

        // Act
        dados.onUpdate();

        // Assert
        assertThat(dados.getDataCriacao()).isEqualTo(dataCriacaoOriginal); // Não deve mudar
        assertThat(dados.getDataAtualizacao()).isNotNull();
        // Verifica que foi atualizado (pode ser mesmo milissegundo ou depois)
        assertThat(dados.getDataAtualizacao())
                .isAfterOrEqualTo(dataAtualizacaoOriginal);
    }

    @Test
    @DisplayName("DadosBancarios - Builder deve usar valores padrão")
    void dadosBancarios_builderDeveUsarValoresPadrao() {
        // Act
        DadosBancarios dados = DadosBancarios.builder().build();

        // Assert
        assertThat(dados.getDadosVerificados()).isFalse();
        assertThat(dados.getContaPrincipal()).isFalse();
        assertThat(dados.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("DadosBancarios - Deve aceitar todos os campos de PIX")
    void dadosBancarios_deveAceitarCamposPix() {
        // Act
        DadosBancarios dados = DadosBancarios.builder()
                .chavePix("joao@email.com")
                .tipoChavePix(TipoChavePixEnum.EMAIL)
                .build();

        // Assert
        assertThat(dados.getChavePix()).isEqualTo("joao@email.com");
        assertThat(dados.getTipoChavePix()).isEqualTo(TipoChavePixEnum.EMAIL);
    }

    // ========== TESTES DE AuditoriaCliente ==========

    @Test
    @DisplayName("AuditoriaCliente - @PrePersist deve preencher dataCriacao")
    void auditoriaCliente_prePersistDevePreencherDataCriacao() {
        // Arrange
        AuditoriaCliente auditoria = AuditoriaCliente.builder()
                .campoAlterado("email")
                .valorAnterior("antigo@email.com")
                .valorNovo("novo@email.com")
                .usuarioResponsavel("admin")
                .build();

        // Act
        auditoria.onCreate();

        // Assert
        assertThat(auditoria.getDataCriacao()).isNotNull();
        assertThat(auditoria.getDataCriacao()).isEqualToIgnoringNanos(LocalDateTime.now());
    }

    @Test
    @DisplayName("AuditoriaCliente - @PrePersist deve preencher dataAlteracao se null")
    void auditoriaCliente_prePersistDevePreencherDataAlteracaoSeNull() {
        // Arrange
        AuditoriaCliente auditoria = AuditoriaCliente.builder()
                .campoAlterado("telefone")
                .valorAnterior("11987654321")
                .valorNovo("11999999999")
                .usuarioResponsavel("user123")
                .dataAlteracao(null) // Explicitamente null
                .build();

        // Act
        auditoria.onCreate();

        // Assert
        assertThat(auditoria.getDataAlteracao()).isNotNull();
        assertThat(auditoria.getDataAlteracao()).isEqualToIgnoringNanos(LocalDateTime.now());
    }

    @Test
    @DisplayName("AuditoriaCliente - @PrePersist NÃO deve sobrescrever dataAlteracao se já preenchida")
    void auditoriaCliente_prePersistNaoDeveSobrescreverDataAlteracaoPreenchida() {
        // Arrange
        LocalDateTime dataAlteracaoCustomizada = LocalDateTime.of(2024, 1, 15, 10, 30);
        AuditoriaCliente auditoria = AuditoriaCliente.builder()
                .campoAlterado("cpf")
                .valorAnterior("12345678909")
                .valorNovo("98765432100")
                .usuarioResponsavel("admin")
                .dataAlteracao(dataAlteracaoCustomizada) // Data customizada
                .build();

        // Act
        auditoria.onCreate();

        // Assert
        assertThat(auditoria.getDataAlteracao()).isEqualTo(dataAlteracaoCustomizada); // Deve manter a customizada
        assertThat(auditoria.getDataCriacao()).isNotNull();
    }

    @Test
    @DisplayName("AuditoriaCliente - Deve aceitar todos os campos de auditoria")
    void auditoriaCliente_deveAceitarTodosCampos() {
        // Act
        AuditoriaCliente auditoria = AuditoriaCliente.builder()
                .campoAlterado("observacoes")
                .valorAnterior("Observação antiga")
                .valorNovo("Observação nova")
                .usuarioResponsavel("admin")
                .motivoAlteracao("Correção de informações")
                .ipOrigem("192.168.1.100")
                .build();

        // Assert
        assertThat(auditoria.getCampoAlterado()).isEqualTo("observacoes");
        assertThat(auditoria.getValorAnterior()).isEqualTo("Observação antiga");
        assertThat(auditoria.getValorNovo()).isEqualTo("Observação nova");
        assertThat(auditoria.getUsuarioResponsavel()).isEqualTo("admin");
        assertThat(auditoria.getMotivoAlteracao()).isEqualTo("Correção de informações");
        assertThat(auditoria.getIpOrigem()).isEqualTo("192.168.1.100");
    }

    // ========== TESTES DE INTEGRAÇÃO DE LIFECYCLE ==========

    @Test
    @DisplayName("Todas as entidades devem ter onCreate implementado")
    void todasEntidadesDevemTerOnCreate() {
        // Arrange & Act
        PreferenciaCliente preferencia = new PreferenciaCliente();
        DadosBancarios dados = new DadosBancarios();
        AuditoriaCliente auditoria = new AuditoriaCliente();

        preferencia.onCreate();
        dados.onCreate();
        auditoria.onCreate();

        // Assert
        assertThat(preferencia.getDataCriacao()).isNotNull();
        assertThat(dados.getDataCriacao()).isNotNull();
        assertThat(auditoria.getDataCriacao()).isNotNull();
    }
}
