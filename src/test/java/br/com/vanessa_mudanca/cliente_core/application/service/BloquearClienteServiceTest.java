package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClienteRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Cliente;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.enums.TipoClienteEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteJaBloqueadoException;
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
 * Testes unitários para BloquearClienteService.
 * Valida bloqueio e desbloqueio de clientes.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BloquearClienteService - Testes de bloqueio e desbloqueio")
class BloquearClienteServiceTest {

    @Mock
    private ClienteRepositoryPort clienteRepository;

    @InjectMocks
    private BloquearClienteService service;

    private ClientePF clienteDesbloqueado;
    private ClientePF clienteBloqueado;
    private UUID publicId;

    @BeforeEach
    void setUp() {
        publicId = UUID.randomUUID();

        // Cliente DESBLOQUEADO (ativo, não bloqueado)
        clienteDesbloqueado = ClientePF.builder()
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
                .motivoBloqueio(null)
                .dataBloqueio(null)
                .usuarioBloqueou(null)
                .build();

        // Cliente BLOQUEADO
        clienteBloqueado = ClientePF.builder()
                .id(2L)
                .publicId(UUID.randomUUID())
                .primeiroNome("Maria")
                .sobrenome("Santos")
                .cpf("98765432100")
                .dataNascimento(LocalDate.of(1985, 5, 20))
                .sexo(SexoEnum.FEMININO)
                .email("maria.santos@email.com")
                .tipoCliente(TipoClienteEnum.CONSIGNANTE)
                .ativo(true)
                .bloqueado(true)
                .motivoBloqueio("Atividade suspeita detectada")
                .dataBloqueio(LocalDateTime.now().minusDays(3))
                .usuarioBloqueou("admin@sistema.com")
                .build();
    }

    // ========== CENÁRIOS DE SUCESSO - BLOQUEAR ==========

    @Test
    @DisplayName("Deve bloquear cliente desbloqueado com sucesso")
    void deveBloquearClienteDesbloqueadoComSucesso() {
        // Arrange
        String motivo = "Cliente apresentou comportamento fraudulento";
        String usuario = "security-team@sistema.com";

        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteDesbloqueado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.bloquear(publicId, motivo, usuario);

        // Assert
        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());

