package br.com.cinema.frame.domain.shared.cliente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository {

    void salvar(Cliente cliente);
    Optional<Cliente> buscarPorId(UUID id);
    Optional<Cliente> buscarPorEmail(String email);
    List<Cliente> listarTodos();
    void remover(UUID id);
}
