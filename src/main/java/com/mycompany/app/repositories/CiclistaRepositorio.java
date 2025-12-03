package com.mycompany.app.repositories;

import com.mycompany.app.models.Ciclista;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CiclistaRepositorio {
    void save(Ciclista ciclista);
    Boolean buscarPorID(UUID id);
    Boolean buscarPorEmail(String email);
    Ciclista getCiclistaPorId(UUID id); //retornar ciclista por id
    List<Ciclista> findAll();
}
