package br.com.cinema.frame.infrastructure.pedido;

import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.backoffice.classificacao.ClassificacaoDeCompraService;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.backoffice.grade.GradeDeExibicaoRepository;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.portal.pedido.PedidoRepository;
import br.com.cinema.frame.domain.portal.pedido.PedidoService;
import br.com.cinema.frame.domain.portal.programacao.ProgramacaoService;
import br.com.cinema.frame.domain.portal.recomendacao.HistoricoDeComprasRepository;
import br.com.cinema.frame.domain.portal.reserva.ReservaRepository;
import br.com.cinema.frame.domain.portal.reserva.ReservaService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PedidoConfig {

    @Bean
    ProgramacaoService programacaoService(GradeDeExibicaoRepository gradeRepository,
                                           IngressoRepository ingressoRepository) {
        return new ProgramacaoService(gradeRepository, ingressoRepository);
    }

    @Bean
    ReservaService reservaService(ReservaRepository reservaRepository,
                                   GradeDeExibicaoRepository gradeRepository) {
        return new ReservaService(reservaRepository, gradeRepository);
    }

    @Bean
    ClassificacaoDeCompraService classificacaoDeCompraService(FilmeRepository filmeRepository) {
        return new ClassificacaoDeCompraService(filmeRepository);
    }

    @Bean
    PedidoService pedidoService(PedidoRepository pedidoRepository,
                                GradeDeExibicaoRepository gradeRepository,
                                BombonieresService bombonieresService,
                                ReservaService reservaService,
                                IngressoRepository ingressoRepository,
                                HistoricoDeComprasRepository historicoRepository,
                                ClassificacaoDeCompraService classificacaoDeCompraService) {
        return new PedidoService(pedidoRepository, gradeRepository, bombonieresService,
                ingressoRepository, null, historicoRepository, classificacaoDeCompraService, reservaService);
    }
}
