package br.com.cinema.frame.domain.backoffice.rbac;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RbacSteps {

    private FuncionarioRepository funcionarioRepository = mock(FuncionarioRepository.class);
    private RbacService rbacService = new RbacService(funcionarioRepository);

    private Funcionario funcionario;
    private Exception excecaoCapturada;
    private boolean resultadoConsulta;

    @Dado("que existe um funcionário cadastrado {string} com o role {string}")
    public void existeFuncionarioCadastradoComRole(String nome, String role) {
        funcionario = new Funcionario(nome, RoleFuncionario.valueOf(role));
        when(funcionarioRepository.buscarPorId(funcionario.getId()))
            .thenReturn(Optional.of(funcionario));
    }

    @Quando("o sistema verificar a permissão {string} para o funcionário cadastrado")
    public void sistemaVerificarPermissaoParaFuncionarioCadastrado(String permissao) {
        rbacService.verificar(funcionario.getId(), Permissao.valueOf(permissao));
    }

    @Quando("o sistema tentar verificar a permissão {string} para o funcionário cadastrado")
    public void sistemaTentarVerificarPermissaoParaFuncionarioCadastrado(String permissao) {
        try {
            rbacService.verificar(funcionario.getId(), Permissao.valueOf(permissao));
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }

    @Quando("o sistema consultar se o funcionário cadastrado possui a permissão {string}")
    public void sistemaConsultarPermissaoFuncionarioCadastrado(String permissao) {
        resultadoConsulta = rbacService.temPermissao(funcionario.getId(), Permissao.valueOf(permissao));
    }

    @Então("o acesso deve ser permitido")
    public void acessoDeveSerPermitido() {
        assertNull(excecaoCapturada);
    }

    @Então("o sistema deve rejeitar informando acesso negado")
    public void rejeitarAcessoNegado() {
        assertNotNull(excecaoCapturada);
        assertInstanceOf(IllegalStateException.class, excecaoCapturada);
        assertTrue(excecaoCapturada.getMessage().contains("Acesso negado"));
    }

    @Então("o resultado da consulta deve ser falso")
    public void resultadoDeveSerFalso() {
        assertFalse(resultadoConsulta);
    }

    @Então("o resultado da consulta deve ser verdadeiro")
    public void resultadoDeveSerVerdadeiro() {
        assertTrue(resultadoConsulta);
    }
}