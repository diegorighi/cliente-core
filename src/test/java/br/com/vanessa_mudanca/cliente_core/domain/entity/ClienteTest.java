package br.com.vanessa_mudanca.cliente_core.domain.entity;

import br.com.vanessa_mudanca.cliente_core.domain.enums.OrigemLeadEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoContatoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoDocumentoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoEnderecoEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes para Cliente entity - Métodos comportamentais e lifecycle callbacks.
 *
 * Cenários testados:
 * 1. Soft delete (deletar/restaurar/isDeletado)
 * 2. Gerenciamento de listas (documentos, contatos, endereços, dados bancários)
 * 3. Lifecycle callbacks (@PrePersist, @PreUpdate)
 */
@DisplayName("Cliente - Testes de Métodos Comportamentais")
class ClienteTest {

    private ClientePF clientePF;

    @BeforeEach
    void setUp() {
        clientePF = ClientePF.builder()
                .primeiroNome("João")
                .sobrenome("Silva")
                .email("joao@example.com")
                .cpf("12345678910")
                .dataNascimento(LocalDate.of(1990, 5, 15))
                .ativo(true)
                .bloqueado(false)
                .build();
    }

    @Nested
    @DisplayName("Soft Delete - deletar() e restaurar()")
    class SoftDeleteTests {

        @Test
        @DisplayName("Deve deletar cliente com motivo e usuário")
        void deveDeletarClienteComMotivoEUsuario() {
            // Given
            String motivo = "Cliente solicitou exclusão";
            String usuario = "admin@system.com";

            // When
            clientePF.deletar(motivo, usuario);

            // Then
            assertThat(clientePF.getAtivo()).isFalse();
            assertThat(clientePF.getDataDelecao()).isNotNull();
            assertThat(clientePF.getDataDelecao()).isBeforeOrEqualTo(LocalDateTime.now());
            assertThat(clientePF.getMotivoDelecao()).isEqualTo(motivo);
            assertThat(clientePF.getUsuarioDeletou()).isEqualTo(usuario);
        }

        @Test
        @DisplayName("Deve marcar ativo como false ao deletar")
        void devemarcarAtivoFalseAoDeletar() {
            // Given
            assertThat(clientePF.getAtivo()).isTrue();

            // When
            clientePF.deletar("Teste", "user");

            // Then
            assertThat(clientePF.getAtivo()).isFalse();
        }

        @Test
        @DisplayName("Deve restaurar cliente deletado")
        void deveRestaurarClienteDeletado() {
            // Given
            clientePF.deletar("Motivo teste", "admin");
            assertThat(clientePF.getAtivo()).isFalse();

            // When
            String usuarioRestaurou = "supervisor@system.com";
            clientePF.restaurar(usuarioRestaurou);

            // Then
            assertThat(clientePF.getAtivo()).isTrue();
            assertThat(clientePF.getDataDelecao()).isNull();
            assertThat(clientePF.getMotivoDelecao()).isNull();
            assertThat(clientePF.getUsuarioDeletou()).isNull();
        }

        @Test
        @DisplayName("isDeletado() deve retornar true quando cliente está deletado")
        void isDeletadoDeveRetornarTrueQuandoDeletado() {
            // Given
            clientePF.deletar("Teste", "user");

            // When
            boolean isDeletado = clientePF.isDeletado();

            // Then
            assertThat(isDeletado).isTrue();
        }

        @Test
        @DisplayName("isDeletado() deve retornar false quando cliente está ativo")
        void isDeletadoDeveRetornarFalseQuandoAtivo() {
            // Given - cliente ativo (setUp)

            // When
            boolean isDeletado = clientePF.isDeletado();

            // Then
            assertThat(isDeletado).isFalse();
        }

        @Test
        @DisplayName("isDeletado() deve retornar false quando apenas ativo=false mas sem dataDelecao")
        void isDeletadoDeveRetornarFalseQuandoApenasAtivoFalse() {
            // Given
            clientePF.setAtivo(false);
            // dataDelecao permanece null

            // When
            boolean isDeletado = clientePF.isDeletado();

            // Then
            assertThat(isDeletado).isFalse();
        }

