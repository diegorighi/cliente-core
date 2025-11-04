package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteJaDeletadoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para DeleteClienteService.
 * Valida soft delete e restauração de clientes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteClienteService - Testes de soft delete e restauração")
class DeleteClienteServiceTest {

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @InjectMocks
    private DeleteClienteService service;

    private ClientePF clienteAtivo;
    private ClientePF clienteDeletado;
    private UUID publicId;

    @BeforeEach
    void setUp() {
        publicId = UUID.randomUUID();

        // Cliente ATIVO (não deletado)
        clienteAtivo = ClientePF.builder()
                .id(1L)
                .publicId(publicId)
                .primeiroNome("João")
                .nomeDoMeio("da")
                .sobrenome("Silva")
                .cpf("12345678909")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .sexo(SexoEnum.MASCULINO)
                .email("joao.silva@email.com")
                .tipoCliente(TipoClienteEnum.COMPRADOR)
                .ativo(true)
                .bloqueado(false)
                .dataDelecao(null)
                .motivoDelecao(null)
                .usuarioDeletou(null)
                .build();

        // Cliente DELETADO (soft delete)
        clienteDeletado = ClientePF.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .primeiroNome("Maria")
                .sobrenome("Santos")
                .cpf("98765432100")
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

    // ========== CENÁRIOS DE SUCESSO - DELETAR ==========

    @Test
    @DisplayName("Deve deletar cliente ativo com sucesso")
    void deveDeletarClienteAtivoComSucesso() {
        // Arrange
        String motivo = "Cliente solicitou exclusão de conta";
        String usuario = "system-admin";

        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteAtivo));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.deletar(publicId, motivo, usuario);

