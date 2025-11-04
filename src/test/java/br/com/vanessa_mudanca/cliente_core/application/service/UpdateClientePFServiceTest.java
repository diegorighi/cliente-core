package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePFRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateContatoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateDocumentoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateEnderecoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePFResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePFRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ContatoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.DocumentoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.EnderecoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePF;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Contato;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Documento;
import br.com.vanessa_mudanca.cliente_core.domain.entity.Endereco;
import br.com.vanessa_mudanca.cliente_core.domain.enums.*;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ClienteNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.ContatoNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.DocumentoNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.exception.EnderecoNaoEncontradoException;
import br.com.vanessa_mudanca.cliente_core.domain.validator.ValidarContatoPrincipalUnicoStrategy;
import br.com.vanessa_mudanca.cliente_core.domain.validator.ValidarDataValidadeStrategy;
import br.com.vanessa_mudanca.cliente_core.domain.validator.ValidarEnderecoPrincipalUnicoStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UpdateClientePFService.
 * Valida todos os cenários de atualização de cliente PF e suas entidades relacionadas.
 *
 * Cenários cobertos:
 * - Atualização de dados básicos do cliente
 * - Atualização seletiva de documentos
 * - Atualização seletiva de endereços
 * - Atualização seletiva de contatos
 * - Validação de propriedade (cross-client ownership)
 * - Validação de principal único
 * - Tratamento de erros (cliente não encontrado, entidades não encontradas)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateClientePFService - Testes de atualização de cliente PF")
class UpdateClientePFServiceTest {

    @Mock
    private ClientePFRepositoryPort clientePFRepository;

    @Mock
    private DocumentoRepositoryPort documentoRepository;

    @Mock
    private EnderecoRepositoryPort enderecoRepository;

    @Mock
    private ContatoRepositoryPort contatoRepository;

    @Mock
    private ValidarDataValidadeStrategy validadorDataValidade;

    @Mock
    private ValidarEnderecoPrincipalUnicoStrategy validadorEnderecoPrincipal;

    @Mock
    private ValidarContatoPrincipalUnicoStrategy validadorContatoPrincipal;

    @InjectMocks
    private UpdateClientePFService service;

    private UUID publicId;
    private ClientePF clienteExistente;
    private Documento documentoExistente;
    private Endereco enderecoExistente;
    private Contato contatoExistente;

    @BeforeEach
    void setUp() {
        publicId = UUID.randomUUID();

        // Cliente existente no banco
        clienteExistente = ClientePF.builder()
                .id(1L)
                .publicId(publicId)
                .primeiroNome("João")
                .nomeDoMeio("da")
                .sobrenome("Silva")
                .cpf("12345678909")
                .rg("MG-12.345.678")
                .dataNascimento(LocalDate.of(1990, 1, 15))
                .sexo(SexoEnum.MASCULINO)
                .email("joao.silva@email.com")
                .nomeMae("Maria da Silva")
                .nomePai("José da Silva")
                .estadoCivil("Casado")
                .profissao("Engenheiro")
                .nacionalidade("Brasileira")
                .naturalidade("Belo Horizonte")
                .tipoCliente(TipoClienteEnum.COMPRADOR)
                .ativo(true)
                .bloqueado(false)
                .build();

        // Documento existente
        documentoExistente = Documento.builder()
                .id(100L)
                .cliente(clienteExistente)
                .tipoDocumento(TipoDocumentoEnum.CPF)
                .numero("12345678909")
                .dataEmissao(LocalDate.of(2010, 1, 1))
                .dataValidade(LocalDate.of(2030, 1, 1))
                .orgaoEmissor("SSP/MG")
                .documentoPrincipal(true)
                .statusDocumento(StatusDocumentoEnum.VALIDO)
                .build();

        // Endereço existente
        enderecoExistente = Endereco.builder()
                .id(200L)
                .cliente(clienteExistente)
                .tipoEndereco(TipoEnderecoEnum.RESIDENCIAL)
                .cep("30140071")
                .logradouro("Av. Afonso Pena")
                .numero("1234")
                .bairro("Centro")
                .cidade("Belo Horizonte")
                .estado(EstadoEnum.MG)
                .pais("Brasil")
                .enderecoPrincipal(true)
                .build();

        // Contato existente
        contatoExistente = Contato.builder()
                .id(300L)
                .cliente(clienteExistente)
                .tipoContato(TipoContatoEnum.CELULAR)
                .valor("31987654321")
                .contatoPrincipal(true)
                .verificado(false)
                .build();
    }

    // ========== CENÁRIOS DE SUCESSO - DADOS BÁSICOS ==========

