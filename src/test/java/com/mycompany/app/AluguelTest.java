package com.mycompany.app;

import com.mycompany.app.handlers.AluguelHandler;
import com.mycompany.app.models.Aluguel;
import io.javalin.http.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.UUID;

import static io.javalin.http.HttpStatus.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AluguelTest {

    private Context mockCtx;
    private final UUID ciclistaId = UUID.randomUUID();
    private final UUID trancaId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockCtx = mock(Context.class);
        when(mockCtx.status(any(io.javalin.http.HttpStatus.class))).thenReturn(mockCtx);
    }

    // --- Testes para realizarAluguel ---

    @Test
    @DisplayName("POST /aluguel - Sucesso")
    void realizarAluguel_Sucesso() throws Exception {
        // 1. Configurar o mock do corpo da requisição
        String jsonBody = String.format("{\"ciclista\": \"%s\", \"trancaInicio\": \"%s\"}", ciclistaId.toString(), trancaId.toString());
        when(mockCtx.body()).thenReturn(jsonBody);

        // 2. Mockar o método estático de lógica de negócio (Aluguel.realizarAluguel)
        Aluguel mockAluguel = mock(Aluguel.class);
        try (MockedStatic<Aluguel> mockedAluguel = Mockito.mockStatic(Aluguel.class)) {
            mockedAluguel.when(() -> Aluguel.realizarAluguel(ciclistaId, trancaId)).thenReturn(mockAluguel);

            // 3. Executar o Handler
            AluguelHandler.realizarAluguel.handle(mockCtx);

            // 4. Verificar o resultado HTTP e o corpo da resposta
            verify(mockCtx).status(OK); // 200 OK
            verify(mockCtx).json(mockAluguel);

            // 5. Verificar se a lógica de negócio foi chamada corretamente
            mockedAluguel.verify(() -> Aluguel.realizarAluguel(ciclistaId, trancaId), Mockito.times(1));
        }
    }

    @Test
    @DisplayName("POST /aluguel - Falha: Campos Faltando")
    void realizarAluguel_CamposFaltando() throws Exception {
        String jsonBody = String.format("{\"ciclista\": \"%s\"}", ciclistaId.toString()); // Falta trancaInicio
        when(mockCtx.body()).thenReturn(jsonBody);

        // Não precisa mockar Aluguel.realizarAluguel, pois a validação do Handler deve falhar antes
        AluguelHandler.realizarAluguel.handle(mockCtx);

        // Verifica status 422 e a mensagem de erro
        verify(mockCtx).status(UNPROCESSABLE_CONTENT);
        verify(mockCtx).result(anyString());
    }

    @Test
    @DisplayName("POST /aluguel - Falha: ID Inválido (Formato)")
    void realizarAluguel_IdInvalido() throws Exception {
        String jsonBody = "{\"ciclista\": \"nao-e-uuid\", \"trancaInicio\": \"1234\"}";
        when(mockCtx.body()).thenReturn(jsonBody);

        AluguelHandler.realizarAluguel.handle(mockCtx);

        // Verifica status 422
        verify(mockCtx).status(UNPROCESSABLE_CONTENT);
        verify(mockCtx).result(anyString());
    }

    @Test
    @DisplayName("POST /aluguel - Falha: Exceção de Negócio (IllegalStateException)")
    void realizarAluguel_ExcecaoNegocio() throws Exception {
        String jsonBody = String.format("{\"ciclista\": \"%s\", \"trancaInicio\": \"%s\"}", ciclistaId.toString(), trancaId.toString());
        when(mockCtx.body()).thenReturn(jsonBody);

        try (MockedStatic<Aluguel> mockedAluguel = Mockito.mockStatic(Aluguel.class)) {
            // Simula falha de negócio (Ex: ciclista já tem aluguel ativo)
            mockedAluguel.when(() -> Aluguel.realizarAluguel(any(UUID.class), any(UUID.class)))
                    .thenThrow(new IllegalStateException("Ciclista já está alugando."));

            AluguelHandler.realizarAluguel.handle(mockCtx);

            // Verifica status 422
            verify(mockCtx).status(UNPROCESSABLE_CONTENT);
            verify(mockCtx).result(contains("Erro no processo de aluguel"));
        }
    }


    // --- Testes para realizarDevolucao ---

    @Test
    @DisplayName("POST /devolucao - Sucesso")
    void realizarDevolucao_Sucesso() throws Exception {
        UUID bicicletaId = UUID.randomUUID();
        UUID trancaFimId = UUID.randomUUID();

        // 1. Configurar o mock do corpo da requisição
        String jsonBody = String.format("{\"idBicicleta\": \"%s\", \"idTranca\": \"%s\"}", bicicletaId.toString(), trancaFimId.toString());
        when(mockCtx.body()).thenReturn(jsonBody);

        // 2. Mockar o método estático de lógica de negócio
        Aluguel mockAluguel = mock(Aluguel.class);
        try (MockedStatic<Aluguel> mockedAluguel = Mockito.mockStatic(Aluguel.class)) {
            mockedAluguel.when(() -> Aluguel.realizarDevolucao(trancaFimId, bicicletaId)).thenReturn(mockAluguel);

            // 3. Executar o Handler
            AluguelHandler.realizarDevolucao.handle(mockCtx);

            // 4. Verificar o resultado HTTP e o corpo da resposta
            verify(mockCtx).status(OK); // 200 OK
            verify(mockCtx).json(mockAluguel);

            // 5. Verificar se a lógica de negócio foi chamada corretamente
            mockedAluguel.verify(() -> Aluguel.realizarDevolucao(trancaFimId, bicicletaId), Mockito.times(1));
        }
    }

    private String contains(String substring) {
        return Mockito.argThat(argument -> argument != null && argument.contains(substring));
    }
}