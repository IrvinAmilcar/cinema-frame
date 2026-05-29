package br.com.cinema.frame.infrastructure.rbac;

import br.com.cinema.frame.domain.backoffice.rbac.Funcionario;
import br.com.cinema.frame.domain.backoffice.rbac.FuncionarioRepository;
import br.com.cinema.frame.domain.backoffice.rbac.RoleFuncionario;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class FuncionarioRepositoryImpl implements FuncionarioRepository {

    private final Map<UUID, Funcionario> store = new ConcurrentHashMap<>();

    public FuncionarioRepositoryImpl() {
    Funcionario operador = new Funcionario(
        UUID.fromString("987e6543-e21b-12d3-a456-426614174000"), // mesmo ID do frontend
        "Operador Padrão",
        RoleFuncionario.OPERADOR_DE_CAIXA
    );
    store.put(operador.getId(), operador);
}

    @Override
    public void salvar(Funcionario funcionario) {
        store.put(funcionario.getId(), funcionario);
    }

    @Override
    public Optional<Funcionario> buscarPorId(UUID id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Funcionario> listarTodos() {
        return new ArrayList<>(store.values());
    }
}