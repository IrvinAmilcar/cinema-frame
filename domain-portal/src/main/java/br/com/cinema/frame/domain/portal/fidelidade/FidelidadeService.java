package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.List;
import java.util.UUID;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.portal.cliente.ClienteRepository;
import br.com.cinema.frame.domain.shared.cliente.ClienteId;


public class FidelidadeService implements FidelidadeServiceInterface {

    private static final int PONTOS_POR_REAL = 1;

    private final FidelidadeRepository fidelidadeRepository;
    private final BeneficioRepository beneficioRepository;
    private final ClienteRepository clienteRepository;
    private final RegistroResgateRepository resgateRepository;
    private final BombonieresService bombonieresService;

    public FidelidadeService(FidelidadeRepository fidelidadeRepository,
                             BeneficioRepository beneficioRepository,
                             ClienteRepository clienteRepository,
                             RegistroResgateRepository resgateRepository) {
        this(fidelidadeRepository, beneficioRepository, clienteRepository, resgateRepository, null);
    }

    public FidelidadeService(FidelidadeRepository fidelidadeRepository,
                             BeneficioRepository beneficioRepository,
                             ClienteRepository clienteRepository,
                             RegistroResgateRepository resgateRepository,
                             BombonieresService bombonieresService) {
        if (fidelidadeRepository == null) throw new IllegalArgumentException("FidelidadeRepository é obrigatório");
        if (beneficioRepository == null) throw new IllegalArgumentException("BeneficioRepository é obrigatório");
        if (clienteRepository == null) throw new IllegalArgumentException("ClienteRepository é obrigatório");
        if (resgateRepository == null) throw new IllegalArgumentException("RegistroResgateRepository é obrigatório");
        this.fidelidadeRepository = fidelidadeRepository;
        this.beneficioRepository = beneficioRepository;
        this.clienteRepository = clienteRepository;
        this.resgateRepository = resgateRepository;
        this.bombonieresService = bombonieresService;
    }

    @Override
    public boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje) {
        return acumularPontos(clienteId, valorGasto, hoje, null, null, 0);
    }

    @Override
    public boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje, String tituloFilme) {
        return acumularPontos(clienteId, valorGasto, hoje, tituloFilme, null, 0);
    }

    @Override
    public boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje,
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
        boolean acumulou = pontos.acumularPontos(pontosComBonus, validade, hoje, descricao);
        fidelidadeRepository.salvar(pontos);
        return acumulou;
    }

    @Override
    public void usarPontosNaCompra(UUID clienteId, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElse(null);
        if (pontos == null) return;

        pontos.expirarPontosVencidos(hoje);
        int saldo = pontos.getSaldoAtivo();
        if (saldo <= 0) return;

        pontos.debitarPontosDeCompra(saldo, hoje);
        fidelidadeRepository.salvar(pontos);
    }

    @Override
    public int consultarSaldo(UUID clienteId, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElse(new PontosCliente(clienteId));

        pontos.expirarPontosVencidos(hoje);
        fidelidadeRepository.salvar(pontos);
        return pontos.getSaldoAtivo();
    }

    @Override
    public List<LancamentoPontos> consultarExtrato(UUID clienteId) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElse(new PontosCliente(clienteId));

        return pontos.getLancamentos();
    }


    @Override
    public List<Beneficio> listarTodosBeneficios(UUID clienteId, LocalDate hoje) {
        return beneficioRepository.listarTodos().stream()
                .filter(b -> b.disponivelNoDia(hoje.getDayOfWeek()))
                .toList();
    }

    @Override
    public List<Beneficio> verificarBeneficios(UUID clienteId, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElse(new PontosCliente(clienteId));

        pontos.expirarPontosVencidos(hoje);
        fidelidadeRepository.salvar(pontos);

        DayOfWeek diaDaSemana = hoje.getDayOfWeek();
        return beneficioRepository.listarTodos().stream()
                .filter(b -> pontos.getSaldoAtivo() >= b.getPontosNecessarios())
                .filter(b -> b.disponivelNoDia(diaDaSemana))
                .toList();
    }

    @Override
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
        resgateRepository.salvar(clienteId, new RegistroResgate(
                beneficioId, beneficio.getPontosNecessarios(),
                LocalDateTime.now(), beneficio.getNome()));
    }

    @Override
    public String resgatarProdutoBomboniere(UUID clienteId, UUID produtoId, LocalDate hoje) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        if (produtoId == null) throw new IllegalArgumentException("ProdutoId é obrigatório");
        if (hoje == null) throw new IllegalArgumentException("Data é obrigatória");

        PontosCliente pontos = fidelidadeRepository.buscarPorCliente(clienteId)
                .orElseThrow(() -> new IllegalStateException("Cliente não possui conta de fidelidade"));

        var produto = bombonieresService.buscarProdutoPorId(produtoId);
        if (!produto.isAtivo())
            throw new IllegalStateException("Produto indisponível: " + produto.getNome());

        int pontosNecessarios = (int) Math.ceil(produto.getPreco() / 2.0 * 100);

        pontos.expirarPontosVencidos(hoje);

        if (pontos.getSaldoAtivo() < pontosNecessarios)
            throw new IllegalStateException("Pontos insuficientes. Necessário: " + pontosNecessarios
                    + " pts, disponível: " + pontos.getSaldoAtivo() + " pts");

        pontos.debitarPontosDeCompra(pontosNecessarios, hoje);
        fidelidadeRepository.salvar(pontos);

        String voucher = "VCH-FIDELIDADE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        resgateRepository.salvar(clienteId, new RegistroResgate(
                produtoId, pontosNecessarios,
                LocalDateTime.now(), produto.getNome()));
        return voucher;
    }

    @Override
    public List<RegistroResgate> consultarHistoricoResgates(UUID clienteId) {
        if (clienteId == null) throw new IllegalArgumentException("ClienteId é obrigatório");
        return resgateRepository.buscarPorCliente(clienteId);
    }

    public BombonieresService getBombonieresService() {
        return bombonieresService;
    }
}