package com.mycompany.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.app.models.Aluguel;
import io.javalin.http.Handler;

import java.util.Map;
import java.util.UUID;

import static io.javalin.http.HttpStatus.*;

public class AluguelHandler {
    private static final ObjectMapper mapper = new ObjectMapper();

    // POST /aluguel - Realizar aluguel
    public static final Handler realizarAluguel = ctx -> {
        try {
            Map<String, Object> dados = mapper.readValue(ctx.body(), Map.class);

            // Requeridos: ciclista e trancaInicio
            String ciclistaIdString = (String) dados.get("ciclista");
            String trancaInicioIdString = (String) dados.get("trancaInicio");

            if (ciclistaIdString == null || trancaInicioIdString == null) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Campos 'ciclista' e 'trancaInicio' são obrigatórios.");
                return;
            }

            UUID ciclistaId = UUID.fromString(ciclistaIdString);
            UUID trancaInicioId = UUID.fromString(trancaInicioIdString);

            Aluguel aluguel = Aluguel.realizarAluguel(ciclistaId, trancaInicioId);

            ctx.status(OK).json(aluguel);

        } catch (IllegalArgumentException e) {
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de ciclista ou tranca inválido (deve ser um UUID válido).");
        } catch (IllegalStateException e) {
            ctx.status(UNPROCESSABLE_CONTENT).result("Erro no processo de aluguel: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };

    // POST /devolucao - Realizar devolução
    public static final Handler realizarDevolucao = ctx -> {
        try {
            Map<String, Object> dados = mapper.readValue(ctx.body(), Map.class);

            String idTrancaString = (String) dados.get("idTranca");
            String idBicicletaString = (String) dados.get("idBicicleta");

            if (idTrancaString == null || idBicicletaString == null) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Campos 'idTranca' e 'idBicicleta' são obrigatórios.");
                return;
            }

            UUID idTranca = UUID.fromString(idTrancaString);
            UUID idBicicleta = UUID.fromString(idBicicletaString);

            Aluguel devolucao = Aluguel.realizarDevolucao(idTranca, idBicicleta);

            ctx.status(OK).json(devolucao);

        } catch (IllegalArgumentException e) {
            ctx.status(UNPROCESSABLE_CONTENT).result("ID inválido: " + e.getMessage());
        } catch (IllegalStateException e) {
            ctx.status(UNPROCESSABLE_CONTENT).result("Erro no processo de devolução: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };

    public static final Handler restaurar = ctx -> {
        Aluguel.restaurar();
        ctx.status(OK).result("Banco de dados de aluguéis restaurado.");
    };
}