        Cliente clienteSalvo = clienteCaptor.getValue();
        assertTrue(clienteSalvo.getBloqueado(), "Cliente deve estar bloqueado");
        assertNotNull(clienteSalvo.getDataBloqueio(), "Data de bloqueio deve estar preenchida");
        assertEquals(motivo, clienteSalvo.getMotivoBloqueio(), "Motivo de bloqueio deve estar correto");
        assertEquals(usuario, clienteSalvo.getUsuarioBloqueou(), "Usuário que bloqueou deve estar correto");
        assertTrue(clienteSalvo.isBloqueado(), "isBloqueado() deve retornar true");
    }

    @Test
    @DisplayName("Deve registrar data e hora do bloqueio")
    void deveRegistrarDataHoraDoBloqueio() {
        // Arrange
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteDesbloqueado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        // Act
        service.bloquear(publicId, "Fraude detectada", "security@sistema.com");

        // Assert
        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());

        LocalDateTime depois = LocalDateTime.now().plusSeconds(1);
        LocalDateTime dataBloqueio = clienteCaptor.getValue().getDataBloqueio();

        assertNotNull(dataBloqueio, "Data de bloqueio não deve ser nula");
        assertTrue(dataBloqueio.isAfter(antes) && dataBloqueio.isBefore(depois),
                "Data de bloqueio deve estar no intervalo de execução do teste");
    }

    // ========== CENÁRIOS DE ERRO - BLOQUEAR ==========

    @Test
    @DisplayName("Deve lançar exceção ao tentar bloquear cliente não encontrado")
    void deveLancarExcecaoAoBloquearClienteNaoEncontrado() {
        // Arrange
        UUID publicIdInexistente = UUID.randomUUID();
        when(clienteRepository.findByPublicId(publicIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNaoEncontradoException.class,
                () -> service.bloquear(publicIdInexistente, "Motivo", "admin"),
                "Deve lançar ClienteNaoEncontradoException");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar bloquear cliente já bloqueado")
    void deveLancarExcecaoAoBloquearClienteJaBloqueado() {
        // Arrange
        UUID publicIdBloqueado = clienteBloqueado.getPublicId();
        when(clienteRepository.findByPublicId(publicIdBloqueado)).thenReturn(Optional.of(clienteBloqueado));

        // Act & Assert
        ClienteJaBloqueadoException exception = assertThrows(ClienteJaBloqueadoException.class,
                () -> service.bloquear(publicIdBloqueado, "Novo motivo", "admin"),
                "Deve lançar ClienteJaBloqueadoException");

        assertEquals(publicIdBloqueado, exception.getPublicId(), "PublicId na exceção deve estar correto");
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Deve verificar se cliente foi bloqueado antes de bloquear novamente")
    void deveVerificarSeClienteFoiBloqueadoAntesDeBloquearNovamente() {
        // Arrange
        UUID publicIdBloqueado = clienteBloqueado.getPublicId();
        when(clienteRepository.findByPublicId(publicIdBloqueado)).thenReturn(Optional.of(clienteBloqueado));

        // Act & Assert
        assertThrows(ClienteJaBloqueadoException.class,
                () -> service.bloquear(publicIdBloqueado, "Motivo", "admin"));

        verify(clienteRepository).findByPublicId(publicIdBloqueado);
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    // ========== CENÁRIOS DE SUCESSO - DESBLOQUEAR ==========

    @Test
    @DisplayName("Deve desbloquear cliente bloqueado com sucesso")
    void deveDesbloquearClienteBloqueadoComSucesso() {
        // Arrange
        UUID publicIdBloqueado = clienteBloqueado.getPublicId();

        when(clienteRepository.findByPublicId(publicIdBloqueado)).thenReturn(Optional.of(clienteBloqueado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.desbloquear(publicIdBloqueado);

        // Assert
        ArgumentCaptor<Cliente> clienteCaptor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(clienteCaptor.capture());

        Cliente clienteDesbloqueado = clienteCaptor.getValue();
        assertFalse(clienteDesbloqueado.getBloqueado(), "Cliente não deve estar bloqueado");
        assertNull(clienteDesbloqueado.getMotivoBloqueio(), "Motivo de bloqueio deve ser nulo após desbloqueio");
        assertNull(clienteDesbloqueado.getDataBloqueio(), "Data de bloqueio deve ser nula após desbloqueio");
        assertNull(clienteDesbloqueado.getUsuarioBloqueou(), "Usuário que bloqueou deve ser nulo após desbloqueio");
        assertFalse(clienteDesbloqueado.isBloqueado(), "isBloqueado() deve retornar false");
    }

    @Test
    @DisplayName("Deve desbloquear cliente mesmo que já esteja desbloqueado")
    void deveDesbloquearClienteMesmoQueJaEstejaDesbloqueado() {
        // Arrange
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteDesbloqueado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.desbloquear(publicId);

        // Assert
        verify(clienteRepository).save(any(Cliente.class));
    }

    // ========== CENÁRIOS DE ERRO - DESBLOQUEAR ==========

    @Test
    @DisplayName("Deve lançar exceção ao tentar desbloquear cliente não encontrado")
    void deveLancarExcecaoAoDesbloquearClienteNaoEncontrado() {
        // Arrange
        UUID publicIdInexistente = UUID.randomUUID();
        when(clienteRepository.findByPublicId(publicIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNaoEncontradoException.class,
                () -> service.desbloquear(publicIdInexistente),
                "Deve lançar ClienteNaoEncontradoException");

        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    // ========== TESTES DE INTEGRAÇÃO COM DOMAIN ==========

    @Test
    @DisplayName("Deve usar método bloquear() da entidade Cliente")
    void deveUsarMetodoBloquearDaEntidade() {
        // Arrange
        ClientePF clienteSpy = spy(clienteDesbloqueado);
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteSpy));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.bloquear(publicId, "Motivo teste", "admin");

        // Assert
        verify(clienteSpy).bloquear("Motivo teste", "admin");
    }

    @Test
    @DisplayName("Deve usar método desbloquear() da entidade Cliente")
    void deveUsarMetodoDesbloquearDaEntidade() {
        // Arrange
        UUID publicIdBloqueado = clienteBloqueado.getPublicId();
        ClientePF clienteSpy = spy(clienteBloqueado);
        when(clienteRepository.findByPublicId(publicIdBloqueado)).thenReturn(Optional.of(clienteSpy));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.desbloquear(publicIdBloqueado);

        // Assert
        verify(clienteSpy).desbloquear();
    }

    // ========== TESTES DE VERIFICAÇÕES ==========

    @Test
    @DisplayName("Deve buscar cliente antes de bloquear")
    void deveBuscarClienteAntesDeBloquear() {
        // Arrange
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteDesbloqueado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.bloquear(publicId, "Motivo", "admin");

        // Assert
        verify(clienteRepository).findByPublicId(publicId);
    }

    @Test
    @DisplayName("Deve buscar cliente antes de desbloquear")
    void deveBuscarClienteAntesDeDesbloquear() {
        // Arrange
        UUID publicIdBloqueado = clienteBloqueado.getPublicId();
        when(clienteRepository.findByPublicId(publicIdBloqueado)).thenReturn(Optional.of(clienteBloqueado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        service.desbloquear(publicIdBloqueado);

        // Assert
        verify(clienteRepository).findByPublicId(publicIdBloqueado);
    }

    // ========== TESTES DE COMPORTAMENTO COMBINADO ==========

    @Test
    @DisplayName("Deve permitir bloquear, desbloquear e bloquear novamente o mesmo cliente")
    void devePermitirBloquearDesbloquearEBloquearNovamente() {
        // Arrange
        when(clienteRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteDesbloqueado));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert - Bloquear
        service.bloquear(publicId, "Primeira bloqueio", "admin1");
        assertTrue(clienteDesbloqueado.isBloqueado(), "Cliente deve estar bloqueado após primeiro bloqueio");

        // Desbloquear
        service.desbloquear(publicId);
        assertFalse(clienteDesbloqueado.isBloqueado(), "Cliente não deve estar bloqueado após desbloqueio");

        // Bloquear novamente
        service.bloquear(publicId, "Segundo bloqueio", "admin2");
        assertTrue(clienteDesbloqueado.isBloqueado(), "Cliente deve estar bloqueado após segundo bloqueio");
        assertEquals("Segundo bloqueio", clienteDesbloqueado.getMotivoBloqueio(),
                "Motivo deve refletir o segundo bloqueio");
    }
}
