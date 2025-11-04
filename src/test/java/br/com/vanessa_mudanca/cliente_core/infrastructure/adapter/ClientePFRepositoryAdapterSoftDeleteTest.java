package br.com.vanessa_mudanca.cliente_core.infrastructure.adapter;

import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.infrastructure.repository.ClientePFJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes para validar queries filtradas de soft delete no ClientePFRepositoryAdapter.
 * Verifica que métodos com sufixo "Active" retornam apenas registros não deletados.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ClientePFRepositoryAdapter - Testes de queries filtradas por soft delete")
class ClientePFRepositoryAdapterSoftDeleteTest {

    @Mock
    private ClientePFJpaRepository jpaRepository;

    @InjectMocks
    private ClientePFRepositoryAdapter adapter;

    private ClientePF clienteAtivo;
    private ClientePF clienteDeletado;
    private String cpfAtivo = "12345678909";
    private String cpfDeletado = "98765432100";
    private UUID publicIdAtivo;
    private UUID publicIdDeletado;

    @BeforeEach
    void setUp() {
        publicIdAtivo = UUID.randomUUID();
        publicIdDeletado = UUID.randomUUID();

        // Cliente ATIVO
        clienteAtivo = ClientePF.builder()
                .id(1L)
                .publicId(publicIdAtivo)
                .primeiroNome("João")
                .sobrenome("Silva")
                .cpf(cpfAtivo)
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .sexo(SexoEnum.MASCULINO)
                .email("joao.silva@email.com")
                .tipoCliente(TipoClienteEnum.COMPRADOR)
                .ativo(true)
                .bloqueado(false)
                .dataDelecao(null)
                .build();

        // Cliente DELETADO
        clienteDeletado = ClientePF.builder()
                .id(2L)
                .publicId(publicIdDeletado)
                .primeiroNome("Maria")
                .sobrenome("Santos")
                .cpf(cpfDeletado)
                .dataNascimento(LocalDate.of(1985, 5, 20))
                .sexo(SexoEnum.FEMININO)
                .email("maria.santos@email.com")
                .tipoCliente(TipoClienteEnum.CONSIGNANTE)
                .ativo(false)
                .bloqueado(false)
                .dataDelecao(LocalDateTime.now().minusDays(5))
                .motivoDelecao("Cliente solicitou exclusão")
                .usuarioDeletou("admin")
                .build();
    }

    // ========== TESTES DE findActiveByCpf ==========

    @Test
    @DisplayName("findActiveByCpf deve retornar cliente ativo quando CPF existe")
    void findActiveByCpfDeveRetornarClienteAtivoQuandoCpfExiste() {
        // Arrange
        when(jpaRepository.findByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfAtivo))
                .thenReturn(Optional.of(clienteAtivo));

        // Act
        Optional<ClientePF> resultado = adapter.findActiveByCpf(cpfAtivo);

