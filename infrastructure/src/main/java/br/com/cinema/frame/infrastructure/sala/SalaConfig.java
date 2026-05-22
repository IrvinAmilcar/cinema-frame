package br.com.cinema.frame.infrastructure.sala;

import br.com.cinema.frame.domain.backoffice.sala.SalaRepository;
import br.com.cinema.frame.domain.backoffice.sala.SalaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SalaConfig {

    @Bean
    public SalaService salaService(SalaRepository salaRepository) {
        return new SalaService(salaRepository);
    }
}
