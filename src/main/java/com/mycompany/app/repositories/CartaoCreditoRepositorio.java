package com.mycompany.app.repositories;

import com.mycompany.app.models.CartaoCredito;
import java.util.Optional;
import java.util.UUID;

public interface CartaoCreditoRepositorio {
    CartaoCredito save(UUID idCiclista, CartaoCredito cartao);

    Optional<CartaoCredito> findByCiclistaId(UUID idCiclista);
}