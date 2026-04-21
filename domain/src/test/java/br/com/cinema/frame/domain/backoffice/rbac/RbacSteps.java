package br.com.cinema.frame.domain.backoffice.rbac;
 
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
 
public class RbacSteps {
 
    private Funcionario funcionario;
    private Exception excecaoCapturada;
    private boolean resultadoConsulta;
    private final ControleDeAcesso controle = new ControleDeAcesso();
 
    @Dado("que existe um funcionário {string} com o role {string}")
    public void existeFuncionarioComRole(String nome, String role) {
        funcionario = new Funcionario(nome, RoleFuncionario.valueOf(role));
    }
 
    @Quando("o sistema verificar a permissão {string}")
    public void sistemaVerificarPermissao(String permissao) {
        controle.verificar(funcionario, Permissao.valueOf(permissao));
    }
 
    @Quando("o sistema tentar verificar a permissão {string}")
    public void sistemaTentarVerificarPermissao(String permissao) {
        try {
            controle.verificar(funcionario, Permissao.valueOf(permissao));
        } catch (Exception e) {
            excecaoCapturada = e;
        }
    }
 
    @Quando("o sistema consultar se possui a permissão {string}")
    public void sistemaConsultarPermissao(String permissao) {
        resultadoConsulta = controle.temPermissao(funcionario, Permissao.valueOf(permissao));
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