        @Test
        @DisplayName("Deve permitir deletar cliente já deletado (idempotência)")
        void devePermitirDeletarClienteJaDeletado() {
            // Given
            clientePF.deletar("Primeira deleção", "user1");
            LocalDateTime primeiradataDelecao = clientePF.getDataDelecao();

            // When - Tentar deletar novamente (pode acontecer em retry)
            clientePF.deletar("Segunda deleção", "user2");

            // Then - Deve atualizar os dados
            assertThat(clientePF.getAtivo()).isFalse();
            assertThat(clientePF.getDataDelecao()).isAfterOrEqualTo(primeiradataDelecao);
            assertThat(clientePF.getMotivoDelecao()).isEqualTo("Segunda deleção");
            assertThat(clientePF.getUsuarioDeletou()).isEqualTo("user2");
        }
    }

    @Nested
    @DisplayName("Gerenciamento de Documentos")
    class GerenciamentoDocumentosTests {

        @Test
        @DisplayName("Deve adicionar documento à lista")
        void deveAdicionarDocumento() {
            // Given
            Documento documento = Documento.builder()
                    .tipoDocumento(TipoDocumentoEnum.CPF)
                    .numero("12345678910")
                    .build();

            assertThat(clientePF.getListaDocumentos()).isEmpty();

            // When
            clientePF.adicionarDocumento(documento);

            // Then
            assertThat(clientePF.getListaDocumentos()).hasSize(1);
            assertThat(clientePF.getListaDocumentos()).contains(documento);
        }

        @Test
        @DisplayName("Deve remover documento da lista")
        void deveRemoverDocumento() {
            // Given
            Documento documento = Documento.builder()
                    .tipoDocumento(TipoDocumentoEnum.RG)
                    .numero("123456789")
                    .build();
            clientePF.adicionarDocumento(documento);
            assertThat(clientePF.getListaDocumentos()).hasSize(1);

            // When
            clientePF.removerDocumento(documento);

            // Then
            assertThat(clientePF.getListaDocumentos()).isEmpty();
        }

        @Test
        @DisplayName("Deve adicionar múltiplos documentos")
        void deveAdicionarMultiplosDocumentos() {
            // Given
            Documento doc1 = Documento.builder().tipoDocumento(TipoDocumentoEnum.CPF).numero("111").build();
            Documento doc2 = Documento.builder().tipoDocumento(TipoDocumentoEnum.RG).numero("222").build();
            Documento doc3 = Documento.builder().tipoDocumento(TipoDocumentoEnum.CNH).numero("333").build();

            // When
            clientePF.adicionarDocumento(doc1);
            clientePF.adicionarDocumento(doc2);
            clientePF.adicionarDocumento(doc3);

            // Then
            assertThat(clientePF.getListaDocumentos()).hasSize(3);
            assertThat(clientePF.getListaDocumentos()).containsExactly(doc1, doc2, doc3);
        }
    }

    @Nested
    @DisplayName("Gerenciamento de Contatos")
    class GerenciamentoContatosTests {

        @Test
        @DisplayName("Deve adicionar contato à lista")
        void deveAdicionarContato() {
            // Given
            Contato contato = Contato.builder()
                    .tipoContato(TipoContatoEnum.CELULAR)
                    .valor("11987654321")
                    .build();

            assertThat(clientePF.getListaContatos()).isEmpty();

            // When
            clientePF.adicionarContato(contato);

            // Then
            assertThat(clientePF.getListaContatos()).hasSize(1);
            assertThat(clientePF.getListaContatos()).contains(contato);
        }

