package br.com.cinema.frame.domain.shared.cliente;

import java.util.UUID;

public class ClienteId {

    private final UUID valor;

    public ClienteId(UUID valor) {
        if (valor == null)
            throw new IllegalArgumentException("ClienteId não pode ser nulo");
        this.valor = valor;
    }

    public UUID getValor() { return valor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClienteId that = (ClienteId) o;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() { return valor.hashCode(); }

    @Override
    public String toString() { return valor.toString(); }
}
