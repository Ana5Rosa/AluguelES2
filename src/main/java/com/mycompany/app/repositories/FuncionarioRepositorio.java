package com.mycompany.app.repositories;

import com.mycompany.app.models.Funcionario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuncionarioRepositorio {
    Funcionario getFuncionarioMatricula(UUID matricula);
    List<Funcionario> findAll();
    void save(Funcionario funcionario);
}