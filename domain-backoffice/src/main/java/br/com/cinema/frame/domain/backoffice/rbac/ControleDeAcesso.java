package br.com.cinema.frame.domain.backoffice.rbac;
 
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
 
public class ControleDeAcesso {
 
    private static final Map<RoleFuncionario, Set<Permissao>> PERMISSOES_POR_ROLE =
            new EnumMap<>(RoleFuncionario.class);
 
    static {
        PERMISSOES_POR_ROLE.put(
            RoleFuncionario.OPERADOR_DE_CAIXA,
            EnumSet.of(Permissao.VENDER_INGRESSO, Permissao.REALIZAR_CHECKIN)
        );
        PERMISSOES_POR_ROLE.put(
            RoleFuncionario.GERENTE,
            EnumSet.of(
                Permissao.VENDER_INGRESSO,
                Permissao.ESTORNAR_VENDA,
                Permissao.ALTERAR_PRECO,
                Permissao.REALIZAR_CHECKIN
            )
        );
    }
 
    public void verificar(Funcionario funcionario, Permissao permissao) {
        if (funcionario == null)
            throw new IllegalArgumentException("Funcionário não pode ser nulo");
        if (permissao == null)
            throw new IllegalArgumentException("Permissão não pode ser nula");
 
        Set<Permissao> permissoes = PERMISSOES_POR_ROLE.get(funcionario.getRole());
 
        if (permissoes == null || !permissoes.contains(permissao))
            throw new IllegalStateException(
                "Acesso negado: " + funcionario.getRole() + " não possui permissão para " + permissao
            );
    }
 
    public boolean temPermissao(Funcionario funcionario, Permissao permissao) {
        if (funcionario == null || permissao == null)
            return false;
 
        Set<Permissao> permissoes = PERMISSOES_POR_ROLE.get(funcionario.getRole());
        return permissoes != null && permissoes.contains(permissao);
    }
}