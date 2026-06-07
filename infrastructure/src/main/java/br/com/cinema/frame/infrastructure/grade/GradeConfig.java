package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.grade.GradeService;
import br.com.cinema.frame.domain.backoffice.grade.GradeServiceInterface;
import br.com.cinema.frame.domain.backoffice.grade.GradeServiceProxy;
import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GradeConfig {

    @Bean
    public GradeServiceInterface gradeService(GradeDeExibicaoRepository gradeRepository,
                                              FilmeRepository filmeRepository,
                                              SalaRepository salaRepository) {
        GradeService real = new GradeService(gradeRepository, filmeRepository, salaRepository);
        return new GradeServiceProxy(real, gradeRepository, filmeRepository, salaRepository);
    }
}
