package br.com.vanessa_mudanca.cliente_core.application.service;

import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateClientePJRequest;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateContatoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateDocumentoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.input.UpdateEnderecoDTO;
import br.com.vanessa_mudanca.cliente_core.application.dto.output.ClientePJResponse;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ClientePJRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.ContatoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.DocumentoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.application.ports.output.EnderecoRepositoryPort;
import br.com.vanessa_mudanca.cliente_core.domain.entity.ClientePJ;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para UpdateClientePJService.
 * Valida todos os cenários de atualização de cliente PJ e suas entidades relacionadas.
 *
 * Cenários cobertos:
 * - Atualização de dados básicos da empresa
 * - Atualização de dados de classificação (porte, natureza jurídica, etc.)
 * - Atualização de dados do responsável
 * - Atualização seletiva de documentos
 * - Atualização seletiva de endereços
 * - Atualização seletiva de contatos
 * - Validação de propriedade (cross-client ownership)
 * - Validação de principal único
 * - Tratamento de erros (cliente não encontrado, entidades não encontradas)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateClientePJService - Testes de atualização de cliente PJ")
class UpdateClientePJServiceTest {

    @Mock
    private ClientePJRepositoryPort clientePJRepository;

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
    private UpdateClientePJService service;

    private UUID publicId;
    private ClientePJ clienteExistente;
    private Documento documentoExistente;
    private Endereco enderecoExistente;
    private Contato contatoExistente;

