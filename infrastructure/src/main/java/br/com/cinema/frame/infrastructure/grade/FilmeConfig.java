package br.com.cinema.frame.infrastructure.grade;

import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.FilmeService;
import br.com.cinema.frame.domain.backoffice.grade.FilmeServiceInterface;
import br.com.cinema.frame.domain.backoffice.grade.FilmeServiceProxy;
import br.com.cinema.frame.domain.backoffice.grade.SessaoRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilmeConfig {

    @Bean
    public FilmeServiceInterface filmeService(FilmeRepository filmeRepository, SessaoRepository sessaoRepository) {
        return new FilmeServiceProxy(new FilmeService(filmeRepository), sessaoRepository);
    }
}
