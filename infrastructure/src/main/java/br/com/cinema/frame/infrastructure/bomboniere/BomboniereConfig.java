package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.AlertaEstoqueObserver;
import br.com.cinema.frame.domain.backoffice.bomboniere.EstoqueObserver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BomboniereConfig {

    // Registrando nosso Observer como um Bean do Spring
    @Bean
    public EstoqueObserver alertaEstoqueObserver() {
        return new AlertaEstoqueObserver();
    }
}