    @Test
    @DisplayName("Deve atualizar apenas dados básicos do cliente quando não há entidades relacionadas no request")
    void deveAtualizarApenasDadosBasicos() {
        // Arrange
        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .primeiroNome("Carlos")
                .sobrenome("Santos")
                .email("carlos.santos@email.com")
                .tipoCliente(TipoClienteEnum.CONSIGNANTE)
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        // Act
        ClientePFResponse response = service.atualizar(request);

        // Assert
        assertNotNull(response);
        verify(clientePFRepository, times(1)).findByPublicId(publicId);
        verify(clientePFRepository, times(1)).save(clienteExistente);

        // Não deve tentar atualizar entidades relacionadas
        verify(documentoRepository, never()).findById(any());
        verify(enderecoRepository, never()).findById(any());
        verify(contatoRepository, never()).findById(any());

        assertEquals("Carlos", clienteExistente.getPrimeiroNome());
        assertEquals("Santos", clienteExistente.getSobrenome());
        assertEquals("carlos.santos@email.com", clienteExistente.getEmail());
        assertEquals(TipoClienteEnum.CONSIGNANTE, clienteExistente.getTipoCliente());
    }

    @Test
    @DisplayName("Deve atualizar dados complementares do cliente")
    void deveAtualizarDadosComplementares() {
        // Arrange
        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .estadoCivil("Divorciado")
                .profissao("Arquiteto")
                .observacoes("Cliente VIP")
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        assertEquals("Divorciado", clienteExistente.getEstadoCivil());
        assertEquals("Arquiteto", clienteExistente.getProfissao());
        assertEquals("Cliente VIP", clienteExistente.getObservacoes());
    }

    // ========== CENÁRIOS DE SUCESSO - DOCUMENTOS ==========

