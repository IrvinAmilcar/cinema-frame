package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.TipoIngresso;
import br.com.cinema.frame.domain.portal.pedido.Pedido;
import br.com.cinema.frame.domain.portal.pedido.PedidoRepository;

import java.time.LocalDate;
import br.com.cinema.frame.infrastructure.grade.FilmeJpaRepository;
import br.com.cinema.frame.infrastructure.grade.SessaoJpaRepository;
import br.com.cinema.frame.infrastructure.sala.SalaJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PedidoRepositoryAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpa;
    private final IngressoJpaRepository ingressoJpa;
    private final SessaoJpaRepository sessaoJpa;
    private final FilmeJpaRepository filmeJpa;
    private final SalaJpaRepository salaJpa;

    public PedidoRepositoryAdapter(PedidoJpaRepository jpa,
                                   IngressoJpaRepository ingressoJpa,
                                   SessaoJpaRepository sessaoJpa,
                                   FilmeJpaRepository filmeJpa,
                                   SalaJpaRepository salaJpa) {
        this.jpa = jpa;
        this.ingressoJpa = ingressoJpa;
        this.sessaoJpa = sessaoJpa;
        this.filmeJpa = filmeJpa;
        this.salaJpa = salaJpa;
    }

    @Override
    @Transactional
    public void salvar(Pedido pedido) {
        PedidoJpa entity = jpa.findById(pedido.getId()).orElseGet(() -> {
            PedidoJpa novo = PedidoJpa.fromDomain(pedido);
            return novo;
        });
        // Atualiza os campos mutáveis na entidade gerenciada pelo JPA
        entity.setReservaId(pedido.getReservaId());
        entity.setFinalizado(pedido.isFinalizado());
        if (entity.getDataSessao() == null) entity.setDataSessao(LocalDate.now());
        jpa.save(entity);
        ingressoJpa.deleteByPedidoId(pedido.getId());
        pedido.getIngressos().forEach(i ->
                ingressoJpa.save(IngressoJpa.fromDomain(i, pedido.getId())));
    }

    @Override
    public Optional<Pedido> buscarPorId(UUID id) {
        return jpa.findById(id).map(p -> {
            Sessao sessao = buscarSessao(p.getSessaoId());
            List<Ingresso> ingressos = ingressoJpa.findByPedidoId(id).stream()
                    .map(i -> Ingresso.reconstituir(i.getId(), sessao, TipoIngresso.valueOf(i.getTipo())))
                    .toList();
            return p.toDomain(sessao, ingressos);
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
        ingressoJpa.deleteByPedidoId(id);
        jpa.deleteById(id);
    }

    private Sessao buscarSessao(UUID sessaoId) {
        var sessaoJpaOpt = sessaoJpa.findById(sessaoId)
                .orElseThrow(() -> new IllegalStateException("Sessão não encontrada: " + sessaoId));
        var filme = filmeJpa.findById(sessaoJpaOpt.getFilmeId())
                .orElseThrow(() -> new IllegalStateException("Filme não encontrado"))
                .toDomain();
        var sala = salaJpa.findById(sessaoJpaOpt.getSalaId())
                .orElseThrow(() -> new IllegalStateException("Sala não encontrada"))
                .toDomain();
        return sessaoJpaOpt.toDomain(filme, sala);
    }
}