        // Assert
        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());

        Cliente clienteSalvo = clienteCaptor.getValue();
        assertFalse(clienteSalvo.getAtivo(), "Cliente deve estar inativo após deleção");
        assertNotNull(clienteSalvo.getDataDelecao(), "Data de deleção deve estar preenchida");
        assertEquals(motivo, clienteSalvo.getMotivoDelecao(), "Motivo de deleção deve estar correto");
        assertEquals(usuario, clienteSalvo.getUsuarioDeletou(), "Usuário que deletou deve estar correto");
        assertTrue(clienteSalvo.isDeletado(), "isDeletado() deve retornar true");
    }

    @Test
    @DisplayName("Deve registrar data e hora da deleção")
    void deveRegistrarDataHoraDaDeleção() {
        // Arrange
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteAtivo));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        // Act
        service.deletar(publicId, "Teste", "admin");

        // Assert
        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());

        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);
        LocalDateTime dataDelecao = clienteCaptor.getValue().getDataDelecao();

        assertNotNull(dataDelecao, "Data de deleção não deve ser nula");
        assertTrue(dataDelecao.isAfter(antes) && dataDelecao.isBefore(depois),
                "Data de deleção deve estar no intervalo de execução do teste");
    }

    // ========== CENÁRIOS DE ERRO - DELETAR ==========

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar cliente não encontrado")
    void deveLancarExcecaoAoDeletarClienteNaoEncontrado() {
        // Arrange
        UUID publicIdInexistente = UUID.randomUUID();
        when(clienteRepository.findByPublicId(publicIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNaoEncontradoException.class,
                () -> service.deletar(publicIdInexistente, "Teste", "admin"),
                "Deve lançar ClienteNaoEncontradoException");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar deletar cliente já deletado")
    void deveLancarExcecaoAoDeletarClienteJaDeletado() {
        // Arrange
        UUID publicIdDeletado = clienteDeletado.getPublicId();
        when(clienteRepository.findByPublicId(publicIdDeletado)).thenReturn(Optional.of(clienteDeletado));

        // Act & Assert
        ClienteJaDeletadoException exception = assertThrows(ClienteJaDeletadoException.class,
                () -> service.deletar(publicIdDeletado, "Novo motivo", "admin"),
                "Deve lançar ClienteJaDeletadoException");

        assertEquals(publicIdDeletado, exception.getPublicId(), "PublicId na exceção deve estar correto");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve verificar se cliente foi deletado antes de deletar novamente")
    void deveVerificarSeClienteFoiDeletadoAntesDeDeletarNovamente() {
        // Arrange
        UUID publicIdDeletado = clienteDeletado.getPublicId();
        when(clienteRepository.findByPublicId(publicIdDeletado)).thenReturn(Optional.of(clienteDeletado));

        // Act & Assert
        assertThrows(ClienteJaDeletadoException.class,
                () -> service.deletar(publicIdDeletado, "Motivo", "admin"));

        verify(clienteRepository).findByPublicId(publicIdDeletado);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    // ========== CENÁRIOS DE SUCESSO - RESTAURAR ==========

    @Test
    @DisplayName("Deve restaurar cliente deletado com sucesso")
    void deveRestaurarClienteDeletadoComSucesso() {
        // Arrange
        String usuario = "system-admin";
        UUID publicIdDeletado = clienteDeletado.getPublicId();

        when(clienteRepository.findByPublicId(publicIdDeletado)).thenReturn(Optional.of(clienteDeletado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.restaurar(publicIdDeletado, usuario);

        // Assert
        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());

        Cliente clienteRestaurado = clienteCaptor.getValue();
        assertTrue(clienteRestaurado.getAtivo(), "Cliente deve estar ativo após restauração");
        assertNull(clienteRestaurado.getDataDelecao(), "Data de deleção deve ser nula após restauração");
        assertNull(clienteRestaurado.getMotivoDelecao(), "Motivo de deleção deve ser nulo após restauração");
        assertNull(clienteRestaurado.getUsuarioDeletou(), "Usuário que deletou deve ser nulo após restauração");
        assertFalse(clienteRestaurado.isDeletado(), "isDeletado() deve retornar false");
    }

    @Test
    @DisplayName("Deve restaurar cliente mesmo que já esteja ativo")
    void deveRestaurarClienteMesmoQueJaEstejaAtivo() {
        // Arrange
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteAtivo));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.restaurar(publicId, "admin");

        // Assert
        verify(clienteRepository).save(any(Cliente.class));
    }

    // ========== CENÁRIOS DE ERRO - RESTAURAR ==========

    @Test
    @DisplayName("Deve lançar exceção ao tentar restaurar cliente não encontrado")
    void deveLancarExcecaoAoRestaurarClienteNaoEncontrado() {
        // Arrange
        UUID publicIdInexistente = UUID.randomUUID();
        when(clienteRepository.findByPublicId(publicIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNaoEncontradoException.class,
                () -> service.restaurar(publicIdInexistente, "admin"),
                "Deve lançar ClienteNaoEncontradoException");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    // ========== TESTES DE INTEGRAÇÃO COM DOMAIN ==========

    @Test
    @DisplayName("Deve usar método deletar() da entidade Cliente")
    void deveUsarMetodoDeletarDaEntidade() {
        // Arrange
        ClientePF clienteSpy = spy(clienteAtivo);
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteSpy));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.deletar(publicId, "Motivo teste", "admin");

        // Assert
        verify(clienteSpy).deletar("Motivo teste", "admin");
    }

    @Test
    @DisplayName("Deve usar método restaurar() da entidade Cliente")
    void deveUsarMetodoRestaurarDaEntidade() {
        // Arrange
        UUID publicIdDeletado = clienteDeletado.getPublicId();
        ClientePF clienteSpy = spy(clienteDeletado);
        when(clienteRepository.findByPublicId(publicIdDeletado)).thenReturn(Optional.of(clienteSpy));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.restaurar(publicIdDeletado, "admin");

        // Assert
        verify(clienteSpy).restaurar("admin");
    }

    // ========== TESTES DE VERIFICAÇÕES ==========

    @Test
    @DisplayName("Deve buscar cliente antes de deletar")
    void deveBuscarClienteAntesDeDeletar() {
        // Arrange
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteAtivo));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.deletar(publicId, "Motivo", "admin");

        // Assert
        verify(clienteRepository).findByPublicId(publicId);
    }

    @Test
    @DisplayName("Deve buscar cliente antes de restaurar")
    void deveBuscarClienteAntesDeRestaurar() {
        // Arrange
        UUID publicIdDeletado = clienteDeletado.getPublicId();
        when(clienteRepository.findByPublicId(publicIdDeletado)).thenReturn(Optional.of(clienteDeletado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.restaurar(publicIdDeletado, "admin");

        // Assert
        verify(clienteRepository).findByPublicId(publicIdDeletado);
    }
}
