package com.mycompany.app.handlers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.app.models.Funcionario;
import io.javalin.http.Handler;

import java.util.Map;
import java.util.UUID;

import static io.javalin.http.HttpStatus.*;
public class FuncionarioHandler {
    private static final ObjectMapper mapper = new ObjectMapper();

    // POST /funcionario - Cadastrar funcionário
    public static final Handler cadastrarFuncionario = ctx -> {
        try {
            Map<String, Object> dados = mapper.readValue(ctx.body(), Map.class);

            String senha = (String) dados.get("senha");
            String confirmaSenha = (String) dados.get("confirmaSenha");
            String email = (String) dados.get("email");
            String nome = (String) dados.get("nome");
            String idade = (String) dados.get("idade");
            String funcao = (String) dados.get("funcao");
            String cpf = (String) dados.get("cpf");

            if (!Funcionario.validarSintaxeEmail(email)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Email inválido.");
                return;
            }

            if (Funcionario.validarEmailEmUso(email)) {
                ctx.status(CONFLICT).result("Email já cadastrado.");
                return;
            }

            if (!Funcionario.validarCPF(cpf)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("CPF inválido (deve ter 11 dígitos).");
                return;
            }

            if (!Funcionario.validarNome(nome)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Informe o nome completo.");
                return;
            }

            if (!Funcionario.validarSenha(senha, confirmaSenha)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Senha inválida: mínimo 6 caracteres, com letras maiúsculas e minúsculas.");
                return;
            }

            Funcionario novo = new Funcionario(
                    senha,
                    confirmaSenha,
                    email,
                    nome,
                    idade,
                    funcao,
                    cpf
            );

            // HTTP 201 (CREATED) para criação bem-sucedida, como em Ciclista
            ctx.status(CREATED).result("Funcionário cadastrado.");

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };

    // PUT /funcionario/{idFuncionario} - Alterar dados do funcionário
    public static final Handler alterarDadosFuncionario = ctx -> {
        try{
            String idString = ctx.pathParam("idFuncionario");
            UUID idFuncionario = UUID.fromString(idString);

            Funcionario funcionarioParaAlterar = Funcionario.getFuncionarioMatricula(idFuncionario);

            if (funcionarioParaAlterar == null) {
                ctx.status(NOT_FOUND).result("Funcionário não encontrado.");
                return;
            }

            // Converte o JSON recebido em um Map
            Map<String, Object> dados = mapper.readValue(ctx.body(), Map.class);

            // Mapeamento dos dados
            String senha = (String) dados.get("senha");
            String confirmaSenha = (String) dados.get("confirmaSenha");
            String email = (String) dados.get("email");
            String nome = (String) dados.get("nome");
            String idade = (String) dados.get("idade");
            String funcao = (String) dados.get("funcao");
            String cpf = (String) dados.get("cpf");

            // === Validações ===

            // 1. Validação de Email (se o email mudou, precisa verificar sintaxe e se já está em uso)
            if (funcionarioParaAlterar.verificarMudancaEmail(email)) {
                if (!Funcionario.validarSintaxeEmail(email)) {
                    ctx.status(UNPROCESSABLE_CONTENT).result("Email inválido (sintaxe).");
                    return;
                }

                if (Funcionario.validarEmailEmUso(email)) {
                    ctx.status(CONFLICT).result("Email já cadastrado para outro usuário.");
                    return;
                }
            }

            // 2. Validação de CPF
            if (!Funcionario.validarCPF(cpf)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("CPF inválido (deve ter 11 dígitos).");
                return;
            }

            // 3. Validação de Nome
            if (!Funcionario.validarNome(nome)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Informe o nome completo.");
                return;
            }

            // 4. Validação de Senha
            if (!Funcionario.validarSenha(senha, confirmaSenha)) {
                ctx.status(UNPROCESSABLE_CONTENT).result("Senha inválida: mínimo 6 caracteres, com letras maiúsculas e minúsculas.");
                return;
            }

            // Chama o método de atualização do modelo
            funcionarioParaAlterar.alterarDados(
                    senha,
                    confirmaSenha,
                    email,
                    nome,
                    idade,
                    funcao,
                    cpf
            );

            // Retorna o objeto atualizado e o status 200 OK
            ctx.status(OK).json(funcionarioParaAlterar);

        } catch (IllegalArgumentException e) {
            // Captura erro se a String não for um UUID válido (422)
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de funcionário inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(BAD_REQUEST).result("Erro ao processar a requisição: " + e.getMessage());
        }
    };

    // GET /funcionario - Listar todos os funcionários
    public static final Handler listarFuncionarios = ctx -> {
        try {
            ctx.status(OK).json(Funcionario.listarTodos());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao listar funcionários.");
        }
    };

    // GET /funcionario/{idFuncionario} - Recuperar funcionário por ID
    public static final Handler recuperarFuncionarioPorMatricula = ctx -> {
        try {
            String idString = ctx.pathParam("matriculaFuncionario");

            UUID idFuncionario = UUID.fromString(idString);

            Funcionario funcionario = Funcionario.getFuncionarioMatricula(idFuncionario);

            if (funcionario != null) {
                // HTTP 200 - OK
                ctx.status(OK).json(funcionario);
            } else {
                // HTTP 404 - Not Found
                ctx.status(NOT_FOUND).result("Funcionário não encontrado.");
            }

        } catch (IllegalArgumentException e) {
            // Captura erro se a String não for um UUID válido (422)
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de funcionário inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            // HTTP 500 - Internal Server Error
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao buscar funcionário.");
        }
    };
    public static final Handler removerFuncionario = ctx -> {
        try {
            String idString = ctx.pathParam("idFuncionario");

            UUID idFuncionario = UUID.fromString(idString);

            Boolean removido = Funcionario.remover(idFuncionario);

            if (removido) {
                ctx.status(OK).result("Funcionário removido com sucesso.");
            } else {
                ctx.status(NOT_FOUND).result("Funcionário não encontrado.");
            }

        } catch (IllegalArgumentException e) {
            ctx.status(UNPROCESSABLE_CONTENT).result("ID de funcionário inválido (deve ser um UUID válido).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao remover funcionário: " + e.getMessage());
        }
    };

    // Handler para restaurar o storage estático
    public static final Handler restaurar = ctx -> {
        try {
            Funcionario.restaurar();
            ctx.status(OK).result("Banco de dados de funcionários restaurado (storage e mock limpos).");
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(INTERNAL_SERVER_ERROR).result("Erro interno ao restaurar banco de dados.");
        }
    };
}