    @BeforeEach
    void setUp() {
        publicId = UUID.randomUUID();

        // Cliente PJ existente no banco
        clienteExistente = ClientePJ.builder()
                .id(1L)
                .publicId(publicId)
                .razaoSocial("Tech Solutions Ltda")
                .nomeFantasia("TechSol")
                .cnpj("12345678000195")
                .inscricaoEstadual("123456789")
                .inscricaoMunicipal("987654321")
                .dataAbertura(LocalDate.of(2015, 5, 10))
                .email("contato@techsol.com.br")
                .porteEmpresa("MEDIA")
                .naturezaJuridica("LTDA")
                .atividadePrincipal("Desenvolvimento de Software")
                .capitalSocial(new BigDecimal("500000.00"))
                .nomeResponsavel("Carlos Silva")
                .cpfResponsavel("12345678909")
                .cargoResponsavel("Diretor Geral")
                .site("www.techsol.com.br")
                .tipoCliente(TipoClienteEnum.COMPRADOR)
                .ativo(true)
                .bloqueado(false)
                .build();

        // Documento existente
        documentoExistente = Documento.builder()
                .id(100L)
                .cliente(clienteExistente)
                .tipoDocumento(TipoDocumentoEnum.CNPJ)
                .numero("12345678000195")
                .dataEmissao(LocalDate.of(2015, 5, 10))
                .orgaoEmissor("Receita Federal")
                .documentoPrincipal(true)
                .statusDocumento(StatusDocumentoEnum.VALIDO)
                .build();

        // Endereço existente
        enderecoExistente = Endereco.builder()
                .id(200L)
                .cliente(clienteExistente)
                .tipoEndereco(TipoEnderecoEnum.COMERCIAL)
                .cep("30140071")
                .logradouro("Av. Afonso Pena")
                .numero("1500")
                .complemento("Sala 1201")
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
    @DisplayName("Deve atualizar apenas dados básicos da empresa quando não há entidades relacionadas no request")
    void deveAtualizarApenasDadosBasicos() {
        // Arrange
        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .razaoSocial("Tech Solutions Brasil Ltda")
                .nomeFantasia("TechSol Brasil")
                .email("contato@techsolbrasil.com.br")
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        // Act
        ClientePJResponse response = service.atualizar(request);

        // Assert
        assertNotNull(response);
        verify(clientePJRepository, times(1)).findByPublicId(publicId);
        verify(clientePJRepository, times(1)).save(clienteExistente);

        // Não deve tentar atualizar entidades relacionadas
        verify(documentoRepository, never()).findById(any());
        verify(enderecoRepository, never()).findById(any());
        verify(contatoRepository, never()).findById(any());

        assertEquals("Tech Solutions Brasil Ltda", clienteExistente.getRazaoSocial());
        assertEquals("TechSol Brasil", clienteExistente.getNomeFantasia());
        assertEquals("contato@techsolbrasil.com.br", clienteExistente.getEmail());
    }

    @Test
    @DisplayName("Deve atualizar dados de classificação da empresa")
    void deveAtualizarDadosClassificacao() {
        // Arrange
        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .porteEmpresa("GRANDE")
                .naturezaJuridica("S/A")
                .atividadePrincipal("Consultoria em TI")
                .capitalSocial(new BigDecimal("2000000.00"))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        assertEquals("GRANDE", clienteExistente.getPorteEmpresa());
        assertEquals("S/A", clienteExistente.getNaturezaJuridica());
        assertEquals("Consultoria em TI", clienteExistente.getAtividadePrincipal());
        assertEquals(new BigDecimal("2000000.00"), clienteExistente.getCapitalSocial());
    }

    @Test
    @DisplayName("Deve atualizar dados do responsável legal da empresa")
    void deveAtualizarDadosResponsavel() {
        // Arrange
        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .nomeResponsavel("Ana Paula Costa")
                .cpfResponsavel("98765432100")
                .cargoResponsavel("CEO")
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        assertEquals("Ana Paula Costa", clienteExistente.getNomeResponsavel());
        assertEquals("98765432100", clienteExistente.getCpfResponsavel());
        assertEquals("CEO", clienteExistente.getCargoResponsavel());
    }

    @Test
    @DisplayName("Deve atualizar informações adicionais (site, tipoCliente, observações)")
    void deveAtualizarInformacoesAdicionais() {
        // Arrange
        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .site("www.newtechsol.com.br")
                .tipoCliente(TipoClienteEnum.CONSIGNANTE)
                .observacoes("Cliente premium - desconto especial")
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        assertEquals("www.newtechsol.com.br", clienteExistente.getSite());
        assertEquals(TipoClienteEnum.CONSIGNANTE, clienteExistente.getTipoCliente());
        assertEquals("Cliente premium - desconto especial", clienteExistente.getObservacoes());
    }

    // ========== CENÁRIOS DE SUCESSO - DOCUMENTOS ==========

    @Test
    @DisplayName("Deve atualizar documento existente com sucesso")
    void deveAtualizarDocumentoExistente() {
        // Arrange
        LocalDate novaDataEmissao = LocalDate.of(2020, 1, 15);

        UpdateDocumentoDTO documentoDTO = UpdateDocumentoDTO.builder()
                .id(100L)
                .dataEmissao(novaDataEmissao)
                .orgaoEmissor("Receita Federal - 2ª Via")
                .observacoes("Documento atualizado")
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .documentos(List.of(documentoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(documentoRepository.findById(100L)).thenReturn(Optional.of(documentoExistente));
        when(documentoRepository.save(any(Documento.class))).thenReturn(documentoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        doNothing().when(validadorDataValidade).validar(any(UpdateDocumentoDTO.class));

        // Act
        service.atualizar(request);

        // Assert
        verify(documentoRepository, times(1)).findById(100L);
        verify(validadorDataValidade, times(1)).validar(documentoDTO);
        verify(documentoRepository, times(1)).save(documentoExistente);

        assertEquals(novaDataEmissao, documentoExistente.getDataEmissao());
        assertEquals("Receita Federal - 2ª Via", documentoExistente.getOrgaoEmissor());
        assertEquals("Documento atualizado", documentoExistente.getObservacoes());
    }

    @Test
    @DisplayName("Deve lançar DocumentoNaoEncontradoException quando documento não existe")
    void deveLancarExcecaoQuandoDocumentoNaoExiste() {
        // Arrange
        UpdateDocumentoDTO documentoDTO = UpdateDocumentoDTO.builder()
                .id(999L) // ID inexistente
                .dataEmissao(LocalDate.now())
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .documentos(List.of(documentoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(documentoRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(DocumentoNaoEncontradoException.class, () -> service.atualizar(request));
        verify(documentoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException quando documento pertence a outro cliente")
    void deveLancarExcecaoQuandoDocumentoPertenceAOutroCliente() {
        // Arrange
        ClientePJ outroCliente = ClientePJ.builder()
                .id(999L) // ID diferente
                .publicId(UUID.randomUUID())
                .build();

        Documento documentoDeOutroCliente = Documento.builder()
                .id(100L)
                .cliente(outroCliente) // Pertence a outro cliente
                .tipoDocumento(TipoDocumentoEnum.CNPJ)
                .build();

        UpdateDocumentoDTO documentoDTO = UpdateDocumentoDTO.builder()
                .id(100L)
                .dataEmissao(LocalDate.now())
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .documentos(List.of(documentoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
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
                .numero("2000")
                .complemento("Conjunto 1502")
                .bairro("Bela Vista")
                .cidade("São Paulo")
                .estado(EstadoEnum.SP)
                .pais("Brasil")
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .enderecos(List.of(enderecoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(enderecoRepository.findById(200L)).thenReturn(Optional.of(enderecoExistente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        verify(enderecoRepository, times(1)).findById(200L);
        verify(enderecoRepository, times(1)).save(enderecoExistente);

        assertEquals("01310100", enderecoExistente.getCep());
        assertEquals("Av. Paulista", enderecoExistente.getLogradouro());
        assertEquals("2000", enderecoExistente.getNumero());
        assertEquals("Conjunto 1502", enderecoExistente.getComplemento());
        assertEquals("São Paulo", enderecoExistente.getCidade());
        assertEquals(EstadoEnum.SP, enderecoExistente.getEstado());
    }

    @Test
    @DisplayName("Deve marcar endereço como principal quando enderecoPrincipal = true")
    void deveMarcarEnderecoComoPrincipal() {
        // Arrange
        UpdateEnderecoDTO enderecoDTO = UpdateEnderecoDTO.builder()
                .id(200L)
                .tipoEndereco(TipoEnderecoEnum.COMERCIAL)
                .enderecoPrincipal(true)
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .enderecos(List.of(enderecoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(enderecoRepository.findById(200L)).thenReturn(Optional.of(enderecoExistente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        doNothing().when(validadorEnderecoPrincipal).validar(any(), any(), any(), anyBoolean());

        // Act
        service.atualizar(request);

        // Assert
        verify(validadorEnderecoPrincipal, times(1)).validar(
                clienteExistente.getId(),
                200L,
                TipoEnderecoEnum.COMERCIAL,
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

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .enderecos(List.of(enderecoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(enderecoRepository.findById(200L)).thenReturn(Optional.of(enderecoExistente));
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(enderecoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        doNothing().when(validadorEnderecoPrincipal).validar(any(), any(), any(), anyBoolean());

        // Act
        service.atualizar(request);

        // Assert - Deve usar o tipo da entidade existente (COMERCIAL)
        verify(validadorEnderecoPrincipal, times(1)).validar(
                clienteExistente.getId(),
                200L,
                TipoEnderecoEnum.COMERCIAL, // Tipo da entidade existente
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

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .enderecos(List.of(enderecoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
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
                .valor("contato@techsolbrasil.com.br")
                .observacoes("Email corporativo")
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.save(any(Contato.class))).thenReturn(contatoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        verify(contatoRepository, times(1)).findById(300L);
        verify(contatoRepository, times(1)).save(contatoExistente);

        assertEquals(TipoContatoEnum.EMAIL, contatoExistente.getTipoContato());
        assertEquals("contato@techsolbrasil.com.br", contatoExistente.getValor());
        assertEquals("Email corporativo", contatoExistente.getObservacoes());
    }

    @Test
    @DisplayName("Deve marcar contato como principal quando contatoPrincipal = true")
    void deveMarcarContatoComoPrincipal() {
        // Arrange
        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(300L)
                .contatoPrincipal(true)
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.save(any(Contato.class))).thenReturn(contatoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

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

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.save(any(Contato.class))).thenReturn(contatoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

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
                .valor("novo@email.com")
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
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

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicIdInexistente)
                .razaoSocial("Nova Empresa")
                .build();

        when(clientePJRepository.findByPublicId(publicIdInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ClienteNaoEncontradoException.class, () -> service.atualizar(request));
        verify(clientePJRepository, never()).save(any());
    }

    // ========== CENÁRIOS INTEGRADOS ==========

    @Test
    @DisplayName("Deve atualizar cliente + documentos + endereços + contatos em uma única transação")
    void deveAtualizarTodasEntidadesEmUmaUnicaTransacao() {
        // Arrange
        UpdateDocumentoDTO documentoDTO = UpdateDocumentoDTO.builder()
                .id(100L)
                .orgaoEmissor("Receita Federal - Atualizado")
                .build();

        UpdateEnderecoDTO enderecoDTO = UpdateEnderecoDTO.builder()
                .id(200L)
                .cidade("Rio de Janeiro")
                .build();

        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(300L)
                .valor("21987654321")
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .razaoSocial("Tech Solutions Brasil Ltda")
                .porteEmpresa("GRANDE")
                .nomeResponsavel("Ana Paula Costa")
                .documentos(List.of(documentoDTO))
                .enderecos(List.of(enderecoDTO))
                .contatos(List.of(contatoDTO))
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(documentoRepository.findById(100L)).thenReturn(Optional.of(documentoExistente));
        when(enderecoRepository.findById(200L)).thenReturn(Optional.of(enderecoExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(documentoRepository.save(any())).thenReturn(documentoExistente);
        when(enderecoRepository.save(any())).thenReturn(enderecoExistente);
        when(contatoRepository.save(any())).thenReturn(contatoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        doNothing().when(validadorDataValidade).validar(any());

        // Act
        ClientePJResponse response = service.atualizar(request);

        // Assert
        assertNotNull(response);

        // Verifica que todas as entidades foram atualizadas
        assertEquals("Tech Solutions Brasil Ltda", clienteExistente.getRazaoSocial());
        assertEquals("GRANDE", clienteExistente.getPorteEmpresa());
        assertEquals("Ana Paula Costa", clienteExistente.getNomeResponsavel());
        assertEquals("Receita Federal - Atualizado", documentoExistente.getOrgaoEmissor());
        assertEquals("Rio de Janeiro", enderecoExistente.getCidade());
        assertEquals("21987654321", contatoExistente.getValor());

        // Verifica que todas as operações de save foram chamadas
        verify(documentoRepository, times(1)).save(documentoExistente);
        verify(enderecoRepository, times(1)).save(enderecoExistente);
        verify(contatoRepository, times(1)).save(contatoExistente);
        verify(clientePJRepository, times(1)).save(clienteExistente);
    }

    @Test
    @DisplayName("Deve atualizar apenas entidades presentes no request (selective update)")
    void deveAtualizarApenasEntidadesPresentes() {
        // Arrange - Request SEM documentos e endereços, apenas dados básicos e contatos
        UpdateContatoDTO contatoDTO = UpdateContatoDTO.builder()
                .id(300L)
                .valor("21987654321")
                .build();

        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .razaoSocial("Tech Solutions Brasil Ltda")
                .contatos(List.of(contatoDTO))
                // documentos e enderecos são null
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(contatoRepository.findById(300L)).thenReturn(Optional.of(contatoExistente));
        when(contatoRepository.save(any())).thenReturn(contatoExistente);
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

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

    @Test
    @DisplayName("Deve atualizar apenas dados de classificação sem tocar em outras entidades")
    void deveAtualizarApenasDadosClassificacaoSemTocarEmOutrasEntidades() {
        // Arrange - Request com APENAS dados de classificação
        UpdateClientePJRequest request = UpdateClientePJRequest.builder()
                .publicId(publicId)
                .porteEmpresa("GRANDE")
                .naturezaJuridica("S/A")
                .atividadePrincipal("Consultoria")
                .capitalSocial(new BigDecimal("3000000.00"))
                // Sem dados básicos, responsável, documentos, endereços, contatos
                .build();

        when(clientePJRepository.findByPublicId(publicId)).thenReturn(Optional.of(clienteExistente));
        when(clientePJRepository.save(any(ClientePJ.class))).thenReturn(clienteExistente);

        // Act
        service.atualizar(request);

        // Assert
        // Apenas dados de classificação devem ser atualizados
        assertEquals("GRANDE", clienteExistente.getPorteEmpresa());
        assertEquals("S/A", clienteExistente.getNaturezaJuridica());
        assertEquals("Consultoria", clienteExistente.getAtividadePrincipal());
        assertEquals(new BigDecimal("3000000.00"), clienteExistente.getCapitalSocial());

        // Dados básicos permanecem inalterados
        assertEquals("Tech Solutions Ltda", clienteExistente.getRazaoSocial());
        assertEquals("TechSol", clienteExistente.getNomeFantasia());

        // Não deve tocar em entidades relacionadas
        verify(documentoRepository, never()).findById(any());
        verify(enderecoRepository, never()).findById(any());
        verify(contatoRepository, never()).findById(any());
    }
}
