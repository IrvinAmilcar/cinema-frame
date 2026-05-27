package br.com.cinema.frame.infrastructure.bomboniere;

import br.com.cinema.frame.domain.backoffice.bomboniere.AlertaEstoqueObserver;
import br.com.cinema.frame.domain.backoffice.bomboniere.BombonieresService;
import br.com.cinema.frame.domain.backoffice.bomboniere.EstoqueObserver;
import br.com.cinema.frame.domain.backoffice.bomboniere.InsumoRepository;
import br.com.cinema.frame.domain.backoffice.bomboniere.MovimentacaoEstoqueRepository;
import br.com.cinema.frame.domain.backoffice.bomboniere.ProdutoDaBombonieresRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BomboniereConfig {

    @Bean
    public EstoqueObserver alertaEstoqueObserver() {
        return new AlertaEstoqueObserver();
    }

    @Bean
    public BombonieresService bombonieresService(
            ProdutoDaBombonieresRepository produtoRepository,
            InsumoRepository insumoRepository,
            MovimentacaoEstoqueRepository movimentacaoRepository,
            EstoqueObserver alertaEstoqueObserver
    ) {
        BombonieresService service = new BombonieresService(
                produtoRepository, insumoRepository, movimentacaoRepository
        );
        service.adicionarObservador(alertaEstoqueObserver);
        return service;
    }
}