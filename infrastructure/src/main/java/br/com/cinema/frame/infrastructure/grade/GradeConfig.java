package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.GradeService;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GradeConfig {

    @Bean
    public GradeService gradeService(GradeDeExibicaoRepository gradeRepository,
                                     FilmeRepository filmeRepository,
                                     SalaRepository salaRepository) {
        return new GradeService(gradeRepository, filmeRepository, salaRepository);
    }
}
