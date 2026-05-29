package br.com.cinema.frame.infrastructure.checkin;

import br.com.cinema.frame.domain.backoffice.checkin.RegistroDeEntrada;
import br.com.cinema.frame.domain.backoffice.checkin.RegistroDeEntradaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class RegistroDeEntradaRepositoryImpl implements RegistroDeEntradaRepository {

    private final RegistroDeEntradaJpaRepository jpa;

    public RegistroDeEntradaRepositoryImpl(RegistroDeEntradaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(RegistroDeEntrada registro) {
        RegistroDeEntradaEntity entity = toEntity(registro);
        jpa.save(entity);
    }

    @Override
    public List<RegistroDeEntrada> buscarPorSessao(UUID sessaoId) {
        return jpa.findBySessaoId(sessaoId)
                  .stream()
                  .map(this::toDomain)
                  .toList();
    }
    private RegistroDeEntradaEntity toEntity(RegistroDeEntrada r) {
        return new RegistroDeEntradaEntity(
            r.getId(),
            r.getIngressoId(),  
            r.getSessaoId(),    
            r.getMomentoEntrada(),
            r.isAutorizado(),
            r.getMotivoRecusa()
        );
    }

    private RegistroDeEntrada toDomain(RegistroDeEntradaEntity e) {
        // adapte conforme o construtor do seu RegistroDeEntrada de domínio
        return RegistroDeEntrada.reconstituir(
            e.getId(),
            e.getIngressoId(),
            e.getSessaoId(),
            e.getMomentoEntrada(),
            e.isAutorizado(),
            e.getMotivoRecusa()
        );
    }
}