package com.mycompany.app.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.app.models.Ciclista;
import io.javalin.http.Handler;

import javax.net.ssl.HandshakeCompletedListener;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static io.javalin.http.HttpStatus.*;

public class CiclistaHandler {

    private static final ObjectMapper mapper = new ObjectMapper();

    // POST /ciclista - Cadastrar um ciclista
    public static final Handler cadastrarCiclista = ctx -> {
        try {
            // Converte o JSON recebido em um Map
            Map<String, Object> dados = mapper.readValue(ctx.body(), Map.class);

            String email = (String) dados.get("email");
            String nacionalidade = (String) dados.get("nacionalidade");
            String cpf = (String) dados.get("cpf");
            String passaporte = (String) dados.get("passaporte");
            Date validadePassaporte = (Date) dados.get("validadePassaporte");
            String pais = (String) dados.get("pais");
            String nome = (String) dados.get("nome");
            Date nascimento = (Date) dados.get("nascimento");
            String senha = (String) dados.get("senha");
            String confirmaSenha = (String) dados.get("confirmaSenha");
            String urlFoto = (String) dados.get("urlFoto");


            // === Validações ===
            if (!Ciclista.validarSintaxeEmail(email)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Email inválido.");
                return;
            }

            if (Ciclista.validarEmail(email)) {
                ctx.status(CONFLICT).result("Email já cadastrado.");
                return;
            }

            if (!Ciclista.validarNacionalidade(nacionalidade, cpf, passaporte, validadePassaporte, pais)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Nacionalidade ou documento inválido.");
                return;
            }

            if (!Ciclista.validarNome(nome)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Informe o nome completo.");
                return;
            }

            if (!Ciclista.validarSenha(senha, confirmaSenha)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Senha inválida: mínimo 6 caracteres, com letras maiúsculas e minúsculas.");
                return;
            }

            Ciclista novo = new Ciclista(
                    email,
                    nacionalidade,
                    cpf,
                    passaporte,
                    validadePassaporte,
                    pais,
                    nascimento,
                    nome,
                    senha,
                    urlFoto
            );

            // Retorna resposta de sucesso
            ctx.status(CREATED).result("Ciclista cadastrado");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };

    public static final Handler validarEmail = ctx -> {
        try {
            String email = ctx.pathParam("email");

            if (!Ciclista.validarSintaxeEmail(email)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Email de ciclista inválido (sintaxe incorreta).");
                return;
            }

            Boolean emailJaCadastrado = Ciclista.validarEmail(email);

            ctx.status(OK).json(emailJaCadastrado);
        } catch (Exception e) {
            // HTTP 500 - Internal Server Error
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao buscar ciclista.");
        }
    };

    // GET /ciclista/{idCiclista} - Recupera dados de um ciclista
    public static final Handler recuperarCiclistaPorId = ctx -> {
        try {
            String idString = ctx.pathParam("idCiclista");
            UUID idCiclista = UUID.fromString(idString);

            Ciclista ciclista = Ciclista.getCiclistaPorId(idCiclista); //

            if (ciclista != null) {
                // HTTP 200 - OK
                ctx.json(ciclista).status(OK);
            } else {
                // HTTP 404 - Not Found
                ctx.status(NOT_FOUND).result("Ciclista não encontrado.");
            }

        } catch (IllegalArgumentException e) {
            // Captura erro se a String não for um UUID válido (422)
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de ciclista inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            // HTTP 500 - Internal Server Error
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao buscar ciclista.");
        }
    };

    public static final Handler restaurar = ctx -> {
        try {
            Ciclista.restaurar();
            ctx.status(OK).result("Banco de dados restaurado (storage e mock limpos).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao restaurar banco de dados.");
        }
    };

    public static final Handler alterarDados = ctx -> {
        try{
            String idString = ctx.pathParam("idCiclista");
            UUID idCiclista = UUID.fromString(idString);

            Ciclista ciclistaParaAlterar = Ciclista.getCiclistaPorId(idCiclista);

            if (ciclistaParaAlterar == null) {
                ctx.status(NOT_FOUND).result("Ciclista não encontrado.");
                return;
            }

            // Converte o JSON recebido em um Map
            Map<String, Object> dados = mapper.readValue(ctx.body(), Map.class);

            UUID id = (UUID) dados.get("id");
            String email = (String) dados.get("email");
            String nacionalidade = (String) dados.get("nacionalidade");
            String cpf = (String) dados.get("cpf");
            String passaporte = (String) dados.get("passaporte");
            Date validadePassaporte = (Date) dados.get("validadePassaporte");
            String pais = (String) dados.get("pais");
            String nome = (String) dados.get("nome");
            Date nascimento = (Date) dados.get("nascimento");
            String senha = (String) dados.get("senha");
            String confirmaSenha = (String) dados.get("confirmaSenha");
            String urlFoto = (String) dados.get("urlFoto");

            // === Validações ===
            if (ciclistaParaAlterar.verificarMudancaEmail(email)) {
                if (!Ciclista.validarSintaxeEmail(email)) {
                    ctx.status(UNPROCESSABLE_CONTENT).result("Email inválido.");
                    return;
                }

                if (Ciclista.validarEmail(email)) {
                    ctx.status(CONFLICT).result("Email já cadastrado para outro usuário.");
                    return;
                }
            }

            if (!Ciclista.validarNacionalidade(nacionalidade, cpf, passaporte, validadePassaporte, pais)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Nacionalidade ou documento inválido.");
                return;
            }

            if (!Ciclista.validarNome(nome)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Informe o nome completo.");
                return;
            }

            if (!Ciclista.validarSenha(senha, confirmaSenha)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Senha inválida: mínimo 6 caracteres, com letras maiúsculas e minúsculas.");
                return;
            }

            ciclistaParaAlterar.alterarDados(
                    email,
                    nacionalidade,
                    cpf,
                    passaporte,
                    validadePassaporte,
                    pais,
                    nascimento,
                    nome,
                    senha,
                    urlFoto
            );

            ctx.status(OK).json(ciclistaParaAlterar);

        } catch (IllegalArgumentException e) {
            // Captura erro se a String não for um UUID válido (422)
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de ciclista inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };

    public static final Handler ativarCiclista = ctx -> {
        try {
            String idString = ctx.pathParam("idCiclista");
            UUID idCiclista = UUID.fromString(idString);

            Ciclista ciclistaParaAtivar = Ciclista.getCiclistaPorId(idCiclista);
            if (ciclistaParaAtivar == null) { // <--- Essencial para retornar o 404 correto!
                ctx.status(NOT_FOUND).result("Ciclista não encontrado.");
                return;
            }

            ciclistaParaAtivar.alterarStatusCiclista(true);

            ctx.status(OK).result("Ciclista Ativado");
        } catch (IllegalArgumentException e) {
            // Captura erro se a String não for um UUID válido (422)
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de ciclista inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };

    public static final Handler validarPermissaoAluguel = ctx -> {
        try {
            String idString = ctx.pathParam("idCiclista");
            UUID idCiclista = UUID.fromString(idString);

            Ciclista ciclistaParaValidar = Ciclista.getCiclistaPorId(idCiclista);
            if (ciclistaParaValidar == null) { // <--- Essencial para retornar o 404 correto!
                ctx.status(NOT_FOUND).result("Ciclista não encontrado.");
                return;
            }

            ctx.status(OK).json(ciclistaParaValidar.verificarPermissaoAluguel());
        } catch (IllegalArgumentException e) {
            // Captura erro se a String não for um UUID válido (422)
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de ciclista inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };

    public static final Handler retornarBicicletaAlugada = ctx -> {
        try {
            String idString = ctx.pathParam("idCiclista");
            UUID idCiclista = UUID.fromString(idString);

            Ciclista ciclista = Ciclista.getCiclistaPorId(idCiclista);
            if (ciclista == null) { // <--- Essencial para retornar o 404 correto!
                ctx.status(NOT_FOUND).result("Ciclista não encontrado.");
                return;
            }

            ctx.status(OK).json(ciclista.retornarBicicletaAlugada());
        } catch (IllegalArgumentException e) {
            // Captura erro se a String não for um UUID válido (422)
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de ciclista inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };
    public static final Handler listarCiclistas = ctx -> {
        ctx.json(Ciclista.listarTodos());
    };
}