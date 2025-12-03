package com.mycompany.app;

import com.mycompany.app.handlers.AluguelHandler;
import com.mycompany.app.handlers.CartaoCreditoHandler;
import com.mycompany.app.handlers.CiclistaHandler;
import com.mycompany.app.handlers.FuncionarioHandler;

import io.javalin.Javalin;
import io.javalin.http.Handler;

import static io.javalin.http.HttpStatus.OK;

public class App {

    public static final Handler restaurarBancoCompleto = ctx -> {
        CiclistaHandler.restaurar.handle(ctx);
        FuncionarioHandler.restaurar.handle(ctx);
        CartaoCreditoHandler.restaurar.handle(ctx);
        AluguelHandler.restaurar.handle(ctx);

        // Define o status final e a mensagem
        ctx.status(OK).result("Bancos de dados de Ciclistas e Funcionários restaurados com sucesso.");
    };

    public static void main(String[] args) {
        var app = Javalin.create(/*config*/)
                .get("/", ctx -> ctx.result("Hello World"))

                // GET para restaurar o banco de dados
                // Usando o novo Handler Combinado
                .get("/restaurarBanco", restaurarBancoCompleto) // Rota única para restaurar tudo

                // Rotas de ciclista
                .post("/ciclista", CiclistaHandler.cadastrarCiclista)
                .post("/ciclista/existeEmail/{email}", CiclistaHandler.validarEmail)
                .get("/ciclista", CiclistaHandler.listarCiclistas)
                .get("/ciclista/{idCiclista}", CiclistaHandler.recuperarCiclistaPorId)
                .put("/ciclista/{idCiclista}", CiclistaHandler.alterarDados)
                .post("/ciclista/{idCiclista}/ativar", CiclistaHandler.ativarCiclista)
                .get("/ciclista/{idCiclista}/permiteAluguel", CiclistaHandler.validarPermissaoAluguel)
                .get("/ciclista/{idCiclista}/bicicletaAlugada", CiclistaHandler.retornarBicicletaAlugada)

                // Rotas de Funcionário
                .post("/funcionario", FuncionarioHandler.cadastrarFuncionario)
                .get("/funcionario", FuncionarioHandler.listarFuncionarios)
                .get("/funcionario/{idFuncionario}", FuncionarioHandler.recuperarFuncionarioPorMatricula)
                .put("/funcionario/{idFuncionario}", FuncionarioHandler.alterarDadosFuncionario)
                .delete("/funcionario/{idFuncionario}", FuncionarioHandler.removerFuncionario)

                // Rotas do Cartao de Credito
                .get("/cartaoDeCredito/{idCiclista}", CartaoCreditoHandler.recuperarCartaoPorCiclistaId)
                .put("/cartaoDeCredito/{idCiclista}", CartaoCreditoHandler.alterarCartaoPorCiclistaId)

                // Rotas de aluguel e devolução
                .post("/aluguel", AluguelHandler.realizarAluguel)
                .post("/devolucao", AluguelHandler.realizarDevolucao)

                .start(7070);
    }
}