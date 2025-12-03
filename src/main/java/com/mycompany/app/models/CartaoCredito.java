package com.mycompany.app.models;

import com.mycompany.app.repositories.CartaoCreditoRepositorio;
import org.mockito.Mockito;

import java.util.*;
import java.util.regex.Pattern;

public class CartaoCredito {
    private static final CartaoCreditoRepositorio mockRepo = Mockito.mock(CartaoCreditoRepositorio.class);
    private static final Map<UUID, CartaoCredito> storage = new HashMap<>();

    private UUID idCiclista;
    private String nomeTitular;
    private String numero;
    private Date validade;
    private String cvv;

    public CartaoCredito(UUID idCiclista, String nomeTitular, String numero, Date validade, String cvv) {
        this.idCiclista = idCiclista;
        this.nomeTitular = nomeTitular;
        this.numero = numero;
        this.validade = validade;
        this.cvv = cvv;

        // Simula salvamento no storage (save/update)
        storage.put(this.idCiclista, this);

        // Mockito setup (opcional, mas para manter o padrão)
        Mockito.when(mockRepo.findByCiclistaId(this.idCiclista))
                .thenReturn(Optional.of(this));
    }

    public static CartaoCredito getCartaoCreditoPorCiclistaId(UUID idCiclista) {
        return storage.get(idCiclista);
    }

    public static CartaoCredito alterarDadosCartao(UUID idCiclista, String nomeTitular, String numero, Date validade, String cvv) {
        CartaoCredito cartaoExistente = storage.get(idCiclista);

        if (cartaoExistente == null) {
            return null;
        }

        cartaoExistente.nomeTitular = nomeTitular;
        cartaoExistente.numero = numero;
        cartaoExistente.validade = validade;
        cartaoExistente.cvv = cvv;

        return cartaoExistente;
    }

    public static void restaurar() {
        Mockito.reset(mockRepo);
        storage.clear();
    }

    public static Boolean validarValidade(Date validade) {
        Date agora = new Date();
        return validade != null && validade.after(agora);
    }

    public static Boolean validarNumero(String numero) {
        return numero != null && numero.matches("\\d{16}");
    }

    public static Boolean validarCVV(String cvv) {
        return cvv != null && cvv.matches("\\d{3}");
    }

    public static Boolean validarNomeTitular(String nomeTitular) {
        if (nomeTitular == null || nomeTitular.trim().isEmpty()) return false;
        return nomeTitular.trim().split(" ").length >= 2;
    }
}