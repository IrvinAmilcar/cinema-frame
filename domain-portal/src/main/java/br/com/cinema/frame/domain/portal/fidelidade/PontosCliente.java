package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PontosCliente {

    private static final int LIMITE_ACUMULO_MENSAL = 10000;
    private static final double VALOR_BONUS_NIVEL1 = 100.0;
    private static final double VALOR_BONUS_NIVEL2 = 200.0;
    private static final double VALOR_BONUS_NIVEL3 = 500.0;
    private static final double MULTIPLICADOR_NIVEL1 = 1.5;
    private static final double MULTIPLICADOR_NIVEL2 = 2.0;
    private static final double MULTIPLICADOR_NIVEL3 = 3.0;

    private final UUID clienteId;
    private int saldoAtivo;
    private final List<LancamentoPontos> lancamentos;
    private final List<RegistroResgate> historicoResgates;

    public PontosCliente(UUID clienteId) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        this.clienteId = clienteId;
        this.saldoAtivo = 0;
        this.lancamentos = new ArrayList<>();
        this.historicoResgates = new ArrayList<>();
    }

    public static PontosCliente reconstituir(UUID clienteId, int saldoAtivo,
                                              List<LancamentoPontos> lancamentos,
                                              List<RegistroResgate> historicoResgates) {
        PontosCliente p = new PontosCliente(clienteId);
        p.lancamentos.clear();
        p.lancamentos.addAll(lancamentos);
        p.historicoResgates.clear();
        p.historicoResgates.addAll(historicoResgates);
        p.saldoAtivo = saldoAtivo;
        return p;
    }

    public int calcularPontosAcumuladosNoMes(int mes, int ano) {
        return lancamentos.stream()
                .filter(l -> l.getDataCriacao().getMonthValue() == mes
                          && l.getDataCriacao().getYear() == ano)
                .mapToInt(LancamentoPontos::getPontosOriginais)
                .sum();
    }

    public boolean acumularPontos(int pontos, LocalDate validade, LocalDate hoje) {
        return acumularPontos(pontos, validade, hoje, "Compra de ingresso");
    }

    public boolean acumularPontos(int pontos, LocalDate validade, LocalDate hoje, String descricao) {
        if (pontos <= 0) throw new IllegalArgumentException("Pontos devem ser positivos");
        if (validade == null) throw new IllegalArgumentException("Validade é obrigatória");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        int jaAcumuladoNoMes = calcularPontosAcumuladosNoMes(hoje.getMonthValue(), hoje.getYear());
        int disponivelNoMes = LIMITE_ACUMULO_MENSAL - jaAcumuladoNoMes;
        if (disponivelNoMes <= 0) return false; // limite atingido, não lança exceção

        int pontosAcumulaveis = Math.min(pontos, disponivelNoMes);
        lancamentos.add(new LancamentoPontos(pontosAcumulaveis, validade, hoje, descricao));
        saldoAtivo += pontosAcumulaveis;
        return true;
    }

    public void expirarPontosVencidos(LocalDate hoje) {
        for (LancamentoPontos l : lancamentos) {
            if (!l.isExpirado() && l.getValidade().isBefore(hoje)) {
                saldoAtivo -= l.getSaldo();
                l.expirar();
            }
        }
    }

    public void debitarPontos(int pontos, UUID beneficioId, LocalDate hoje) {
        if (pontos <= 0) throw new IllegalArgumentException("Pontos a debitar devem ser positivos");
        if (saldoAtivo < pontos) throw new IllegalStateException("Saldo insuficiente de pontos");

        long resgatesDoMes = historicoResgates.stream()
                .filter(r -> r.getBeneficioId().equals(beneficioId))
                .filter(r -> r.getData().getMonthValue() == hoje.getMonthValue()
                          && r.getData().getYear() == hoje.getYear())
                .count();

        if (resgatesDoMes >= 3)
            throw new IllegalStateException("Limite de 3 resgates do mesmo benefício por mês atingido");

        debitarFIFO(pontos);
        saldoAtivo -= pontos;
        historicoResgates.add(new RegistroResgate(beneficioId, pontos, hoje));
    }

    public void debitarPontosDeCompra(int pontos, LocalDate hoje) {
        if (pontos <= 0) throw new IllegalArgumentException("Pontos a debitar devem ser positivos");
        if (saldoAtivo < pontos) throw new IllegalStateException("Saldo insuficiente de pontos");
        debitarFIFO(pontos);
        saldoAtivo -= pontos;
        historicoResgates.add(new RegistroResgate(new UUID(0L, 0L), pontos, hoje));
    }

    private void debitarFIFO(int pontos) {
        int restante = pontos;
        for (LancamentoPontos l : lancamentos) {
            if (restante == 0) break;
            if (l.getSaldo() > 0 && !l.isExpirado()) {
                int consumido = Math.min(l.getSaldo(), restante);
                l.debitar(consumido);
                restante -= consumido;
            }
        }
    }

    public static int calcularPontosComBonus(double valorGasto, int pontosBase) {
        if (valorGasto >= VALOR_BONUS_NIVEL3) return (int) Math.floor(pontosBase * MULTIPLICADOR_NIVEL3);
        if (valorGasto >= VALOR_BONUS_NIVEL2) return (int) Math.floor(pontosBase * MULTIPLICADOR_NIVEL2);
        if (valorGasto >= VALOR_BONUS_NIVEL1) return (int) Math.floor(pontosBase * MULTIPLICADOR_NIVEL1);
        return pontosBase;
    }

    public void registrarResgateNoHistorico(RegistroResgate registro) {
        if (registro == null) throw new IllegalArgumentException("Registro é obrigatório");
        historicoResgates.add(registro);
    }

    public int getSaldoAtivo() { return saldoAtivo; }
    public UUID getClienteId() { return clienteId; }
    public List<LancamentoPontos> getLancamentos() { return List.copyOf(lancamentos); }
    public List<RegistroResgate> getHistoricoResgates() { return List.copyOf(historicoResgates); }
}