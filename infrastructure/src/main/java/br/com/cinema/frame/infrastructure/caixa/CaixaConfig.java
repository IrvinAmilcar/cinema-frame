package br.com.cinema.frame.infrastructure.caixa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.cinema.frame.domain.backoffice.caixa.CaixaService;
import br.com.cinema.frame.domain.backoffice.caixa.FechamentoCaixaRepository;

@Configuration
public class CaixaConfig {

    @Bean
    public CaixaService caixaService(FechamentoCaixaRepository fechamentoCaixaRepository) {
        return new CaixaService(fechamentoCaixaRepository);
    }
}
