package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.Insumo;
import br.com.cinema.frame.domain.backoffice.bomboniere.InsumoRepository;
import org.springframework.stereotype.Repository;
import br.com.cinema.frame.infrastructure.bomboniere.InsumoJpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class InsumoRepositoryAdapter
        implements InsumoRepository {

    private final InsumoJpaRepository jpa;

    public InsumoRepositoryAdapter(
            InsumoJpaRepository jpa
    ) {
        this.jpa = jpa;
    }

    @Override
public void salvar(Insumo insumo) {
    // Busca se já existe para garantir que é uma atualização ou cria novo
    InsumoJpa jpaEntity = jpa.findById(insumo.getId())
            .orElse(new InsumoJpa()); // Se não achar, cria um novo
            
    // Atualiza os campos
    jpaEntity.setId(insumo.getId()); // Certifique-se de ter o setter setId no InsumoJpa
    jpaEntity.setNome(insumo.getNome());
    jpaEntity.setUnidade(insumo.getUnidade());
    jpaEntity.setQuantidadeEmEstoque(insumo.getQuantidadeEmEstoque());
    jpaEntity.setNivelCritico(insumo.getNivelCritico());
    
    jpa.save(jpaEntity);
}

    @Override
    public Optional<Insumo> buscarPorId(UUID id) {

        return jpa.findById(id)
                .map(InsumoJpa::toDomain);
    }

    @Override
    public Optional<Insumo> buscarPorNome(String nome) {

        return jpa.findByNome(nome)
                .map(InsumoJpa::toDomain);
    }

    @Override
    public List<Insumo> listarTodos() {

        return jpa.findAll()
                .stream()
                .map(InsumoJpa::toDomain)
                .toList();
    }

    @Override
    public List<Insumo> listarEstoqueCritico() {
       
        return jpa.findAll().stream() 
            .filter(j -> j.getQuantidadeEmEstoque() <= j.getNivelCritico()) 
            .map(InsumoJpa::toDomain)
            .toList();
    }
}