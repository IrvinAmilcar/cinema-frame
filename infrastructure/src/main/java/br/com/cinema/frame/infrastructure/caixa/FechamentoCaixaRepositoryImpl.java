package br.com.cinema.frame.infrastructure.caixa;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import br.com.cinema.frame.domain.backoffice.caixa.FechamentoCaixa;
import br.com.cinema.frame.domain.backoffice.caixa.FechamentoCaixaRepository;

@Repository
public class FechamentoCaixaRepositoryImpl implements FechamentoCaixaRepository {

    private final FechamentoCaixaJpaRepository jpa;

    public FechamentoCaixaRepositoryImpl(FechamentoCaixaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(FechamentoCaixa fechamento) {
        jpa.save(toEntity(fechamento));
    }

    @Override
    public Optional<FechamentoCaixa> buscarPorData(LocalDate data) {
        return jpa.findByData(data).map(this::toDomain);
    }

    @Override
    public boolean existeFechamentoParaData(LocalDate data) {
        return jpa.existsByData(data);
    }

    private FechamentoCaixaEntity toEntity(FechamentoCaixa f) {
        return new FechamentoCaixaEntity(
                f.getId(),
                f.getData(),
                f.getTotalVendas(),
                f.getTotalIngressos(),
                f.getTotalSessoes(),
                f.getTaxaOcupacaoMedia(),
                f.getMomentoFechamento()
        );
    }

    private FechamentoCaixa toDomain(FechamentoCaixaEntity e) {
        return new FechamentoCaixa(
                e.getId(),
                e.getData(),
                e.getTotalVendas(),
                e.getTotalIngressos(),
                e.getTotalSessoes(),
                e.getTaxaOcupacaoMedia(),
                e.getMomentoFechamento()
        );
    }
}