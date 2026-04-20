package br.com.cinema.frame.domain.backoffice.classificacao;

import java.time.LocalDate;
import java.time.Period;

public class ValidacaoClassificacao {

    public boolean validar(LocalDate dataNascimento, ClassificacaoIndicativa classificacao) {
        if (dataNascimento == null)
            throw new IllegalArgumentException("Data de nascimento não pode ser nula");

        if (classificacao == null)
            throw new IllegalArgumentException("Classificação indicativa não pode ser nula");
        
        if (dataNascimento.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro");

        if (classificacao.isLivre())
            return true;

        int idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        return idade >= classificacao.getIdadeMinima();
    }
}
