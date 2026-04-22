package br.com.cinema.frame.domain.backoffice.classificacao;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ClassificacaoSteps {

    private ClassificacaoIndicativa classificacao;
    private LocalDate dataNascimento;
    private boolean resultado;
    private final ClassificacaoService validacao = new ClassificacaoService();

    @Dado("que o filme possui classificação {string}")
    public void filmeComClassificacao(String classificacaoStr) {
        classificacao = ClassificacaoIndicativa.valueOf(classificacaoStr);
    }

    @Dado("a pessoa nasceu em {string}")
    public void pessoaNasceuEm(String data) {
        dataNascimento = LocalDate.parse(data);
    }

    @Dado("a pessoa nasceu há exatamente {int} anos")
    public void pessoaNasceuHaExatamenteAnos(int anos) {
        dataNascimento = LocalDate.now().minusYears(anos);
    }

    @Quando("o sistema validar a classificação")
    public void sistemaValidarClassificacao() {
        resultado = validacao.validar(dataNascimento, classificacao);
    }

    @Então("o acesso deve ser permitido")
    public void acessoDeveSerPermitido() {
        assertTrue(resultado);
    }

    @Então("o acesso deve ser negado")
    public void acessoDeveSerNegado() {
        assertFalse(resultado);
    }
}
