package br.com.cinema.frame.infrastructure.cliente;

import br.com.cinema.frame.domain.portal.cliente.Cliente;
import br.com.cinema.frame.domain.portal.cliente.ClienteRepository;
import br.com.cinema.frame.domain.shared.cliente.ClienteId;
import org.springframework.stereotype.Repository;

import java.time.MonthDay;
import java.util.List;
import java.util.Optional;

@Repository
public class ClienteRepositoryAdapter implements ClienteRepository {

    private final ClienteJpaRepository jpa;

    public ClienteRepositoryAdapter(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    public void salvarComSenha(Cliente cliente, String senha) {
        jpa.save(ClienteJpa.fromDomain(cliente, senha));
    }

    public Optional<ClienteJpa> buscarJpaPorEmail(String email) {
        return jpa.findByEmail(email);
    }

    @Override
    public void salvar(Cliente cliente) {
        jpa.findById(cliente.getId().getValor()).ifPresent(existing ->
                jpa.save(ClienteJpa.fromDomain(cliente, existing.getSenha())));
    }

    @Override
    public Optional<Cliente> buscarPorId(ClienteId id) {
        return jpa.findById(id.getValor()).map(ClienteJpa::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorEmail(String email) {
        return jpa.findByEmail(email).map(ClienteJpa::toDomain);
    }

    @Override
    public List<Cliente> listarTodos() {
        return jpa.findAll().stream().map(ClienteJpa::toDomain).toList();
    }

    @Override
    public void remover(ClienteId id) {
        jpa.deleteById(id.getValor());
    }

    @Override
    public Optional<MonthDay> buscarAniversarioPorCliente(ClienteId id) {
        return jpa.findById(id.getValor())
                .map(c -> MonthDay.of(c.getDataNascimento().getMonth(),
                        c.getDataNascimento().getDayOfMonth()));
    }
}
