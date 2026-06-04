package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBomboniere;
import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.portal.pedido.Pedido;
import br.com.cinema.frame.domain.portal.pedido.PedidoRepository;
import br.com.cinema.frame.infrastructure.bomboniere.ProdutoJpaRepository;
import br.com.cinema.frame.infrastructure.grade.FilmeJpaRepository;
import br.com.cinema.frame.infrastructure.grade.SessaoJpaRepository;
import br.com.cinema.frame.infrastructure.sala.SalaJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PedidoRepositoryAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpa;
    private final IngressoJpaRepository ingressoJpa;
    private final PedidoProdutoJpaRepository produtoJpa;
    private final PedidoItemPendenteJpaRepository itensPendentesJpa;
    private final ProdutoJpaRepository produtoInfoJpa;
    private final SessaoJpaRepository sessaoJpa;
    private final FilmeJpaRepository filmeJpa;
    private final SalaJpaRepository salaJpa;

    public PedidoRepositoryAdapter(PedidoJpaRepository jpa,
                                   IngressoJpaRepository ingressoJpa,
                                   PedidoProdutoJpaRepository produtoJpa,
                                   PedidoItemPendenteJpaRepository itensPendentesJpa,
                                   ProdutoJpaRepository produtoInfoJpa,
                                   SessaoJpaRepository sessaoJpa,
                                   FilmeJpaRepository filmeJpa,
                                   SalaJpaRepository salaJpa) {
        this.jpa = jpa;
        this.ingressoJpa = ingressoJpa;
        this.produtoJpa = produtoJpa;
        this.itensPendentesJpa = itensPendentesJpa;
        this.produtoInfoJpa = produtoInfoJpa;
        this.sessaoJpa = sessaoJpa;
        this.filmeJpa = filmeJpa;
        this.salaJpa = salaJpa;
    }

    @Override
    @Transactional
    public void salvar(Pedido pedido) {
        PedidoJpa entity = jpa.findById(pedido.getId()).orElseGet(() -> PedidoJpa.fromDomain(pedido));
        entity.setReservaId(pedido.getReservaId());
        entity.setFinalizado(pedido.isFinalizado());
        if (entity.getDataSessao() == null) entity.setDataSessao(LocalDate.now());
        jpa.save(entity);

        ingressoJpa.deleteByPedidoId(pedido.getId());
        pedido.getIngressos().forEach(i -> ingressoJpa.save(IngressoJpa.fromDomain(i, pedido.getId())));

        if (!pedido.isFinalizado()) {
            // Pedido em andamento: persiste produtos pendentes para restaurar na próxima chamada
            itensPendentesJpa.deleteByPedidoId(pedido.getId());
            pedido.getProdutos().forEach(p ->
                    itensPendentesJpa.save(new PedidoItemPendenteJpa(
                            pedido.getId(), p.getId(), p.getNome(), p.getPreco())));
        } else {
            // Pedido finalizado: move pendentes para tabela de vendas com data e limpa pendentes
            itensPendentesJpa.deleteByPedidoId(pedido.getId());
            if (!pedido.getProdutos().isEmpty()) {
                produtoJpa.deleteByPedidoId(pedido.getId());
                LocalDate hoje = LocalDate.now();
                pedido.getProdutos().forEach(p ->
                        produtoJpa.save(new PedidoProdutoJpa(
                                pedido.getId(), p.getId(), p.getNome(), p.getPreco(), hoje)));
            }
        }
    }

    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return jpa.findById(id).map(p -> {
            Sessao sessao = buscarSessao(p.getSessaoId());
            List<Ingresso> ingressos = ingressoJpa.findByPedidoId(id).stream()
                    .map(i -> Ingresso.reconstituir(i.getId(), sessao, TipoIngresso.valueOf(i.getTipo())))
                    .toList();
            // Restaura produtos pendentes para que o pedido mantenha estado entre chamadas
            List<ProdutoDaBomboniere> produtos = itensPendentesJpa.findByPedidoId(id).stream()
                    .map(item -> produtoInfoJpa.findById(item.getProdutoId())
                            .map(pj -> pj.toDomain())
                            .orElse(null))
                    .filter(prod -> prod != null)
                    .toList();
            return Pedido.reconstituirComTudo(id, sessao, p.getClienteId(), p.getReservaId(), ingressos, produtos);
        });
    }

    @Override
    public List<Pedido> buscarFinalizadosPorClienteAPartirDe(UUID clienteId, LocalDate dataMinima) {
        return jpa.findAtivosDoCliente(clienteId, dataMinima).stream()
                .map(p -> {
                    Sessao sessao = buscarSessao(p.getSessaoId());
                    List<Ingresso> ingressos = ingressoJpa.findByPedidoId(p.getId()).stream()
                            .map(i -> Ingresso.reconstituir(i.getId(), sessao, TipoIngresso.valueOf(i.getTipo())))
                            .toList();
                    return p.toDomain(sessao, ingressos);
                })
                .toList();
    }

    @Override
    public List<Pedido> buscarFinalizadosPorCliente(UUID clienteId) {
        return jpa.findByClienteIdAndFinalizadoTrue(clienteId).stream()
                .map(p -> {
                    Sessao sessao = buscarSessao(p.getSessaoId());
                    List<Ingresso> ingressos = ingressoJpa.findByPedidoId(p.getId()).stream()
                            .map(i -> Ingresso.reconstituir(i.getId(), sessao, TipoIngresso.valueOf(i.getTipo())))
                            .toList();
                    return p.toDomain(sessao, ingressos);
                })
                .toList();
    }

    @Override
    public List<Pedido> listarTodos() {
        return jpa.findAll().stream()
                .map(p -> {
                    Sessao sessao = buscarSessao(p.getSessaoId());
                    List<Ingresso> ingressos = ingressoJpa.findByPedidoId(p.getId()).stream()
                            .map(i -> Ingresso.reconstituir(i.getId(), sessao, TipoIngresso.valueOf(i.getTipo())))
                            .toList();
                    return p.toDomain(sessao, ingressos);
                })
                .toList();
    }

    @Override
    public void remover(UUID id) {
        itensPendentesJpa.deleteByPedidoId(id);
        ingressoJpa.deleteByPedidoId(id);
        jpa.deleteById(id);
    }

    private Sessao buscarSessao(UUID sessaoId) {
        var s = sessaoJpa.findById(sessaoId)
                .orElseThrow(() -> new IllegalStateException("Sessão não encontrada: " + sessaoId));
        var filme = filmeJpa.findById(s.getFilmeId())
                .orElseThrow(() -> new IllegalStateException("Filme não encontrado")).toDomain();
        var sala = salaJpa.findById(s.getSalaId())
                .orElseThrow(() -> new IllegalStateException("Sala não encontrada")).toDomain();
        return s.toDomain(filme, sala);
    }
}
