package br.com.cinema.frame.application.programacao;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.portal.programacao.ProgramacaoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ListarFilmesUseCase {

    private final ProgramacaoService programacaoService;

    public ListarFilmesUseCase(ProgramacaoService programacaoService) {
        this.programacaoService = programacaoService;
    }

    public List<Sessao> executar(LocalDate data) {
        if (data.equals(LocalDate.now())) {
            return programacaoService.listarSessoesDisponiveis(LocalDateTime.now());
        }
        return programacaoService.listarSessoesPorData(data);
    }
}
