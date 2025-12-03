package com.mycompany.app.models;

import com.mycompany.app.repositories.FuncionarioRepositorio;
import org.mockito.Mockito;

import java.util.*;
import java.util.regex.Pattern;

public class Funcionario {
    private static final FuncionarioRepositorio mockRepo = Mockito.mock(FuncionarioRepositorio.class);
    private static final Map<UUID, Funcionario> storage = new HashMap<>();

    private UUID matricula;
    private String senha;
    private String confirmacaoSenha;
    private String email;
    private String nome;
    private String idade;
    private String funcao;
    private String cpf;

    public Funcionario(String senha, String confirmacaoSenha, String email, String nome, String idade, String funcao, String cpf) {
        this.matricula = UUID.randomUUID();

        while (validarMatricula(this.matricula)) {
            this.matricula = UUID.randomUUID();
        }

        this.senha = senha;
        this.confirmacaoSenha = confirmacaoSenha;
        this.email = email;
        this.nome = nome;
        this.idade = idade;
        this.funcao = funcao;
        this.cpf = cpf;

        storage.put(this.matricula, this);

        Mockito.when(mockRepo.getFuncionarioMatricula(this.matricula))
                .thenReturn(this);

        Mockito.when(mockRepo.findAll())
                .thenReturn(new ArrayList<>(storage.values()));
    }

    private Boolean validarMatricula(UUID matricula) {
        return storage.containsKey(matricula);
    }

    public void alterarDados(String senha, String confirmacaoSenha, String email, String nome, String idade, String funcao, String cpf){
        this.email = email;
        this.cpf = cpf;
        this.nome = nome;
        this.senha = senha;
        this.confirmacaoSenha = confirmacaoSenha;
        this.idade = idade;
        this.funcao = funcao;

        storage.put(this.matricula, this);
    }
    public Boolean verificarMudancaEmail(String email){
        if (email.equals(this.email)) {
            return false;
        }
        return true;
    };

    public Map<String, Object> getFuncionario (String matriculaString) {

        UUID matricula = UUID.fromString(matriculaString);
        Funcionario funcionario = storage.get(matricula);

        if (funcionario == null) {
            return null;
        }

        Map<String, Object> funcionarioInformacoes = new HashMap<>();

        funcionarioInformacoes.put("matricula", funcionario.matricula.toString());
        funcionarioInformacoes.put("senha", funcionario.senha);
        funcionarioInformacoes.put("confirmacaoSenha", funcionario.confirmacaoSenha);
        funcionarioInformacoes.put("email", funcionario.email);
        funcionarioInformacoes.put("nome", funcionario.nome);

        try {
            funcionarioInformacoes.put("idade", Integer.parseInt(funcionario.idade));
        } catch (NumberFormatException e) {
            funcionarioInformacoes.put("idade", 0);
        }

        funcionarioInformacoes.put("funcao", funcionario.funcao);
        funcionarioInformacoes.put("cpf", funcionario.cpf);

        return funcionarioInformacoes;
    }
    public static List<Funcionario> listarTodos() {
        return new ArrayList<>(storage.values());
    }

    public static Funcionario getFuncionarioMatricula(UUID matricula) {
        return storage.get(matricula);
    }

    public static Boolean validarSintaxeEmail(String email) {
        String regex = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.matches(regex, email);
    }

    public static Boolean validarEmailEmUso(String email) {
        return storage.values().stream().anyMatch(c -> c.email.equalsIgnoreCase(email));
    }

    public static Boolean validarCPF(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }

    public static Boolean validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return false;
        return nome.trim().split(" ").length >= 2;
    }

    public static Boolean validarSenha(String senhaA, String senhaB) {
        if (senhaA == null || !senhaA.equals(senhaB)) return false;
        // Exemplo: Mínimo 6 caracteres, maiúscula e minúscula
        String regex = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$";
        return senhaA.matches(regex);
    }

    public static Boolean remover(UUID matricula) {
        return storage.remove(matricula) != null;
    }

    public static void restaurar() {
        Mockito.reset(mockRepo);
        storage.clear();
    }
}