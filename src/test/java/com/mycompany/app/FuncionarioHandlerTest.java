package com.mycompany.app;

import com.mycompany.app.handlers.FuncionarioHandler;
import com.mycompany.app.models.Funcionario;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

import static io.javalin.http.HttpStatus.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FuncionarioHandlerTest {

    private Context mockCtx;
    private final UUID funcionarioId = UUID.randomUUID();

    // JSON válido para testes de cadastro
    private final String jsonCadastro = "{" +
            "\"senha\": \"SenhaForte123\"," +
            "\"confirmaSenha\": \"SenhaForte123\"," +
            "\"email\": \"func@teste.com\"," +
            "\"nome\": \"Nome Sobrenome\"," +
            "\"idade\": \"30\"," +
            "\"funcao\": \"Gerente\"," +
            "\"cpf\": \"12345678901\"" +
            "}";

    @BeforeEach
    void setUp() {
        mockCtx = mock(Context.class);
        // Configuração vital para evitar NullPointerException no encadeamento .status().result()/.json()
        when(mockCtx.status(any(io.javalin.http.HttpStatus.class))).thenReturn(mockCtx);
        when(mockCtx.pathParam("idFuncionario")).thenReturn(funcionarioId.toString());
    }

    // Método auxiliar para configurar todos os mocks de validação retornando TRUE (sucesso)
    private void mockValidacoesSucesso(MockedStatic<Funcionario> mockedFuncionario) {
        mockedFuncionario.when(() -> Funcionario.validarSintaxeEmail(anyString())).thenReturn(true);
        mockedFuncionario.when(() -> Funcionario.validarEmailEmUso(anyString())).thenReturn(false); // False = não está em uso
        mockedFuncionario.when(() -> Funcionario.validarCPF(anyString())).thenReturn(true);
        mockedFuncionario.when(() -> Funcionario.validarNome(anyString())).thenReturn(true);
        mockedFuncionario.when(() -> Funcionario.validarSenha(anyString(), anyString())).thenReturn(true);
    }

    // --- TESTES: cadastrarFuncionario (POST) ---

    @Test
    @DisplayName("POST /funcionario - Sucesso (201 CREATED)")
    void cadastrarFuncionario_Sucesso() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastro);

        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class);
             MockedConstruction<Funcionario> mockedConstruction = Mockito.mockConstruction(Funcionario.class)) {

            mockValidacoesSucesso(mockedFuncionario);

            FuncionarioHandler.cadastrarFuncionario.handle(mockCtx);

            // CORREÇÃO: O Handler retorna status 201 e a mensagem "Funcionário cadastrado."
            verify(mockCtx).status(CREATED);
            verify(mockCtx).result("Funcionário cadastrado.");
        }
    }

    @Test
    @DisplayName("POST /funcionario - Falha: Email Inválido (422)")
    void cadastrarFuncionario_EmailInvalido() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastro);

        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class)) {
            // Falha na sintaxe
            mockedFuncionario.when(() -> Funcionario.validarSintaxeEmail(anyString())).thenReturn(false);

            FuncionarioHandler.cadastrarFuncionario.handle(mockCtx);

            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result("Email inválido.");
        }
    }

    @Test
    @DisplayName("POST /funcionario - Falha: Email Já Cadastrado (409)")
    void cadastrarFuncionario_EmailEmUso() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastro);

        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class)) {
            mockedFuncionario.when(() -> Funcionario.validarSintaxeEmail(anyString())).thenReturn(true);
            // Falha: Email já existe
            mockedFuncionario.when(() -> Funcionario.validarEmailEmUso(anyString())).thenReturn(true);

            FuncionarioHandler.cadastrarFuncionario.handle(mockCtx);

            verify(mockCtx).status(CONFLICT);
            verify(mockCtx).result("Email já cadastrado.");
        }
    }

    @Test
    @DisplayName("POST /funcionario - Falha: CPF Inválido (422)")
    void cadastrarFuncionario_CpfInvalido() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastro);

        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class)) {
            mockedFuncionario.when(() -> Funcionario.validarSintaxeEmail(anyString())).thenReturn(true);
            mockedFuncionario.when(() -> Funcionario.validarEmailEmUso(anyString())).thenReturn(false);
            // Falha: CPF
            mockedFuncionario.when(() -> Funcionario.validarCPF(anyString())).thenReturn(false);

            FuncionarioHandler.cadastrarFuncionario.handle(mockCtx);

            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result("CPF inválido.");
        }
    }

    @Test
    @DisplayName("POST /funcionario - Falha: Nome Inválido (422)")
    void cadastrarFuncionario_NomeInvalido() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastro);

        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class)) {
            mockedFuncionario.when(() -> Funcionario.validarSintaxeEmail(anyString())).thenReturn(true);
            mockedFuncionario.when(() -> Funcionario.validarEmailEmUso(anyString())).thenReturn(false);
            mockedFuncionario.when(() -> Funcionario.validarCPF(anyString())).thenReturn(true);
            // Falha: Nome
            mockedFuncionario.when(() -> Funcionario.validarNome(anyString())).thenReturn(false);

            FuncionarioHandler.cadastrarFuncionario.handle(mockCtx);

            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result("Nome do titular inválido (deve ser completo).");
        }
    }

    @Test
    @DisplayName("POST /funcionario - Falha: Senha Inválida (422)")
    void cadastrarFuncionario_SenhaInvalida() throws Exception {
        when(mockCtx.body()).thenReturn(jsonCadastro);

        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class)) {
            mockedFuncionario.when(() -> Funcionario.validarSintaxeEmail(anyString())).thenReturn(true);
            mockedFuncionario.when(() -> Funcionario.validarEmailEmUso(anyString())).thenReturn(false);
            mockedFuncionario.when(() -> Funcionario.validarCPF(anyString())).thenReturn(true);
            mockedFuncionario.when(() -> Funcionario.validarNome(anyString())).thenReturn(true);
            // Falha: Senha
            mockedFuncionario.when(() -> Funcionario.validarSenha(anyString(), anyString())).thenReturn(false);

            FuncionarioHandler.cadastrarFuncionario.handle(mockCtx);

            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result("Senha inválida ou não confere com a confirmação.");
        }
    }

    // --- TESTES: removerFuncionario (DELETE) ---

    @Test
    @DisplayName("DELETE /funcionario/{idFuncionario} - Sucesso (200 OK)")
    void removerFuncionario_Sucesso() throws Exception {
        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class)) {
            mockedFuncionario.when(() -> Funcionario.remover(funcionarioId)).thenReturn(true);

            FuncionarioHandler.removerFuncionario.handle(mockCtx);

            verify(mockCtx).status(OK);
            verify(mockCtx).result("Funcionário removido com sucesso.");
        }
    }

    @Test
    @DisplayName("DELETE /funcionario/{idFuncionario} - Não Encontrado (404)")
    void removerFuncionario_NaoEncontrado() throws Exception {
        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class)) {
            mockedFuncionario.when(() -> Funcionario.remover(funcionarioId)).thenReturn(false);

            FuncionarioHandler.removerFuncionario.handle(mockCtx);

            verify(mockCtx).status(NOT_FOUND);
            verify(mockCtx).result("Funcionário não encontrado.");
        }
    }

    @Test
    @DisplayName("DELETE /funcionario/{idFuncionario} - ID Inválido (422)")
    void removerFuncionario_IdInvalido() throws Exception {
        when(mockCtx.pathParam("idFuncionario")).thenReturn("uuid-invalido");

        FuncionarioHandler.removerFuncionario.handle(mockCtx);

        verify(mockCtx).status(UNPROCESSABLE_CONTENT);
        // Usa contains para verificar se a mensagem contém o texto esperado
        verify(mockCtx).result(contains("ID de funcionário inválido"));
    }

    // --- TESTE: restaurar (POST) ---

    @Test
    @DisplayName("POST /funcionario/restaurar - Sucesso (200 OK)")
    void restaurar_Sucesso() throws Exception {
        try (MockedStatic<Funcionario> mockedFuncionario = Mockito.mockStatic(Funcionario.class)) {
            FuncionarioHandler.restaurar.handle(mockCtx);

            mockedFuncionario.verify(Funcionario::restaurar, times(1));
            verify(mockCtx).status(OK);
            verify(mockCtx).result(contains("restaurado"));
        }
    }
}