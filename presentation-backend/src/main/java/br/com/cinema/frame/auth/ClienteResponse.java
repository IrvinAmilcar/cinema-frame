package br.com.cinema.frame.auth;

import br.com.cinema.frame.domain.portal.cliente.Cliente;

import java.util.UUID;

public record ClienteResponse(UUID clienteId, String nome, String email) {
    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId().getValor(), c.getNome(), c.getEmail());
    }
}
