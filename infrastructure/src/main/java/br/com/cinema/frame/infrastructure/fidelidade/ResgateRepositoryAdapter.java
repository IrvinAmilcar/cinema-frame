package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.portal.fidelidade.RegistroResgate;
import br.com.cinema.frame.domain.portal.fidelidade.RegistroResgateRepository;

@Repository
public class ResgateRepositoryAdapter implements RegistroResgateRepository {

    private final ResgateJpaRepository jpa;

    public ResgateRepositoryAdapter(ResgateJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(UUID clienteId, RegistroResgate registro) {
        jpa.save(ResgateJpa.fromDomain(clienteId, registro));
    }

    @Override
    public List<RegistroResgate> buscarPorClienteEMes(UUID clienteId, int mes, int ano) {
        return jpa.findByClienteIdAndMesAno(clienteId, mes, ano).stream()
                .map(ResgateJpa::toDomain)
                .toList();
    }

    @Override
    public List<RegistroResgate> buscarPorCliente(UUID clienteId) {
        return jpa.findByClienteId(clienteId).stream()
                .map(ResgateJpa::toDomain)
                .toList();
    }
}
