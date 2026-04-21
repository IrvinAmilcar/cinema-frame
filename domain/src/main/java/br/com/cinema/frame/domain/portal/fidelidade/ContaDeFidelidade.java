package br.com.cinema.frame.domain.portal.fidelidade;

import java.util.UUID;

public class ContaDeFidelidade {

    private final UUID id;
    private final UUID clienteId;
    private int saldoDePontos;

    public ContaDeFidelidade(UUID clienteId) {
        if (clienteId == null)
            throw new IllegalArgumentException("ID do cliente não pode ser nulo");

        this.id = UUID.randomUUID();
        this.clienteId = clienteId;
        this.saldoDePontos = 0;
    }

    public void adicionarPontos(int pontos) {
        if (pontos < 0)
            throw new IllegalArgumentException("Pontos a adicionar não podem ser negativos");

        this.saldoDePontos += pontos;
    }

    public void removerPontos(int pontos) {
        if (pontos <= 0)
            throw new IllegalArgumentException("Pontos a remover devem ser positivos");
        if (pontos > saldoDePontos)
            throw new IllegalStateException("Saldo de pontos insuficiente para resgate");

        this.saldoDePontos -= pontos;
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public int getSaldoDePontos() { return saldoDePontos; }
}
