package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class FidelidadeService {

    private static final int PONTOS_POR_REAL = 1; // 1 ponto por real

    private final FidelidadeRepository fidelidadeRepository;
    private final BeneficioRepository beneficioRepository;

    public FidelidadeService(FidelidadeRepository fidelidadeRepository,
                             BeneficioRepository beneficioRepository) {
        if (fidelidadeRepository == null) throw new IllegalArgumentException("FidelidadeRepository é obrigatório");
        if (beneficioRepository == null) throw new IllegalArgumentException("BeneficioRepository é obrigatório");
        this.fidelidadeRepository = fidelidadeRepository;
        this.beneficioRepository = beneficioRepository;
    }

    public void acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        if (valorGasto <= 0) throw new IllegalArgumentException("Valor gasto deve ser positivo");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElse(new PontosCliente(clienteId));

        int pontosGanhos = (int) Math.floor(valorGasto * PONTOS_POR_REAL);
        LocalDate validade = hoje.plusMonths(12);
        pontos.acumularPontos(pontosGanhos, validade);

        fidelidadeRepository.salvar(pontos);
    }

    public int consultarSaldo(UUID clienteId, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente não possui conta de fidelidade"));

        pontos.expirarPontosVencidos(hoje);
        fidelidadeRepository.salvar(pontos);

        return pontos.getSaldoAtivo();
    }

    public List<Beneficio> verificarBeneficios(UUID clienteId, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente não possui conta de fidelidade"));

        pontos.expirarPontosVencidos(hoje);
        fidelidadeRepository.salvar(pontos);

        DayOfWeek diaDaSemana = hoje.getDayOfWeek();
        return beneficioRepository.listarTodos().stream()
                .filter(b -> pontos.getSaldoAtivo() >= b.getPontosNecessarios())
                .filter(b -> b.disponivelNoDia(diaDaSemana))
                .toList();
    }

    public void resgatarBeneficio(UUID clienteId, UUID beneficioId, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        if (beneficioId == null) throw new IllegalArgumentException("BeneficioId é obrigatório");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente não possui conta de fidelidade"));

        Beneficio beneficio = beneficioRepository.buscarPorId(beneficioId)
                .orElseThrow(() -> new IllegalArgumentException("Benefício não encontrado"));

        if (!beneficio.disponivelNoDia(hoje.getDayOfWeek())) {
            throw new IllegalStateException("Benefício não disponível neste dia da semana");
        }

        pontos.expirarPontosVencidos(hoje);

        if (pontos.getSaldoAtivo() < beneficio.getPontosNecessarios()) {
            throw new IllegalStateException("Pontos insuficientes para resgatar o benefício");
        }

        pontos.debitarPontos(beneficio.getPontosNecessarios());
        fidelidadeRepository.salvar(pontos);
    }
}
