package br.com.cinema.frame.domain.portal.cliente;

import br.com.cinema.frame.domain.shared.cliente.ClienteId;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {

    void salvar(Cliente cliente);
    Optional<Cliente> buscarPorId(ClienteId id);
    Optional<Cliente> buscarPorEmail(String email);
    List<Cliente> listarTodos();
    void remover(ClienteId id);
}
