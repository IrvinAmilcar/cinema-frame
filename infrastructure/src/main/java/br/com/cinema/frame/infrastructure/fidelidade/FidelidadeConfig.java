package br.com.cinema.frame.infrastructure.fidelidade;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.portal.cliente.ClienteRepository;
import br.com.cinema.frame.domain.portal.fidelidade.BeneficioRepository;
import br.com.cinema.frame.domain.portal.fidelidade.FidelidadeRepository;
import br.com.cinema.frame.domain.portal.fidelidade.FidelidadeService;
import br.com.cinema.frame.domain.portal.fidelidade.RegistroResgateRepository;

@Configuration
public class FidelidadeConfig {

    @Bean
    public FidelidadeService fidelidadeService(
            FidelidadeRepository fidelidadeRepository,
            BeneficioRepository beneficioRepository,
            ClienteRepository clienteRepository,
            RegistroResgateRepository resgateRepository,
            BombonieresService bombonieresService) {
        return new FidelidadeService(fidelidadeRepository, beneficioRepository,
                clienteRepository, resgateRepository, bombonieresService);
    }
}