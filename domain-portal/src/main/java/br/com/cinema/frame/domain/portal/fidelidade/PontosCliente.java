package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PontosCliente {

    private final UUID clienteId;
    private int saldoAtivo;
    private final List<LancamentoPontos> lancamentos;

    public PontosCliente(UUID clienteId) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        this.clienteId = clienteId;
        this.saldoAtivo = 0;
        this.lancamentos = new ArrayList<>();
    }

    public void acumularPontos(int pontos, LocalDate validade) {
        if (pontos <= 0) throw new IllegalArgumentException("Pontos devem ser positivos");
        if (validade == null) throw new IllegalArgumentException("Validade é obrigatória");
        lancamentos.add(new LancamentoPontos(pontos, validade));
        saldoAtivo += pontos;
    }

    public void expirarPontosVencidos(LocalDate hoje) {
        for (LancamentoPontos l : lancamentos) {
            if (!l.isExpirado() && l.getValidade().isBefore(hoje)) {
                saldoAtivo -= l.getSaldo();
                l.expirar();
            }
        }
    }

    public void debitarPontos(int pontos) {
        if (pontos <= 0) throw new IllegalArgumentException("Pontos a debitar devem ser positivos");
        if (saldoAtivo < pontos) throw new IllegalStateException("Saldo insuficiente de pontos");
        // debita dos lançamentos mais antigos primeiro 
        int restante = pontos;
        for (LancamentoPontos l : lancamentos) {
            if (restante == 0) break;
            if (l.getSaldo() > 0 && !l.isExpirado()) {
                int consumido = Math.min(l.getSaldo(), restante);
                l.debitar(consumido);
                restante -= consumido;
            }
        }
        saldoAtivo -= pontos;
    }

    public int getSaldoAtivo() { return saldoAtivo; }
    public UUID getClienteId() { return clienteId; }
    public List<LancamentoPontos> getLancamentos() { return List.copyOf(lancamentos); }
}