        // Assert
        assertTrue(resultado.isPresent(), "Cliente ativo deve ser encontrado");
        assertEquals(cpfAtivo, resultado.get().getCpf(), "CPF deve ser o esperado");
        assertTrue(resultado.get().getAtivo(), "Cliente deve estar ativo");
        assertNull(resultado.get().getDataDelecao(), "Data de deleção deve ser nula");
        verify(jpaRepository).findByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfAtivo);
    }

    @Test
    @DisplayName("findActiveByCpf NÃO deve retornar cliente deletado")
    void findActiveByCpfNaoDeveRetornarClienteDeletado() {
        // Arrange
        when(jpaRepository.findByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfDeletado))
                .thenReturn(Optional.empty());

        // Act
        Optional<ClientePF> resultado = adapter.findActiveByCpf(cpfDeletado);

        // Assert
        assertFalse(resultado.isPresent(), "Cliente deletado NÃO deve ser retornado por findActive");
        verify(jpaRepository).findByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfDeletado);
    }

    @Test
    @DisplayName("findActiveByCpf deve usar query com filtro de soft delete")
    void findActiveByCpfDeveUsarQueryComFiltro() {
        // Act
        adapter.findActiveByCpf(cpfAtivo);

        // Assert
        verify(jpaRepository).findByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfAtivo);
        verify(jpaRepository, never()).findByCpf(cpfAtivo);
    }

    // ========== TESTES DE findActiveByPublicId ==========

    @Test
    @DisplayName("findActiveByPublicId deve retornar cliente ativo quando publicId existe")
    void findActiveByPublicIdDeveRetornarClienteAtivoQuandoPublicIdExiste() {
        // Arrange
        when(jpaRepository.findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(publicIdAtivo))
                .thenReturn(Optional.of(clienteAtivo));

        // Act
        Optional<ClientePF> resultado = adapter.findActiveByPublicId(publicIdAtivo);

        // Assert
        assertTrue(resultado.isPresent(), "Cliente ativo deve ser encontrado");
        assertEquals(publicIdAtivo, resultado.get().getPublicId(), "PublicId deve ser o esperado");
        assertTrue(resultado.get().getAtivo(), "Cliente deve estar ativo");
        assertNull(resultado.get().getDataDelecao(), "Data de deleção deve ser nula");
        verify(jpaRepository).findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(publicIdAtivo);
    }

    @Test
    @DisplayName("findActiveByPublicId NÃO deve retornar cliente deletado")
    void findActiveByPublicIdNaoDeveRetornarClienteDeletado() {
        // Arrange
        when(jpaRepository.findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(publicIdDeletado))
                .thenReturn(Optional.empty());

        // Act
        Optional<ClientePF> resultado = adapter.findActiveByPublicId(publicIdDeletado);

        // Assert
        assertFalse(resultado.isPresent(), "Cliente deletado NÃO deve ser retornado por findActive");
        verify(jpaRepository).findByPublicIdAndAtivoTrueAndDataDelecaoIsNull(publicIdDeletado);
    }

    // ========== TESTES DE existsActiveByCpf ==========

    @Test
    @DisplayName("existsActiveByCpf deve retornar true para cliente ativo")
    void existsActiveByCpfDeveRetornarTrueParaClienteAtivo() {
        // Arrange
        when(jpaRepository.existsByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfAtivo))
                .thenReturn(true);

        // Act
        boolean existe = adapter.existsActiveByCpf(cpfAtivo);

        // Assert
        assertTrue(existe, "Deve retornar true para cliente ativo");
        verify(jpaRepository).existsByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfAtivo);
    }

    @Test
    @DisplayName("existsActiveByCpf deve retornar false para cliente deletado")
    void existsActiveByCpfDeveRetornarFalseParaClienteDeletado() {
        // Arrange
        when(jpaRepository.existsByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfDeletado))
                .thenReturn(false);

        // Act
        boolean existe = adapter.existsActiveByCpf(cpfDeletado);

        // Assert
        assertFalse(existe, "Deve retornar false para cliente deletado");
        verify(jpaRepository).existsByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfDeletado);
    }

    // ========== TESTES DE COMPARAÇÃO: findByCpf vs findActiveByCpf ==========

    @Test
    @DisplayName("findByCpf deve retornar QUALQUER cliente (ativo ou deletado)")
    void findByCpfDeveRetornarQualquerCliente() {
        // Arrange
        when(jpaRepository.findByCpf(cpfDeletado))
                .thenReturn(Optional.of(clienteDeletado));

        // Act
        Optional<ClientePF> resultado = adapter.findByCpf(cpfDeletado);

        // Assert
        assertTrue(resultado.isPresent(), "findByCpf deve encontrar cliente mesmo deletado");
        assertFalse(resultado.get().getAtivo(), "Cliente encontrado está deletado");
        assertNotNull(resultado.get().getDataDelecao(), "Data de deleção está preenchida");
    }

    @Test
    @DisplayName("Mesmo CPF: findByCpf encontra deletado, findActiveByCpf NÃO encontra")
    void mesmoCpfComportamentoDiferente() {
        // Arrange
        when(jpaRepository.findByCpf(cpfDeletado))
                .thenReturn(Optional.of(clienteDeletado));
        when(jpaRepository.findByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfDeletado))
                .thenReturn(Optional.empty());

        // Act
        Optional<ClientePF> resultadoSemFiltro = adapter.findByCpf(cpfDeletado);
        Optional<ClientePF> resultadoComFiltro = adapter.findActiveByCpf(cpfDeletado);

        // Assert
        assertTrue(resultadoSemFiltro.isPresent(), "findByCpf deve encontrar cliente deletado");
        assertFalse(resultadoComFiltro.isPresent(), "findActiveByCpf NÃO deve encontrar cliente deletado");
        verify(jpaRepository).findByCpf(cpfDeletado);
        verify(jpaRepository).findByCpfAndAtivoTrueAndDataDelecaoIsNull(cpfDeletado);
    }
}
