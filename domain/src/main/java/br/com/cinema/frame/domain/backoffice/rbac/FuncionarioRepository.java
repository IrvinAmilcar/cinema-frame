package br.com.cinema.frame.domain.backoffice.rbac;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuncionarioRepository {

    void salvar(Funcionario funcionario);
    Optional<Funcionario> buscarPorId(UUID id);
    List<Funcionario> listarTodos();
}