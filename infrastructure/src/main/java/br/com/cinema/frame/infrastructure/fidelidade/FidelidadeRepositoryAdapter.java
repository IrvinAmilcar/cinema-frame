package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.portal.fidelidade.FidelidadeRepository;
import br.com.cinema.frame.domain.portal.fidelidade.LancamentoPontos;
import br.com.cinema.frame.domain.portal.fidelidade.PontosCliente;
import br.com.cinema.frame.domain.portal.fidelidade.RegistroResgate;

@Repository
public class FidelidadeRepositoryAdapter implements FidelidadeRepository {

    private final PontosClienteJpaRepository jpa;
    private final ResgateJpaRepository resgateJpa;

    public FidelidadeRepositoryAdapter(PontosClienteJpaRepository jpa,
                                        ResgateJpaRepository resgateJpa) {
        this.jpa = jpa;
        this.resgateJpa = resgateJpa;
    }

    @Override
    public void salvar(PontosCliente pontosCliente) {
        // Busca o JPA existente para fazer merge (evita recriar coleções e conflito de contexto)
        PontosClienteJpa existente = jpa.findById(pontosCliente.getClienteId())
                .orElse(new PontosClienteJpa());

        existente.atualizarDe(pontosCliente);
        jpa.save(existente);
    }

    @Override
    public Optional<PontosCliente> buscarPorCliente(UUID clienteId) {
        return jpa.findById(clienteId).map(pontosJpa -> {
            List<LancamentoPontos> lancamentos = pontosJpa.getLancamentos().stream()
                    .map(LancamentoJpa::toDomain)
                    .toList();

            List<RegistroResgate> resgates = resgateJpa.findByClienteId(clienteId).stream()
                    .map(ResgateJpa::toDomain)
                    .toList();

            return PontosCliente.reconstituir(
                    pontosJpa.getClienteId(),
                    pontosJpa.getSaldoAtivo(),
                    lancamentos,
                    resgates
            );
        });
    }
}