    @Test
    @DisplayName("Deve atualizar documento existente com sucesso")
    void deveAtualizarDocumentoExistente() {
        // Arrange
        LocalDate novaDataEmissao = LocalDate.of(2020, 5, 10);
        LocalDate novaDataValidade = LocalDate.of(2035, 5, 10);

        UpdateDocumentoDTO documentoDTO = UpdateDocumentoDTO.builder()
                .id(100L)
                .dataEmissao(novaDataEmissao)
                .dataValidade(novaDataValidade)
                .orgaoEmissor("SSP/SP")
                .observacoes("Segunda via")
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .documentos(List.of(documentoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(documentoRepository.findById(100L)).thenReturn(Optional.of(documentoExistente));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documentoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        doNothing().when(validadorDataValidade).validar(any(UpdateDocumentoDTO.class));

        // Act
        service.atualizar(request);

        // Assert
        verify(documentoRepository, times(1)).findById(100L);
        verify(validadorDataValidade, times(1)).validar(documentoDTO);
        verify(documentoRepository, times(1)).save(documentoExistente);

        assertEquals(novaDataEmissao, documentoExistente.getDataEmissao());
        assertEquals(novaDataValidade, documentoExistente.getDataValidade());
        assertEquals("SSP/SP", documentoExistente.getOrgaoEmissor());
        assertEquals("Segunda via", documentoExistente.getObservacoes());
    }

    @Test
    @DisplayName("Deve lançar DocumentoNaoEncontradoException quando documento não existe")
    void deveLancarExcecaoQuandoDocumentoNaoExiste() {
        // Arrange
        UpdateDocumentoDTO documentoDTO = UpdateDocumentoDTO.builder()
                .id(999L) // ID inexistente
                .dataEmissao(LocalDate.now())
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .documentos(List.of(documentoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(documentoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentoNaoEncontradoException.class, () -> service.atualizar(request));
        verify(documentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando documento pertence a outro cliente")
    void deveLancarExcecaoQuandoDocumentoPertenceAOutroCliente() {
        // Arrange
        ClientePF outroCliente = ClientePF.builder()
                .id(999L) // ID diferente
                .publicId(UUID.randomUUID())
                .build();

        Documento documentoDeOutroCliente = Documento.builder()
                .id(100L)
                .cliente(outroCliente) // Pertence a outro cliente
                .tipoDocumento(TipoDocumentoEnum.RG)
                .build();

        UpdateDocumentoDTO documentoDTO = UpdateDocumentoDTO.builder()
                .id(100L)
                .dataEmissao(LocalDate.now())
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .documentos(List.of(documentoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(documentoRepository.findById(100L)).thenReturn(Optional.of(documentoDeOutroCliente));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.atualizar(request)
        );

        assertTrue(exception.getMessage().contains("Documento"));
        assertTrue(exception.getMessage().contains("não pertence ao cliente"));
        verify(documentoRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE SUCESSO - ENDEREÇOS ==========

    @Test
    @DisplayName("Deve atualizar endereço existente com sucesso")
    void deveAtualizarEnderecoExistente() {
        // Arrange
        UpdateEnderecoDTO enderecoDTO = UpdateEnderecoDTO.builder()
                .id(200L)
                .cep("01310100")
                .logradouro("Av. Paulista")
                .numero("1578")
                .complemento("Andar 10")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado(EstadoEnum.SP)
                .pais("Brasil")
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .enderecos(List.of(enderecoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(enderecoRepository.findById(200L)).thenReturn(Optional.of(enderecoExistente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        verify(enderecoRepository, times(1)).findById(200L);
        verify(enderecoRepository, times(1)).save(enderecoExistente);

        assertEquals("01310100", enderecoExistente.getCep());
        assertEquals("Av. Paulista", enderecoExistente.getLogradouro());
        assertEquals("1578", enderecoExistente.getNumero());
        assertEquals("Andar 10", enderecoExistente.getComplemento());
        assertEquals("São Paulo", enderecoExistente.getCidade());
        assertEquals(EstadoEnum.SP, enderecoExistente.getEstado());
    }

    @Test
    @DisplayName("Deve marcar endereço como principal quando enderecoPrincipal = true")
    void deveMarcarEnderecoComoPrincipal() {
        // Arrange
        UpdateEnderecoDTO enderecoDTO = UpdateEnderecoDTO.builder()
                .id(200L)
                .tipoEndereco(TipoEnderecoEnum.RESIDENCIAL)
                .enderecoPrincipal(true)
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .enderecos(List.of(enderecoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(enderecoRepository.findById(200L)).thenReturn(Optional.of(enderecoExistente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        doNothing().when(validadorEnderecoPrincipal).validar(any(), any(), any(), anyBoolean());

        // Act
        service.atualizar(request);

        // Assert
        verify(validadorEnderecoPrincipal, times(1)).validar(
                clienteExistente.getId(),
                200L,
                TipoEnderecoEnum.RESIDENCIAL,
                true
        );
        assertTrue(enderecoExistente.getEnderecoPrincipal());
    }

    @Test
    @DisplayName("Deve usar tipoEndereco da entidade quando DTO não especifica tipo")
    void deveUsarTipoEnderecoExistenteQuandoDTONaoEspecifica() {
        // Arrange - DTO sem tipoEndereco, apenas marcando como principal
        UpdateEnderecoDTO enderecoDTO = UpdateEnderecoDTO.builder()
                .id(200L)
                .tipoEndereco(null) // Não especifica tipo
                .enderecoPrincipal(true)
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .enderecos(List.of(enderecoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(enderecoRepository.findById(200L)).thenReturn(Optional.of(enderecoExistente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        doNothing().when(validadorEnderecoPrincipal).validar(any(), any(), any(), anyBoolean());

        // Act
        service.atualizar(request);

        // Assert - Deve usar o tipo da entidade existente (RESIDENCIAL)
        verify(validadorEnderecoPrincipal, times(1)).validar(
                clienteExistente.getId(),
                200L,
                TipoEnderecoEnum.RESIDENCIAL, // Tipo da entidade existente
                true
        );
    }

    @Test
    @DisplayName("Deve lançar EnderecoNaoEncontradoException quando endereço não existe")
    void deveLancarExcecaoQuandoEnderecoNaoExiste() {
        // Arrange
        UpdateEnderecoDTO enderecoDTO = UpdateEnderecoDTO.builder()
                .id(999L) // ID inexistente
                .cep("01310100")
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .enderecos(List.of(enderecoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(enderecoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EnderecoNaoEncontradoException.class, () -> service.atualizar(request));
        verify(enderecoRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE SUCESSO - CONTATOS ==========

    @Test
    @DisplayName("Deve atualizar contato existente com sucesso")
    void deveAtualizarContatoExistente() {
        // Arrange
        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(300L)
                .tipoContato(TipoContatoEnum.EMAIL)
                .valor("novo.email@example.com")
                .observacoes("Email comercial")
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.save(any(Contato.class))).thenReturn(contatoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        verify(contatoRepository, times(1)).findById(300L);
        verify(contatoRepository, times(1)).save(contatoExistente);

        assertEquals(TipoContatoEnum.EMAIL, contatoExistente.getTipoContato());
        assertEquals("novo.email@example.com", contatoExistente.getValor());
        assertEquals("Email comercial", contatoExistente.getObservacoes());
    }

    @Test
    @DisplayName("Deve marcar contato como principal quando contatoPrincipal = true")
    void deveMarcarContatoComoPrincipal() {
        // Arrange
        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(300L)
                .contatoPrincipal(true)
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.save(any(Contato.class))).thenReturn(contatoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        doNothing().when(validadorContatoPrincipal).validar(any(), any(), anyBoolean());

        // Act
        service.atualizar(request);

        // Assert
        verify(validadorContatoPrincipal, times(1)).validar(
                clienteExistente.getId(),
                300L,
                true
        );
        assertTrue(contatoExistente.getContatoPrincipal());
    }

    @Test
    @DisplayName("Deve remover flag principal quando contatoPrincipal = false")
    void deveRemoverFlagPrincipalDoContato() {
        // Arrange
        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(300L)
                .contatoPrincipal(false)
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.save(any(Contato.class))).thenReturn(contatoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        doNothing().when(validadorContatoPrincipal).validar(any(), any(), anyBoolean());

        // Act
        service.atualizar(request);

        // Assert
        verify(validadorContatoPrincipal, times(1)).validar(
                clienteExistente.getId(),
                300L,
                false
        );
        assertFalse(contatoExistente.getContatoPrincipal());
    }

    @Test
    @DisplayName("Deve lançar ContatoNaoEncontradoException quando contato não existe")
    void deveLancarExcecaoQuandoContatoNaoExiste() {
        // Arrange
        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(999L) // ID inexistente
                .valor("novo.email@example.com")
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ContatoNaoEncontradoException.class, () -> service.atualizar(request));
        verify(contatoRepository, never()).save(any());
    }

    // ========== CENÁRIOS DE ERRO - CLIENTE NÃO ENCONTRADO ==========

    @Test
    @DisplayName("Deve lançar ClienteNaoEncontradoException quando cliente não existe")
    void deveLancarExcecaoQuandoClienteNaoExiste() {
        // Arrange
        UUID publicIdInexistente = UUID.randomUUID();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicIdInexistente)
                .primeiroNome("João")
                .build();

        when(clientePFRepository.findByPublicId(publicIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNaoEncontradoException.class, () -> service.atualizar(request));
        verify(clientePFRepository, never()).save(any());
    }

    // ========== CENÁRIOS INTEGRADOS ==========

    @Test
    @DisplayName("Deve atualizar cliente + documentos + endereços + contatos em uma única transação")
    void deveAtualizarTodasEntidadesEmUmaUnicaTransacao() {
        // Arrange
        UpdateDocumentoDTO documentoDTO = UpdateDocumentoDTO.builder()
                .id(100L)
                .orgaoEmissor("SSP/RJ")
                .build();

        UpdateEnderecoDTO enderecoDTO = UpdateEnderecoDTO.builder()
                .id(200L)
                .cidade("Rio de Janeiro")
                .build();

        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(300L)
                .valor("21987654321")
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .primeiroNome("Pedro")
                .sobrenome("Oliveira")
                .documentos(List.of(documentoDTO))
                .enderecos(List.of(enderecoDTO))
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(documentoRepository.findById(100L)).thenReturn(Optional.of(documentoExistente));
        when(enderecoRepository.findById(200L)).thenReturn(Optional.of(enderecoExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(documentoRepository.save(any())).thenReturn(documentoExistente);
        when(enderecoRepository.save(any())).thenReturn(enderecoExistente);
        when(contatoRepository.save(any())).thenReturn(contatoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        doNothing().when(validadorDataValidade).validar(any());

        // Act
        ClientePFResponse response = service.atualizar(request);

        // Assert
        assertNotNull(response);

        // Verifica que todas as entidades foram atualizadas
        assertEquals("Pedro", clienteExistente.getPrimeiroNome());
        assertEquals("Oliveira", clienteExistente.getSobrenome());
        assertEquals("SSP/RJ", documentoExistente.getOrgaoEmissor());
        assertEquals("Rio de Janeiro", enderecoExistente.getCidade());
        assertEquals("21987654321", contatoExistente.getValor());

        // Verifica que todas as operações de save foram chamadas
        verify(documentoRepository, times(1)).save(documentoExistente);
        verify(enderecoRepository, times(1)).save(enderecoExistente);
        verify(contatoRepository, times(1)).save(contatoExistente);
        verify(clientePFRepository, times(1)).save(clienteExistente);
    }

    @Test
    @DisplayName("Deve atualizar apenas entidades presentes no request (selective update)")
    void deveAtualizarApenasEntidadesPresentes() {
        // Arrange - Request SEM documentos e endereços, apenas contatos
        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(300L)
                .valor("21987654321")
                .build();

        UpdateClientePFRequest request = UpdateClientePFRequest.builder()
                .publicId(publicId)
                .primeiroNome("Pedro")
                .contatos(List.of(contatoDTO))
                // documentos e enderecos são null
                .build();

        when(clientePFRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.save(any())).thenReturn(contatoExistente);
        when(clientePFRepository.save(any(ClientePF.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        verify(contatoRepository, times(1)).save(contatoExistente);

        // Não deve tentar atualizar documentos e endereços
        verify(documentoRepository, never()).findById(any());
        verify(enderecoRepository, never()).findById(any());
        verify(documentoRepository, never()).save(any());
        verify(enderecoRepository, never()).save(any());
    }
}
