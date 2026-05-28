package br.com.cinema.frame.domain.portal.cliente;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ClienteAuthPort {
    boolean emailJaCadastrado(String email);
    Cliente cadastrar(String nome, String email, String senha, LocalDate dataNascimento);
    Optional<Cliente> autenticar(String email, String senha);
    Optional<Cliente> buscarPorId(UUID clienteId);
}
