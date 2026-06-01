package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.infrastructure.grade.FilmeJpaRepository;
import br.com.cinema.frame.infrastructure.grade.SessaoJpaRepository;
import br.com.cinema.frame.infrastructure.sala.SalaJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class IngressoRepositoryAdapter implements IngressoRepository {

    private final IngressoJpaRepository jpa;
    private final SessaoJpaRepository sessaoJpa;
    private final FilmeJpaRepository filmeJpa;
    private final SalaJpaRepository salaJpa;

    public IngressoRepositoryAdapter(IngressoJpaRepository jpa,
                                     SessaoJpaRepository sessaoJpa,
                                     FilmeJpaRepository filmeJpa,
                                     SalaJpaRepository salaJpa) {
        this.jpa = jpa;
        this.sessaoJpa = sessaoJpa;
        this.filmeJpa = filmeJpa;
        this.salaJpa = salaJpa;
    }

    @Override
    public void salvar(Ingresso ingresso) {
        // Atualiza campos mutáveis (ex: utilizado) sem alterar pedidoId
        jpa.findById(ingresso.getId()).ifPresent(e -> {
            e.setUtilizado(ingresso.isUtilizado());
            jpa.save(e);
        });
    }

    @Override
    public Optional<Ingresso> buscarPorId(UUID id) {
        
        return jpa.findByIdWithLock(id).map(e -> e.toDomain(buscarSessao(e.getSessaoId())));
    }

    @Override
    public List<Ingresso> buscarPorSessao(Sessao sessao) {
        return jpa.findBySessaoId(sessao.getId()).stream()
                .map(e -> e.toDomain(sessao))
                .toList();
    }

    @Override
    public void remover(UUID id) {
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