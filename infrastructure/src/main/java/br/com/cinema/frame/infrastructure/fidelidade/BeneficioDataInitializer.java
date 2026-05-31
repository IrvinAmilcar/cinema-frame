package br.com.cinema.frame.infrastructure.fidelidade;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import br.com.cinema.frame.domain.portal.fidelidade.Beneficio;
import br.com.cinema.frame.domain.portal.fidelidade.BeneficioRepository;
import br.com.cinema.frame.domain.portal.fidelidade.TipoBeneficio;

/**
 * Inicializa os benefícios padrão do programa de fidelidade.
 * Roda uma vez na inicialização — só insere se o banco estiver vazio.
 */
@Component
public class BeneficioDataInitializer implements ApplicationRunner {

    private final BeneficioRepository beneficioRepository;

    public BeneficioDataInitializer(BeneficioRepository beneficioRepository) {
        this.beneficioRepository = beneficioRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Beneficio> existentes = beneficioRepository.listarTodos();
        if (!existentes.isEmpty()) return;

        List<Beneficio> beneficios = List.of(
            new Beneficio(
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "Combo 2 Pipocas + 1 Refrigerante",
                TipoBeneficio.COMBO_PIPOCA_REFRIGERANTE,
                1500,
                true,
                Set.of(),
                Set.of()
            ),
            new Beneficio(
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "1 Ingresso Grátis",
                TipoBeneficio.INGRESSO_GRATIS,
                3000,
                false,
                Set.of(TipoBeneficio.DOIS_INGRESSOS_GRATIS),
                Set.of()
            ),
            new Beneficio(
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "2 Ingressos Grátis",
                TipoBeneficio.DOIS_INGRESSOS_GRATIS,
                5000,
                false,
                Set.of(TipoBeneficio.INGRESSO_GRATIS),
                Set.of()
            ),
            new Beneficio(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "Combo Bala Fine + Pipoca",
                TipoBeneficio.COMBO_BALA_PIPOCA,
                800,
                true,
                Set.of(),
                Set.of()
            )
        );

        beneficios.forEach(beneficioRepository::salvar);
    }
}
