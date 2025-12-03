package com.mycompany.app;

import com.mycompany.app.handlers.CiclistaHandler;
import com.mycompany.app.models.Ciclista;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.javalin.http.HttpStatus.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CiclistaTest {

    private Context mockCtx;
    private final UUID ciclistaId = UUID.randomUUID();

    // JSON base para cadastro
    private final String jsonCadastroBase = "{" +
            "\"email\": \"teste@email.com\"," +
            "\"nacionalidade\": \"Brasileira\"," +
            "\"cpf\": \"12345678901\"," +
            "\"pais\": \"Brasil\"," +
            "\"nome\": \"Nome Sobrenome\"," +
            "\"senha\": \"Senha123\"," +
            "\"confirmaSenha\": \"Senha123\"," +
            "\"urlFoto\": \"foto.jpg\"" +
            "}";

    @BeforeEach
    void setUp() {
        mockCtx = mock(Context.class);
        // Configuração para evitar NullPointerException no encadeamento .status().json()
        when(mockCtx.status(any(io.javalin.http.HttpStatus.class))).thenReturn(mockCtx);
        when(mockCtx.pathParam("idCiclista")).thenReturn(ciclistaId.toString());
    }

    private void mockValidacoesSucesso(MockedStatic<Ciclista> mockedCiclista) {
        mockedCiclista.when(() -> Ciclista.validarSintaxeEmail(anyString())).thenReturn(true);
        mockedCiclista.when(() -> Ciclista.validarEmail(anyString())).thenReturn(false); // Sucesso: não existe
        mockedCiclista.when(() -> Ciclista.validarNacionalidade(anyString(), anyString(), any(), any(), anyString())).thenReturn(true);
        mockedCiclista.when(() -> Ciclista.validarSenha(anyString(), anyString())).thenReturn(true);
        mockedCiclista.when(() -> Ciclista.validarNome(anyString())).thenReturn(true);
        mockedCiclista.when(() -> Ciclista.validarCPF(anyString())).thenReturn(true);
    }

    // --- TESTES: cadastrarCiclista (POST) ---

    @Test
    @DisplayName("POST /ciclista - Sucesso (201 CREATED)")
    void cadastrarCiclista_Sucesso() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastroBase);

        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class);
             MockedConstruction<Ciclista> mockedConstruction = Mockito.mockConstruction(Ciclista.class)) {

            mockValidacoesSucesso(mockedCiclista);

            CiclistaHandler.cadastrarCiclista.handle(mockCtx);

            // Verifica status e que chamou result (não json, conforme seu código)
            verify(mockCtx).status(CREATED);
            verify(mockCtx).result("Ciclista cadastrado");
        }
    }

    @Test
    @DisplayName("POST /ciclista - Falha: Email Inválido (422)")
    void cadastrarCiclista_EmailInvalido() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastroBase);

        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class)) {
            // Falha na sintaxe
            mockedCiclista.when(() -> Ciclista.validarSintaxeEmail(anyString())).thenReturn(false);

            CiclistaHandler.cadastrarCiclista.handle(mockCtx);

            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result("Email inválido.");
        }
    }

    @Test
    @DisplayName("POST /ciclista - Falha: Email já em uso (409)")
    void cadastrarCiclista_EmailEmUso() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastroBase);

        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class)) {
            mockedCiclista.when(() -> Ciclista.validarSintaxeEmail(anyString())).thenReturn(true);
            // Falha: Email já existe
            mockedCiclista.when(() -> Ciclista.validarEmail(anyString())).thenReturn(true);

            CiclistaHandler.cadastrarCiclista.handle(mockCtx);

            verify(mockCtx).status(CONFLICT);
            verify(mockCtx).result("Email já cadastrado.");
        }
    }

    @Test
    @DisplayName("POST /ciclista - Falha: Nacionalidade/CPF Inválido (422)")
    void cadastrarCiclista_NacionalidadeInvalida() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastroBase);

        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class)) {
            // Passa nas validações iniciais
            mockedCiclista.when(() -> Ciclista.validarSintaxeEmail(anyString())).thenReturn(true);
            mockedCiclista.when(() -> Ciclista.validarEmail(anyString())).thenReturn(false);

            // Falha na validação de nacionalidade
            mockedCiclista.when(() -> Ciclista.validarNacionalidade(anyString(), anyString(), any(), any(), anyString())).thenReturn(false);
            // Simula o comportamento do Handler: se nacionalidade falhar, ele pode checar CPF
            mockedCiclista.when(() -> Ciclista.validarCPF(anyString())).thenReturn(false);

            CiclistaHandler.cadastrarCiclista.handle(mockCtx);

            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result("Nacionalidade ou documento inválido.");
        }
    }

    // --- TESTES: retornarBicicletaAlugada (GET) ---

    @Test
    @DisplayName("GET /ciclista/{idCiclista}/bicicleta - Sucesso (200 OK)")
    void retornarBicicletaAlugada_Sucesso() throws Exception {
        Ciclista mockCiclistaInstancia = mock(Ciclista.class);
        Map<String, Object> dadosBike = new HashMap<>();
        dadosBike.put("id", "bike-123");

        when(mockCiclistaInstancia.retornarBicicletaAlugada()).thenReturn(dadosBike);

        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class)) {
            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(mockCiclistaInstancia);

            CiclistaHandler.retornarBicicletaAlugada.handle(mockCtx);

            verify(mockCtx).status(OK);
            verify(mockCtx).json(dadosBike);
        }
    }

    @Test
    @DisplayName("GET /ciclista/{idCiclista}/bicicleta - Ciclista Não Encontrado (404)")
    void retornarBicicletaAlugada_NaoEncontrado() throws Exception {
        try (MockedStatic<Ciclista> mockedCiclista = Mockito.mockStatic(Ciclista.class)) {
            mockedCiclista.when(() -> Ciclista.getCiclistaPorId(ciclistaId)).thenReturn(null);

            CiclistaHandler.retornarBicicletaAlugada.handle(mockCtx);

            verify(mockCtx).status(NOT_FOUND);
            verify(mockCtx).result("Ciclista não encontrado.");
        }
    }

    @Test
    @DisplayName("GET /ciclista/{idCiclista}/bicicleta - ID Inválido (422)")
    void retornarBicicletaAlugada_IdInvalido() throws Exception {
        when(mockCtx.pathParam("idCiclista")).thenReturn("invalido");

        CiclistaHandler.retornarBicicletaAlugada.handle(mockCtx);

        verify(mockCtx).status(UNPROCESSABLE_CONTENT);
        verify(mockCtx).result(contains("ID de ciclista inválido"));
    }
}