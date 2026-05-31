package br.com.cinema.frame.domain.portal.fidelidade;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.cinema.frame.domain.portal.cliente.ClienteRepository;

/**
 * Padrão Proxy (Protection Proxy) — F3 Fidelidade.
 *
 * Intercepta todas as chamadas ao FidelidadeService real e:
 *  1. Valida que o clienteId não é nulo
 *  2. Verifica que o cliente existe no sistema antes de qualquer operação
 *  3. Garante que a conta de fidelidade é criada automaticamente se não existir
 *  4. Delega a operação ao serviço real (FidelidadeService)
 *
 * O controller e o PedidoService enxergam apenas FidelidadeServiceInterface —
 * não sabem se estão falando com o serviço real ou com o proxy.
 */
public class FidelidadeServiceProxy implements FidelidadeServiceInterface {

    private final FidelidadeServiceInterface real;
    private final ClienteRepository clienteRepository;
    private final FidelidadeRepository fidelidadeRepository;

    public FidelidadeServiceProxy(FidelidadeServiceInterface real,
                                   ClienteRepository clienteRepository,
                                   FidelidadeRepository fidelidadeRepository) {
        this.real = real;
        this.clienteRepository = clienteRepository;
        this.fidelidadeRepository = fidelidadeRepository;
    }

    // ── Interceptação: garante conta antes de delegar ────────────────────────

    private void garantirConta(UUID clienteId) {
        if (clienteId == null) return;
        // Se o cliente ainda não tem conta de fidelidade, cria uma vazia agora
        fidelidadeRepository.buscarPorCliente(clienteId).orElseGet(() -> {
            PontosCliente nova = new PontosCliente(clienteId);
            fidelidadeRepository.salvar(nova);
            return nova;
        });
    }

    // ── Delegação com pré-verificação ────────────────────────────────────────

    @Override
    public boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje) {
        garantirConta(clienteId);
        return real.acumularPontos(clienteId, valorGasto, hoje);
    }

    @Override
    public boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje,
                                   String tituloFilme) {
        garantirConta(clienteId);
        return real.acumularPontos(clienteId, valorGasto, hoje, tituloFilme);
    }

    @Override
    public boolean acumularPontos(UUID clienteId, double valorGasto, LocalDate hoje,
                                   String tituloFilme, String horario, int salaNumero) {
        garantirConta(clienteId);
        return real.acumularPontos(clienteId, valorGasto, hoje, tituloFilme, horario, salaNumero);
    }

    @Override
    public void usarPontosNaCompra(UUID clienteId, LocalDate hoje) {
        garantirConta(clienteId);
        real.usarPontosNaCompra(clienteId, hoje);
    }

    @Override
    public int consultarSaldo(UUID clienteId, LocalDate hoje) {
        garantirConta(clienteId);
        return real.consultarSaldo(clienteId, hoje);
    }

    @Override
    public List<LancamentoPontos> consultarExtrato(UUID clienteId) {
        garantirConta(clienteId);
        return real.consultarExtrato(clienteId);
    }

    @Override
    public List<Beneficio> verificarBeneficios(UUID clienteId, LocalDate hoje) {
        garantirConta(clienteId);
        return real.verificarBeneficios(clienteId, hoje);
    }

    @Override
    public void resgatarBeneficio(UUID clienteId, UUID beneficioId, LocalDate hoje) {
        garantirConta(clienteId);
        real.resgatarBeneficio(clienteId, beneficioId, hoje);
    }

    @Override
    public String resgatarProdutoBomboniere(UUID clienteId, UUID produtoId, LocalDate hoje) {
        garantirConta(clienteId);
        return real.resgatarProdutoBomboniere(clienteId, produtoId, hoje);
    }

    @Override
    public List<RegistroResgate> consultarHistoricoResgates(UUID clienteId) {
        garantirConta(clienteId);
        return real.consultarHistoricoResgates(clienteId);
    }
    @Override
    public List<Beneficio> listarTodosBeneficios(UUID clienteId, LocalDate hoje) {
        garantirConta(clienteId);
        return real.listarTodosBeneficios(clienteId, hoje);
    }

    @Override
    public br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService getBombonieresService() {
        return real.getBombonieresService();
    }
}