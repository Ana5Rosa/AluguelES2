package com.mycompany.app;

import com.mycompany.app.handlers.CartaoCreditoHandler;
import com.mycompany.app.models.CartaoCredito;
import com.mycompany.app.models.Ciclista;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static io.javalin.http.HttpStatus.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartaoCreditoTest {

    private Context mockCtx;
    private final UUID ciclistaId = UUID.randomUUID();
    private final CartaoCredito mockCartao = mock(CartaoCredito.class);
    private final Ciclista mockCiclista = mock(Ciclista.class);
    private final long validadeTimestampFutura = new Date().getTime() + 100000000;
    private final String corpoJsonValido = String.format("{\"nomeTitular\": \"Nome Completo\", \"numero\": \"1234567890123456\", \"validade\": %d, \"cvv\": \"123\"}", validadeTimestampFutura);

    @BeforeEach
    void setUp() {
        mockCtx = mock(Context.class);
        // Configuração essencial para Fluent Interface do Javalin
        when(mockCtx.status(any(io.javalin.http.HttpStatus.class))).thenReturn(mockCtx);

        // Configurações padrão para a maioria dos testes
        when(mockCtx.pathParam("idCiclista")).thenReturn(ciclistaId.toString());
        when(mockCtx.body()).thenReturn(corpoJsonValido);
    }

    // Método auxiliar para mockar todas as validações com sucesso
    private void mockValidacoesSucesso(MockedStatic<CartaoCredito> mockedCartao) {
        mockedCartao.when(() -> CartaoCredito.validarNomeTitular(anyString())).thenReturn(true);
        mockedCartao.when(() -> CartaoCredito.validarNumero(anyString())).thenReturn(true);
        mockedCartao.when(() -> CartaoCredito.validarCVV(anyString())).thenReturn(true);
        mockedCartao.when(() -> CartaoCredito.validarValidade(any(Date.class))).thenReturn(true);
    }

    // ----------------------------------------------------------------------
    // Testes para recuperarCartaoPorCiclistaId (GET /cartaoDeCredito/{idCiclista})
    // ----------------------------------------------------------------------

    @Test
    @DisplayName("GET /cartaoDeCredito/{idCiclista} - Sucesso: Cartão Encontrado (200 OK)")
    void recuperarCartaoPorCiclistaId_Encontrado() throws Exception {
        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class);
             MockedStatic<CartaoCredito> mockedCartao = Mockito.mockStatic(CartaoCredito.class)) {

            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(mockCiclista);
            mockedCartao.when(() -> CartaoCredito.getCartaoCreditoPorCiclistaId(ciclistaId)).thenReturn(mockCartao);

            CartaoCreditoHandler.recuperarCartaoPorCiclistaId.handle(mockCtx);

            verify(mockCtx).status(OK);
            verify(mockCtx).json(mockCartao);
        }
    }

    @Test
    @DisplayName("GET /cartaoDeCredito/{idCiclista} - Falha: Cartão Não Cadastrado (404 NOT FOUND)")
    void recuperarCartaoPorCiclistaId_CartaoNaoCadastrado() throws Exception {
        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class);
             MockedStatic<CartaoCredito> mockedCartao = Mockito.mockStatic(CartaoCredito.class)) {

            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(mockCiclista);
            mockedCartao.when(() -> CartaoCredito.getCartaoCreditoPorCiclistaId(ciclistaId)).thenReturn(null);

            CartaoCreditoHandler.recuperarCartaoPorCiclistaId.handle(mockCtx);

            verify(mockCtx).status(NOT_FOUND);
            verify(mockCtx).result(anyString());
        }
    }

    // ------------------------------------------------------------------------
    // Testes para alterarCartaoPorCiclistaId (PUT /cartaoDeCredito/{idCiclista})
    // ------------------------------------------------------------------------

    @Test
    @DisplayName("PUT /cartaoDeCredito/{idCiclista} - Sucesso: Cartão Atualizado (200 OK)")
    void alterarCartaoPorCiclistaId_AtualizacaoSucesso() throws Exception {
        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class);
             MockedStatic<CartaoCredito> mockedCartao = Mockito.mockStatic(CartaoCredito.class)) {

            // Pre-condições
            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(mockCiclista);
            mockValidacoesSucesso(mockedCartao);

            // Simula que o Cartão JÁ EXISTIA (o método alterar retorna o objeto mockado)
            mockedCartao.when(() -> CartaoCredito.alterarDadosCartao(any(UUID.class), anyString(), anyString(), any(Date.class), anyString()))
                    .thenReturn(mockCartao);

            CartaoCreditoHandler.alterarCartaoPorCiclistaId.handle(mockCtx);

            verify(mockCtx).status(OK);
            verify(mockCtx).json(mockCartao);
        }
    }

    @Test
    @DisplayName("PUT /cartaoDeCredito/{idCiclista} - Sucesso: Cartão Criado (200 OK)")
    void alterarCartaoPorCiclistaId_CriacaoSucesso() throws Exception {

        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class);
             MockedStatic<CartaoCredito> mockedCartao = Mockito.mockStatic(CartaoCredito.class);
             MockedConstruction<CartaoCredito> mockedConstruction = Mockito.mockConstruction(CartaoCredito.class)) {

            // Pre-condições
            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(mockCiclista);
            mockValidacoesSucesso(mockedCartao);

            // Simula que o Cartão NÃO EXISTIA (retorna null, forçando o 'new CartaoCredito')
            mockedCartao.when(() -> CartaoCredito.alterarDadosCartao(any(UUID.class), anyString(), anyString(), any(Date.class), anyString()))
                    .thenReturn(null);

            // Execução
            CartaoCreditoHandler.alterarCartaoPorCiclistaId.handle(mockCtx);

            // Verificações
            // 1. Verifica se apenas 1 objeto foi construído
            List<CartaoCredito> constructed = mockedConstruction.constructed();
            assert constructed.size() == 1;

            // 2. Verifica se o status é OK e se o JSON retornado é o objeto construído
            verify(mockCtx).status(OK);
            verify(mockCtx).json(constructed.get(0));
        }
    }

    // --- Testes de Falha Específicos do Handler (Validações) ---

    @Test
    @DisplayName("PUT /cartaoDeCredito/{idCiclista} - Falha: Ciclista Não Encontrado (404)")
    void alterarCartaoPorCiclistaId_CiclistaNaoEncontrado() throws Exception {
        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class)) {
            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(null);

            CartaoCreditoHandler.alterarCartaoPorCiclistaId.handle(mockCtx);

            verify(mockCtx).status(NOT_FOUND);
            verify(mockCtx).result("Ciclista não encontrado.");
        }
    }

    @Test
    @DisplayName("PUT /cartaoDeCredito/{idCiclista} - Falha: Validação de Nome (422)")
    void alterarCartaoPorCiclistaId_ValidacaoNomeFalha() throws Exception {
        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class);
             MockedStatic<CartaoCredito> mockedCartao = Mockito.mockStatic(CartaoCredito.class)) {

            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(mockCiclista);
            mockValidacoesSucesso(mockedCartao);
            // Simula falha específica
            mockedCartao.when(() -> CartaoCredito.validarNomeTitular(anyString())).thenReturn(false);

            CartaoCreditoHandler.alterarCartaoPorCiclistaId.handle(mockCtx);

            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result("Nome do titular inválido (deve ser completo).");
        }
    }

    @Test
    @DisplayName("PUT /cartaoDeCredito/{idCiclista} - Falha: Validação de Validade (422)")
    void alterarCartaoPorCiclistaId_ValidacaoValidadeFalha() throws Exception {
        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class);
             MockedStatic<CartaoCredito> mockedCartao = Mockito.mockStatic(CartaoCredito.class)) {

            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(mockCiclista);
            mockValidacoesSucesso(mockedCartao);
            // Simula falha específica
            mockedCartao.when(() -> CartaoCredito.validarValidade(any(Date.class))).thenReturn(false);

            CartaoCreditoHandler.alterarCartaoPorCiclistaId.handle(mockCtx);

            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result("Validade do cartão inválida (deve ser no futuro).");
        }
    }

    // --- Testes de Exceção ---

    @Test
    @DisplayName("PUT /cartaoDeCredito/{idCiclista} - Falha: ID Ciclista Inválido (422)")
    void alterarCartaoPorCiclistaId_IDCiclistaInvalido() throws Exception {
        when(mockCtx.pathParam("idCiclista")).thenReturn("nao-e-uuid");

        CartaoCreditoHandler.alterarCartaoPorCiclistaId.handle(mockCtx);

        verify(mockCtx).status(UNPROCESSABLE_CONTENT);
        verify(mockCtx).result("ID de ciclista inválido (deve ser um UUID válido).");
    }

    @Test
    @DisplayName("PUT /cartaoDeCredito/{idCiclista} - Falha: Timestamp Inválido (422)")
    void alterarCartaoPorCiclistaId_TimestampInvalido() throws Exception {
        // Envia uma String onde deveria ser um Long (timestamp)
        String jsonInvalido = "{\"nomeTitular\": \"Nome\", \"numero\": \"123\", \"validade\": \"inválido\", \"cvv\": \"123\"}";
        when(mockCtx.body()).thenReturn(jsonInvalido);

        // 🛠️ CORREÇÃO: Mockar o Ciclista para garantir que passe da verificação de 404
        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class)) {
            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(mockCiclista);

            CartaoCreditoHandler.alterarCartaoPorCiclistaId.handle(mockCtx);

            // Handler captura ClassCastException ou erro de parse do Jackson
            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result(anyString());
        }
    }

    // --- Teste para restaurar (POST /cartaoDeCredito/restaurar) ---

    @Test
    @DisplayName("POST /cartaoDeCredito/restaurar - Sucesso (200 OK)")
    void restaurar_Sucesso() throws Exception {
        try (MockedStatic<CartaoCredito> mockedCartao = Mockito.mockStatic(CartaoCredito.class)) {
            mockedCartao.when(CartaoCredito::restaurar).thenAnswer(invocation -> null);

            CartaoCreditoHandler.restaurar.handle(mockCtx);

            mockedCartao.verify(CartaoCredito::restaurar);
            verify(mockCtx).status(OK);
            verify(mockCtx).result(anyString());
        }
    }
}