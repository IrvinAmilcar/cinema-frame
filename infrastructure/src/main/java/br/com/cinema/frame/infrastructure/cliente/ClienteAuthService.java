package br.com.cinema.frame.infrastructure.cliente;

import br.com.cinema.frame.domain.portal.cliente.Cliente;
import br.com.cinema.frame.domain.portal.cliente.ClienteAuthPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClienteAuthService implements ClienteAuthPort {

    private final ClienteJpaRepository jpa;

    public ClienteAuthService(ClienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public boolean emailJaCadastrado(String email) {
        return jpa.findByEmail(email).isPresent();
    }

    @Override
    public Cliente cadastrar(String nome, String email, String senha, LocalDate dataNascimento) {
        Cliente cliente = new Cliente(nome, email, dataNascimento);
        jpa.save(ClienteJpa.fromDomain(cliente, senha));
        return cliente;
    }

    @Override
    public Optional<Cliente> autenticar(String email, String senha) {
        return jpa.findByEmail(email)
                .filter(c -> c.getSenha().equals(senha))
                .map(ClienteJpa::toDomain);
    }

    @Override
    public Optional<Cliente> buscarPorId(UUID clienteId) {
        return jpa.findById(clienteId).map(ClienteJpa::toDomain);
    }
}
