package br.com.cinema.frame.infrastructure.rbac;

import br.com.cinema.frame.domain.backoffice.rbac.Funcionario;
import br.com.cinema.frame.domain.backoffice.rbac.FuncionarioRepository;
import br.com.cinema.frame.domain.backoffice.rbac.Permissao;
import br.com.cinema.frame.domain.backoffice.rbac.RoleFuncionario;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class FuncionarioRepositoryImpl implements FuncionarioRepository {

    private final FuncionarioJpaRepository jpa;

    public FuncionarioRepositoryImpl(FuncionarioJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void salvar(Funcionario funcionario) {
        jpa.save(toEntity(funcionario));
    }

    @Override
    public Optional<Funcionario> buscarPorId(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Funcionario> listarTodos() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void deletar(UUID id) {
        jpa.deleteById(id);
    }

    // ---------- mapeamentos ----------

    private FuncionarioEntity toEntity(Funcionario f) {
        FuncionarioEntity e = new FuncionarioEntity();
        e.setId(f.getId());
        e.setNome(f.getNome());
        e.setEmail(f.getEmail());
        e.setRole(f.getRole());
        e.setAtivo(f.isAtivo());
        e.setPermissoes(new HashSet<>(f.getPermissoesEspecificas()));
        return e;
    }

    private Funcionario toDomain(FuncionarioEntity e) {
        return new Funcionario(
            e.getId(),
            e.getNome(),
            e.getEmail(),
            e.getRole(),
            e.isAtivo(),
            e.getPermissoes()
        );
    }
}