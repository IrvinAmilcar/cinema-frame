package br.com.cinema.frame.domain.portal.cliente;

import java.time.MonthDay;
import java.util.List;
import java.util.Optional;

import br.com.cinema.frame.domain.shared.cliente.ClienteId;

public interface ClienteRepository {

    void salvar(Cliente cliente);
    Optional<Cliente> buscarPorId(ClienteId id);
    Optional<Cliente> buscarPorEmail(String email);
    List<Cliente> listarTodos();
    void remover(ClienteId id);

    Optional<MonthDay> buscarAniversarioPorCliente(ClienteId id);
}
