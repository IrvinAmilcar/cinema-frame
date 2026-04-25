package br.com.cinema.frame.domain.backoffice.rbac;

import java.util.UUID;

public class RbacService {

    private final FuncionarioRepository funcionarioRepository;
    private final ControleDeAcesso controleDeAcesso;

    public RbacService(FuncionarioRepository funcionarioRepository) {
        if (funcionarioRepository == null)
            throw new IllegalArgumentException("FuncionarioRepository não pode ser nulo");
        this.funcionarioRepository = funcionarioRepository;
        this.controleDeAcesso = new ControleDeAcesso();
    }

    public void verificar(UUID funcionarioId, Permissao permissao) {
        Funcionario funcionario = funcionarioRepository.buscarPorId(funcionarioId)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado: " + funcionarioId));
        controleDeAcesso.verificar(funcionario, permissao);
    }

    public boolean temPermissao(UUID funcionarioId, Permissao permissao) {
        Funcionario funcionario = funcionarioRepository.buscarPorId(funcionarioId)
            .orElseThrow(() -> new IllegalArgumentException("Funcionário não encontrado: " + funcionarioId));
        return controleDeAcesso.temPermissao(funcionario, permissao);
    }
}