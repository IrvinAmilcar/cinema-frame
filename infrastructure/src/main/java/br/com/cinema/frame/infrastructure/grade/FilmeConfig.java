package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.FilmeService;
import br.com.cinema.frame.domain.backoffice.grade.SessaoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilmeConfig {

    @Bean
    public FilmeService filmeService(FilmeRepository filmeRepository, SessaoRepository sessaoRepository) {
        return new FilmeService(filmeRepository, sessaoRepository);
    }
}
