package br.com.vanessa_mudanca.cliente_core.application.service;

import java.util.UUID;

import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.enums.SexoEnum;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para FindClientePFByIdService.
 * Valida o uso de Optional e programação funcional.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FindClientePFByIdService - Testes")
class FindClientePFByIdServiceTest {

    @Mock
    private ClientePFRepositoryPort clientePFRepository;

    @InjectMocks
    private FindClientePFByIdService service;

    private ClientePF clientePF;

    private UUID publicId;

    @BeforeEach
    void setUp() {
        publicId = UUID.randomUUID();
        clientePF = ClientePF.builder()
                .id(1L)
                .publicId(publicId)
                .primeiroNome("João")
                .nomeDoMeio("da")
                .sobrenome("Silva")
                .cpf("12345678909")
                .rg("MG-12.345.678")
                .dataNascimento(LocalDate.of(1990, 5, 15))
                .sexo(SexoEnum.MASCULINO)
                .nomeMae("Maria da Silva")
                .nomePai("José da Silva")
                .estadoCivil("Casado")
                .profissao("Engenheiro")
                .nacionalidade("Brasileira")
                .naturalidade("Belo Horizonte")
                .build();
    }

    @Test
    @DisplayName("Deve retornar ClientePFResponse quando cliente existe")
    void deveRetornarClientePFResponse_QuandoClienteExiste() {
        // Given
        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clientePF));

        // When
        ClientePFResponse response = service.findByPublicId(publicId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.publicId()).isEqualTo(publicId);
        assertThat(response.primeiroNome()).isEqualTo("João");
        assertThat(response.nomeDoMeio()).isEqualTo("da");
        assertThat(response.sobrenome()).isEqualTo("Silva");
        assertThat(response.nomeCompleto()).isEqualTo("João da Silva");
        assertThat(response.cpf()).isEqualTo("12345678909");
        assertThat(response.dataNascimento()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(response.sexo()).isEqualTo(SexoEnum.MASCULINO);

        verify(clientePFRepository, times(1)).findByPublicId(publicId);
    }

    @Test
    @DisplayName("Deve lançar ClienteNaoEncontradoException quando cliente não existe")
    void deveLancarClienteNaoEncontradoException_QuandoClienteNaoExiste() {
        // Given
        UUID idInexistente = UUID.randomUUID();
        when(clientePFRepository.findByPublicId(idInexistente)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> service.findByPublicId(idInexistente))
                .isInstanceOf(ClienteNaoEncontradoException.class)
                .hasMessageContaining("não encontrado");

        verify(clientePFRepository, times(1)).findByPublicId(idInexistente);
    }

    @Test
    @DisplayName("Deve calcular idade corretamente a partir da data de nascimento")
    void deveCalcularIdadeCorretamente() {
        // Given
        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clientePF));

        // When
        ClientePFResponse response = service.findByPublicId(publicId);

        // Then
        assertThat(response.idade()).isGreaterThanOrEqualTo(33); // Cliente nasceu em 1990
        verify(clientePFRepository, times(1)).findByPublicId(publicId);
    }

    @Test
    @DisplayName("Deve retornar nome completo corretamente")
    void deveRetornarNomeCompletoCorretamente() {
        // Given
        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clientePF));

        // When
        ClientePFResponse response = service.findByPublicId(publicId);

        // Then
        assertThat(response.nomeCompleto()).isEqualTo("João da Silva");

        verify(clientePFRepository, times(1)).findByPublicId(publicId);
    }

    @Test
    @DisplayName("Deve retornar nome completo sem nome do meio quando não informado")
    void deveRetornarNomeCompletoSemNomeDoMeio() {
        // Given
        UUID publicId2 = UUID.randomUUID();
        ClientePF clienteSemNomeDoMeio = ClientePF.builder()
                .id(2L)
                .publicId(publicId2)
                .primeiroNome("Maria")
                .sobrenome("Santos")
                .cpf("98765432100")
                .dataNascimento(LocalDate.of(1985, 3, 20))
                .sexo(SexoEnum.FEMININO)
                .build();

        when(clientePFRepository.findByPublicId(publicId2)).thenReturn(Optional.of(clienteSemNomeDoMeio));

        // When
        ClientePFResponse response = service.findByPublicId(publicId2);

        // Then
        assertThat(response.nomeCompleto()).isEqualTo("Maria Santos");
        verify(clientePFRepository, times(1)).findByPublicId(publicId2);
    }

    @Test
    @DisplayName("Deve retornar idade nula quando data de nascimento não informada")
    void deveRetornarIdadeNula_QuandoDataNascimentoNaoInformada() {
        // Given
        UUID publicId3 = UUID.randomUUID();
        ClientePF clienteSemDataNascimento = ClientePF.builder()
                .id(3L)
                .publicId(publicId3)
                .primeiroNome("José")
                .sobrenome("Oliveira")
                .cpf("11122233344")
                .sexo(SexoEnum.MASCULINO)
                .build();

        when(clientePFRepository.findByPublicId(publicId3)).thenReturn(Optional.of(clienteSemDataNascimento));

        // When
        ClientePFResponse response = service.findByPublicId(publicId3);

        // Then
        assertThat(response.idade()).isNull();
        verify(clientePFRepository, times(1)).findByPublicId(publicId3);
    }
}
