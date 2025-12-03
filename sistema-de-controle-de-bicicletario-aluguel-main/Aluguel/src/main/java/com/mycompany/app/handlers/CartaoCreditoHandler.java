package com.mycompany.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.app.models.CartaoCredito;
import com.mycompany.app.models.Ciclista;
import io.javalin.http.Handler;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static io.javalin.http.HttpStatus.*;

public class CartaoCreditoHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    // GET /cartaoDeCredito/{idCiclista} - Recupera dados de cartão
    public static final Handler recuperarCartaoPorCiclistaId = ctx -> {
        try {
            String idString = ctx.pathParam("idCiclista");
            UUID idCiclista = UUID.fromString(idString);

            if (Ciclista.getCiclistaPorId(idCiclista) == null) {
                ctx.status(NOT_FOUND).result("Ciclista não encontrado.");
                return;
            }

            CartaoCredito cartao = CartaoCredito.getCartaoCreditoPorCiclistaId(idCiclista);

            if (cartao != null) {
                ctx.status(OK).json(cartao);
            } else {
                ctx.status(NOT_FOUND).result("Cartão de crédito não encontrado para este ciclista.");
            }

        } catch (IllegalArgumentException e) {
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de ciclista inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao buscar dados do cartão.");
        }
    };

    // PUT /cartaoDeCredito/{idCiclista} - Alterar dados de cartão
    public static final Handler alterarCartaoPorCiclistaId = ctx -> {
        try {
            String idString = ctx.pathParam("idCiclista");
            UUID idCiclista = UUID.fromString(idString);

            if (Ciclista.getCiclistaPorId(idCiclista) == null) {
                ctx.status(NOT_FOUND).result("Ciclista não encontrado.");
                return;
            }

            Map<String, Object> dados = mapper.readValue(ctx.body(), Map.class);
            String nomeTitular = (String) dados.get("nomeTitular");
            String numero = (String) dados.get("numero");
            Date validade = new Date((Long) dados.get("validade"));
            String cvv = (String) dados.get("cvv");

            if (!CartaoCredito.validarNomeTitular(nomeTitular)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Nome do titular inválido (deve ser completo).");
                return;
            }
            if (!CartaoCredito.validarNumero(numero)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Número do cartão inválido (deve ter 11 dígitos, conforme o modelo).");
                return;
            }
            if (!CartaoCredito.validarCVV(cvv)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("CVV inválido (deve ter 3 dígitos).");
                return;
            }
            if (!CartaoCredito.validarValidade(validade)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Validade do cartão inválida (deve ser no futuro).");
                return;
            }

            // 4. Salva/Atualiza os dados (PUT é usado para criar se não existe ou atualizar)
            CartaoCredito cartaoAtualizado = CartaoCredito.alterarDadosCartao(idCiclista, nomeTitular, numero, validade, cvv);

            if (cartaoAtualizado == null) {
                // Se não existia, cria um novo (seguindo a semântica de um PUT em REST)
                cartaoAtualizado = new CartaoCredito(idCiclista, nomeTitular, numero, validade, cvv);
            }

            ctx.status(OK).json(cartaoAtualizado);

        } catch (IllegalArgumentException e) {
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de ciclista inválido (deve ser um UUID válido).");
        } catch (ClassCastException e) {
            ctx.status(UNPROCESSABLE_CONTENT).result("Formato de dados inválido no corpo da requisição (verifique se 'validade' é um número (timestamp)).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao alterar dados do cartão: " + e.getMessage());
        }
    };

    // Handler para restaurar o storage estático (para testes)
    public static final Handler restaurar = ctx -> {
        try {
            CartaoCredito.restaurar();
            ctx.status(OK).result("Banco de dados de cartões de crédito restaurado.");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao restaurar banco de dados.");
        }
    };
}