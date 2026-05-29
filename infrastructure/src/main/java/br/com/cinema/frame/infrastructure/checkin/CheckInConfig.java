package br.com.cinema.frame.infrastructure.checkin;

import br.com.cinema.frame.domain.backoffice.checkin.CheckInObserver;
import br.com.cinema.frame.domain.backoffice.checkin.CheckInService;
import br.com.cinema.frame.domain.backoffice.checkin.LotaCargaObserver;
import br.com.cinema.frame.domain.backoffice.checkin.RegistroDeEntradaRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.rbac.FuncionarioRepository;
import br.com.cinema.frame.domain.backoffice.rbac.RbacService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CheckInConfig {

    @Bean
    public CheckInObserver lotaCargaObserver() {
        return new LotaCargaObserver();
    }

    @Bean
    public RbacService rbacService(FuncionarioRepository funcionarioRepository) {
        return new RbacService(funcionarioRepository);
    }

    @Bean
    public CheckInService checkInService(
            IngressoRepository ingressoRepository,
            RegistroDeEntradaRepository registroRepository,
            RbacService rbacService,
            List<CheckInObserver> observers) {
        return new CheckInService(ingressoRepository, registroRepository, rbacService, observers);
    }
}