package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.UUID;

import br.com.cinema.frame.domain.portal.cliente.ClienteRepository;
import br.com.cinema.frame.domain.shared.cliente.ClienteId;

public class FidelidadeService {

    private static final int PONTOS_POR_REAL = 1;

    private final FidelidadeRepository fidelidadeRepository;
    private final BeneficioRepository beneficioRepository;
    private final ClienteRepository clienteRepository;
    private final RegistroResgateRepository resgateRepository;

    public FidelidadeService(FidelidadeRepository fidelidadeRepository,
                             BeneficioRepository beneficioRepository,
                             ClienteRepository clienteRepository,
                             RegistroResgateRepository resgateRepository) {
        if (fidelidadeRepository == null) throw new IllegalArgumentException("FidelidadeRepository é obrigatório");
        if (beneficioRepository == null) throw new IllegalArgumentException("BeneficioRepository é obrigatório");
        if (clienteRepository == null) throw new IllegalArgumentException("ClienteRepository é obrigatório");
        if (resgateRepository == null) throw new IllegalArgumentException("RegistroResgateRepository é obrigatório");
        this.fidelidadeRepository = fidelidadeRepository;
        this.beneficioRepository = beneficioRepository;
        this.clienteRepository = clienteRepository;
        this.resgateRepository = resgateRepository;
    }

    public void acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje) {
        acumularPontos(clienteId, valorGasto, hoje, null, null, 0);
    }

    public void acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje, String tituloFilme) {
        acumularPontos(clienteId, valorGasto, hoje, tituloFilme, null, 0);
    }

    public void acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje,
                                String tituloFilme, String horario, int salaNumero) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        if (valorGasto <= 0) throw new IllegalArgumentException("Valor gasto deve ser positivo");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElse(new PontosCliente(clienteId));

        int pontosBase = (int) Math.floor(valorGasto * PONTOS_POR_REAL);
        int pontosComBonus = PontosCliente.calcularPontosComBonus(valorGasto, pontosBase);

        boolean ehAniversario = clienteRepository.buscarAniversarioPorCliente(new ClienteId(clienteId))
                .map(aniversario -> aniversario.equals(MonthDay.from(hoje)))
                .orElse(false);
        if (ehAniversario) pontosComBonus = pontosComBonus * 2;

        String descricao;
        if (tituloFilme != null && !tituloFilme.isBlank()) {
            StringBuilder sb = new StringBuilder("Compra de ingresso — ").append(tituloFilme);
            if (horario != null && !horario.isBlank()) sb.append(" · ").append(horario);
            if (salaNumero > 0) sb.append(" · Sala ").append(salaNumero);
            descricao = sb.toString();
        } else {
            descricao = "Compra de ingresso";
        }

        LocalDate validade = hoje.plusMonths(12);
        pontos.acumularPontos(pontosComBonus, validade, hoje, descricao);
        fidelidadeRepository.salvar(pontos);
    }

    public void usarPontosNaCompra(UUID clienteId, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente não possui conta de fidelidade"));

        pontos.expirarPontosVencidos(hoje);
        int saldo = pontos.getSaldoAtivo();
        if (saldo <= 0) return;

        pontos.debitarPontosDeCompra(saldo, hoje);
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

    public List<LancamentoPontos> consultarExtrato(UUID clienteId) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente não possui conta de fidelidade"));
        return pontos.getLancamentos();
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

        if (!beneficio.disponivelNoDia(hoje.getDayOfWeek()))
            throw new IllegalStateException("Benefício não disponível neste dia da semana");

        pontos.expirarPontosVencidos(hoje);

        if (pontos.getSaldoAtivo() < beneficio.getPontosNecessarios())
            throw new IllegalStateException("Pontos insuficientes para resgatar o benefício");

        List<RegistroResgate> resgatesHoje = resgateRepository.buscarPorClienteEMes(
                clienteId, hoje.getMonthValue(), hoje.getYear()
        ).stream().filter(r -> r.getData().equals(hoje)).toList();

        for (RegistroResgate registro : resgatesHoje) {
            Beneficio jaResgatado = beneficioRepository.buscarPorId(registro.getBeneficioId()).orElse(null);
            if (jaResgatado != null && beneficio.incompativelCom(jaResgatado))
                throw new IllegalStateException("Benefício incompatível com '" + jaResgatado.getNome() + "' já resgatado hoje");
        }

        pontos.debitarPontos(beneficio.getPontosNecessarios(), beneficioId, hoje);
        fidelidadeRepository.salvar(pontos);
        resgateRepository.salvar(clienteId, new RegistroResgate(beneficioId, beneficio.getPontosNecessarios(), hoje));
    }

    public List<RegistroResgate> consultarHistoricoResgates(UUID clienteId) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        return resgateRepository.buscarPorCliente(clienteId);
    }
}