        @Test
        @DisplayName("Deve remover contato da lista")
        void deveRemoverContato() {
            // Given
            Contato contato = Contato.builder()
                    .tipoContato(TipoContatoEnum.EMAIL)
                    .valor("test@example.com")
                    .build();
            clientePF.adicionarContato(contato);
            assertThat(clientePF.getListaContatos()).hasSize(1);

            // When
            clientePF.removerContato(contato);

            // Then
            assertThat(clientePF.getListaContatos()).isEmpty();
        }

        @Test
        @DisplayName("Deve adicionar múltiplos contatos")
        void deveAdicionarMultiplosContatos() {
            // Given
            Contato contato1 = Contato.builder().tipoContato(TipoContatoEnum.CELULAR).valor("111").build();
            Contato contato2 = Contato.builder().tipoContato(TipoContatoEnum.EMAIL).valor("test@test.com").build();

            // When
            clientePF.adicionarContato(contato1);
            clientePF.adicionarContato(contato2);

            // Then
            assertThat(clientePF.getListaContatos()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Gerenciamento de Endereços")
    class GerenciamentoEnderecosTests {

        @Test
        @DisplayName("Deve adicionar endereço à lista")
        void deveAdicionarEndereco() {
            // Given
            Endereco endereco = Endereco.builder()
                    .tipoEndereco(TipoEnderecoEnum.RESIDENCIAL)
                    .logradouro("Rua Teste")
                    .numero("123")
                    .cidade("São Paulo")
                    .cep("01234567")
                    .build();

            assertThat(clientePF.getListaEnderecos()).isEmpty();

            // When
            clientePF.adicionarEndereco(endereco);

            // Then
            assertThat(clientePF.getListaEnderecos()).hasSize(1);
            assertThat(clientePF.getListaEnderecos()).contains(endereco);
        }

        @Test
        @DisplayName("Deve remover endereço da lista")
        void deveRemoverEndereco() {
            // Given
            Endereco endereco = Endereco.builder()
                    .tipoEndereco(TipoEnderecoEnum.COMERCIAL)
                    .logradouro("Av Teste")
                    .build();
            clientePF.adicionarEndereco(endereco);
            assertThat(clientePF.getListaEnderecos()).hasSize(1);

            // When
            clientePF.removerEndereco(endereco);

            // Then
            assertThat(clientePF.getListaEnderecos()).isEmpty();
        }

        @Test
        @DisplayName("Deve adicionar múltiplos endereços")
        void deveAdicionarMultiplosEnderecos() {
            // Given
            Endereco end1 = Endereco.builder().tipoEndereco(TipoEnderecoEnum.RESIDENCIAL).logradouro("Rua 1").build();
            Endereco end2 = Endereco.builder().tipoEndereco(TipoEnderecoEnum.COMERCIAL).logradouro("Rua 2").build();

            // When
            clientePF.adicionarEndereco(end1);
            clientePF.adicionarEndereco(end2);

            // Then
            assertThat(clientePF.getListaEnderecos()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Gerenciamento de Dados Bancários")
    class GerenciamentoDadosBancariosTests {

        @Test
        @DisplayName("Deve adicionar dados bancários e estabelecer relacionamento bidirecional")
        void deveAdicionarDadosBancarios() {
            // Given
            DadosBancarios dados = DadosBancarios.builder()
                    .banco("001")
                    .agencia("1234")
                    .conta("56789-0")
                    .build();

            assertThat(clientePF.getListaDadosBancarios()).isEmpty();

            // When
            clientePF.adicionarDadosBancarios(dados);

            // Then
            assertThat(clientePF.getListaDadosBancarios()).hasSize(1);
            assertThat(clientePF.getListaDadosBancarios()).contains(dados);
            assertThat(dados.getCliente()).isEqualTo(clientePF); // Relacionamento bidirecional
        }

        @Test
        @DisplayName("Deve remover dados bancários e limpar relacionamento bidirecional")
        void deveRemoverDadosBancarios() {
            // Given
            DadosBancarios dados = DadosBancarios.builder()
                    .banco("237")
                    .agencia("5678")
                    .conta("12345-6")
                    .build();
            clientePF.adicionarDadosBancarios(dados);
            assertThat(clientePF.getListaDadosBancarios()).hasSize(1);
            assertThat(dados.getCliente()).isEqualTo(clientePF);

            // When
            clientePF.removerDadosBancarios(dados);

            // Then
            assertThat(clientePF.getListaDadosBancarios()).isEmpty();
            assertThat(dados.getCliente()).isNull(); // Relacionamento bidirecional limpo
        }

        @Test
        @DisplayName("Deve adicionar múltiplos dados bancários")
        void deveAdicionarMultiplosDadosBancarios() {
            // Given
            DadosBancarios dados1 = DadosBancarios.builder().banco("001").build();
            DadosBancarios dados2 = DadosBancarios.builder().banco("237").build();

            // When
            clientePF.adicionarDadosBancarios(dados1);
            clientePF.adicionarDadosBancarios(dados2);

            // Then
            assertThat(clientePF.getListaDadosBancarios()).hasSize(2);
            assertThat(dados1.getCliente()).isEqualTo(clientePF);
            assertThat(dados2.getCliente()).isEqualTo(clientePF);
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks - @PrePersist e @PreUpdate")
    class LifecycleCallbacksTests {

        @Test
        @DisplayName("onCreate() deve gerar publicId se null")
        void onCreateDeveGerarPublicIdSeNull() {
            // Given
            ClientePF cliente = ClientePF.builder()
                    .primeiroNome("Test")
                    .sobrenome("User")
                    .cpf("123")
                    .build();
            assertThat(cliente.getPublicId()).isNull();

            // When
            cliente.onCreate();

            // Then
            assertThat(cliente.getPublicId()).isNotNull();
            assertThat(cliente.getPublicId()).isInstanceOf(UUID.class);
        }

        @Test
        @DisplayName("onCreate() NÃO deve sobrescrever publicId existente")
        void onCreateNaoDeveSobrescreverPublicIdExistente() {
            // Given
            UUID publicIdOriginal = UUID.randomUUID();
            ClientePF cliente = ClientePF.builder()
                    .publicId(publicIdOriginal)
                    .primeiroNome("Test")
                    .sobrenome("User")
                    .cpf("123")
                    .build();

            // When
            cliente.onCreate();

            // Then
            assertThat(cliente.getPublicId()).isEqualTo(publicIdOriginal);
        }

        @Test
        @DisplayName("onCreate() deve definir dataCriacao")
        void onCreateDeveDefinirDataCriacao() {
            // Given
            ClientePF cliente = ClientePF.builder().primeiroNome("Test").sobrenome("User").cpf("123").build();
            assertThat(cliente.getDataCriacao()).isNull();

            LocalDateTime antes = LocalDateTime.now();

            // When
            cliente.onCreate();

            LocalDateTime depois = LocalDateTime.now();

            // Then
            assertThat(cliente.getDataCriacao()).isNotNull();
            assertThat(cliente.getDataCriacao()).isAfterOrEqualTo(antes);
            assertThat(cliente.getDataCriacao()).isBeforeOrEqualTo(depois);
        }

        @Test
        @DisplayName("onCreate() deve definir dataAtualizacao")
        void onCreateDeveDefinirDataAtualizacao() {
            // Given
            ClientePF cliente = ClientePF.builder().primeiroNome("Test").sobrenome("User").cpf("123").build();
            assertThat(cliente.getDataAtualizacao()).isNull();

            LocalDateTime antes = LocalDateTime.now();

            // When
            cliente.onCreate();

            LocalDateTime depois = LocalDateTime.now();

            // Then
            assertThat(cliente.getDataAtualizacao()).isNotNull();
            assertThat(cliente.getDataAtualizacao()).isAfterOrEqualTo(antes);
            assertThat(cliente.getDataAtualizacao()).isBeforeOrEqualTo(depois);
        }

        @Test
        @DisplayName("onCreate() deve definir dataCriacao e dataAtualizacao com mesmo valor")
        void onCreateDeveDefinirDataCriacaoEAtualizacaoIguais() {
            // Given
            ClientePF cliente = ClientePF.builder().primeiroNome("Test").sobrenome("User").cpf("123").build();

            // When
            cliente.onCreate();

            // Then
            assertThat(cliente.getDataCriacao()).isNotNull();
            assertThat(cliente.getDataAtualizacao()).isNotNull();
            // Podem diferir em milissegundos, mas devem ser muito próximas
            assertThat(cliente.getDataAtualizacao()).isAfterOrEqualTo(cliente.getDataCriacao());
        }

        @Test
        @DisplayName("onUpdate() deve atualizar dataAtualizacao")
        void onUpdateDeveAtualizarDataAtualizacao() {
            // Given
            ClientePF cliente = ClientePF.builder().primeiroNome("Test").sobrenome("User").cpf("123").build();
            cliente.onCreate();
            LocalDateTime dataAtualizacaoOriginal = cliente.getDataAtualizacao();

            // Simular passagem de tempo
            try {
                Thread.sleep(10); // 10ms
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // When
            cliente.onUpdate();

            // Then
            assertThat(cliente.getDataAtualizacao()).isNotNull();
            assertThat(cliente.getDataAtualizacao()).isAfter(dataAtualizacaoOriginal);
        }

        @Test
        @DisplayName("onUpdate() NÃO deve alterar dataCriacao")
        void onUpdateNaoDeveAlterarDataCriacao() {
            // Given
            ClientePF cliente = ClientePF.builder().primeiroNome("Test").sobrenome("User").cpf("123").build();
            cliente.onCreate();
            LocalDateTime dataCriacaoOriginal = cliente.getDataCriacao();

            // When
            cliente.onUpdate();

            // Then
            assertThat(cliente.getDataCriacao()).isEqualTo(dataCriacaoOriginal);
        }
    }

    @Nested
    @DisplayName("Edge Cases e Cenários Especiais")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve permitir deletar com motivo null")
        void devePermitirDeletarComMotivoNull() {
            // When
            clientePF.deletar(null, "user");

            // Then
            assertThat(clientePF.getAtivo()).isFalse();
            assertThat(clientePF.getMotivoDelecao()).isNull();
            assertThat(clientePF.getUsuarioDeletou()).isEqualTo("user");
        }

        @Test
        @DisplayName("Deve permitir deletar com usuário null")
        void devePermitirDeletarComUsuarioNull() {
            // When
            clientePF.deletar("Motivo teste", null);

            // Then
            assertThat(clientePF.getAtivo()).isFalse();
            assertThat(clientePF.getMotivoDelecao()).isEqualTo("Motivo teste");
            assertThat(clientePF.getUsuarioDeletou()).isNull();
        }

        @Test
        @DisplayName("Deve permitir restaurar com usuário não informado")
        void devePermitirRestaurarSemUsuario() {
            // Given
            clientePF.deletar("Teste", "admin");

            // When
            clientePF.restaurar(null);

            // Then
            assertThat(clientePF.getAtivo()).isTrue();
            assertThat(clientePF.getDataDelecao()).isNull();
        }

        @Test
        @DisplayName("Deve manter campos de marketing após soft delete")
        void deveManterCamposMarketingAposSoftDelete() {
            // Given
            clientePF.setOrigemLead(OrigemLeadEnum.GOOGLE_ADS);
            clientePF.setUtmSource("google");
            clientePF.setUtmCampaign("campanha-teste");

            // When
            clientePF.deletar("Teste", "user");

            // Then - Campos de marketing devem permanecer
            assertThat(clientePF.getOrigemLead()).isEqualTo(OrigemLeadEnum.GOOGLE_ADS);
            assertThat(clientePF.getUtmSource()).isEqualTo("google");
            assertThat(clientePF.getUtmCampaign()).isEqualTo("campanha-teste");
        }
    }
}
