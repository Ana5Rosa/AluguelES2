package com.mycompany.app.models;

import com.mycompany.app.repositories.CiclistaRepositorio;
import org.mockito.Mockito;

import java.util.*;
import java.util.regex.Pattern;

public class Ciclista {
    private static final CiclistaRepositorio mockRepo = Mockito.mock(CiclistaRepositorio.class);
    private static final Map<UUID, Ciclista> storage = new HashMap<>();
    private UUID id;
    private String email;
    private String nacionalidade;
    private String cpf;
    private String passaporte;
    private Date validadePassaporte;
    private String pais;
    private Date nascimento;
    private String nome;
    private String senha;
    private String urlFoto;
    private Boolean status;
    private Boolean statusPermicaoAluguel;
    private UUID bicicletaId = null;

    public Ciclista(String email, String nacionalidade, String cpf, String passaporte,
                    Date validadePassaporte, String pais, Date nascimento,
                    String nome, String senha, String urlFoto) {

        this.id = UUID.randomUUID();
        while (validarID(this.id)) {
            this.id = UUID.randomUUID();
        }

        this.email = email;
        this.nacionalidade = nacionalidade;
        this.cpf = cpf;
        this.passaporte = passaporte;
        this.validadePassaporte = validadePassaporte;
        this.pais = pais;
        this.nascimento = nascimento;
        this.nome = nome;
        this.senha = senha;
        this.urlFoto = urlFoto;
        this.status = false;
        this.statusPermicaoAluguel =  false;

        storage.put(this.id, this);

        Mockito.when(mockRepo.buscarPorID(this.id))
                .thenReturn(true);

        Mockito.when(mockRepo.buscarPorEmail(this.email))
                .thenReturn(true);

        Mockito.when(mockRepo.getCiclistaPorId(this.id))
                .thenReturn(this);

        Mockito.when(mockRepo.findAll())
                .thenReturn(new ArrayList<>(storage.values()));
    }

    public void alterarDados(String email, String nacionalidade, String cpf, String passaporte,
                             Date validadePassaporte, String pais, Date nascimento,
                             String nome, String senha, String urlFoto){
        this.email = email;
        this.nacionalidade = nacionalidade;
        this.cpf = cpf;
        this.passaporte = passaporte;
        this.validadePassaporte = validadePassaporte;
        this.pais = pais;
        this.nascimento = nascimento;
        this.nome = nome;
        this.senha = senha;
        this.urlFoto = urlFoto;

        storage.put(this.id, this);
    }
    private Boolean validarID(UUID id) {
        return storage.containsKey(id);
    }

    public static Boolean validarSintaxeEmail(String email) {
        String regex = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$";
        return Pattern.matches(regex, email);
    }

    public static Boolean validarEmail(String email) {
        return storage.values().stream().anyMatch(c -> c.email.equalsIgnoreCase(email));
    }

    public Boolean verificarMudancaEmail(String email){
        if (email.equals(this.email)) {
            return false;
        }
        return true;
    };

    public static Boolean validarCPF(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }

    public static Boolean validarPassaporte(String passaporte, Date dataValidadePassaporte, String pais) {
        if (passaporte == null || passaporte.isEmpty()) return false;
        Date agora = new Date();
        return dataValidadePassaporte != null && dataValidadePassaporte.after(agora);
    }

    public static Boolean validarNacionalidade(String nacionalidade, String cpf, String passaporte, Date dataValidadePassaporte, String pais) {
        if ("brasileira".equalsIgnoreCase(nacionalidade)) {
            return Ciclista.validarCPF(cpf);
        }
        if (!"brasileira".equalsIgnoreCase(nacionalidade) && "brasil".equalsIgnoreCase(pais)) {
            return false;
        }
        return Ciclista.validarPassaporte(passaporte, dataValidadePassaporte, pais);
    }

    public static Boolean validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return false;
        return nome.trim().split(" ").length >= 2;
    }

    public static Boolean validarSenha(String senhaA, String senhaB) {
        if (senhaA == null || senhaB == null || !senhaA.equals(senhaB)) return false;
        String regex = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$";
        return senhaA.matches(regex);
    }

    public static List<Ciclista> listarTodos() {
        return new ArrayList<>(storage.values());
    }

    public static Ciclista getCiclistaPorId(UUID id) {
        return storage.get(id);
    }

    public void alterarStatusCiclista(Boolean status) {
        this.status = status;
    }

    public boolean verificarPermissaoAluguel() {
        return (this.status && this.statusPermicaoAluguel);
    }

    public static void restaurar() {
        Mockito.reset(mockRepo);
        storage.clear();
    }

    public Object retornarBicicletaAlugada () {
        if (bicicletaId != null) {
            // Cria um Map para simular o objeto JSON com o ID da bicicleta
            Map<String, Object> bicicletaData = new HashMap<>();
            bicicletaData.put("id", this.bicicletaId.toString());

            // return Bicicleta.retornarDadosId(bicicletaId);

            return bicicletaData; // Retorna o Map simples
        }

        return null;
    }
}
