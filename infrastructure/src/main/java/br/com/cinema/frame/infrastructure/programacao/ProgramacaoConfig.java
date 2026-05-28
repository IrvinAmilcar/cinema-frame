package br.com.cinema.frame.infrastructure.programacao;

import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.SessaoRepository;
import br.com.cinema.frame.domain.portal.notificacao.FilmeFavoritadoRepository;
import br.com.cinema.frame.domain.portal.notificacao.NotificacaoService;
import br.com.cinema.frame.domain.portal.programacao.ProgramacaoComRecomendacaoDecorator;
import br.com.cinema.frame.domain.portal.programacao.ProgramacaoService;
import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeComprasRepository;
import br.com.cinema.frame.domain.portal.recomendacao.FilmeSugeridoRepository;
import br.com.cinema.frame.domain.portal.recomendacao.MotorDeRecomendacao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProgramacaoConfig {

    @Bean
    MotorDeRecomendacao motorDeRecomendacao(HistoricoDeComprasRepository historicoRepository,
                                            FilmeSugeridoRepository filmeSugeridoRepository,
                                            FilmeFavoritadoRepository filmeFavoritadoRepository) {
        return new MotorDeRecomendacao(historicoRepository, filmeSugeridoRepository, filmeFavoritadoRepository);
    }

    @Bean
    ProgramacaoComRecomendacaoDecorator programacaoDecorator(ProgramacaoService programacaoService,
                                                              HistoricoDeComprasRepository historicoRepository,
                                                              FilmeFavoritadoRepository favoritoRepository) {
        return new ProgramacaoComRecomendacaoDecorator(programacaoService, historicoRepository, favoritoRepository);
    }

    @Bean
    NotificacaoService notificacaoService(FilmeFavoritadoRepository filmeFavoritadoRepository,
                                          FilmeRepository filmeRepository,
                                          SessaoRepository sessaoRepository) {
        return new NotificacaoService(filmeFavoritadoRepository, filmeRepository, sessaoRepository);
    }
}
