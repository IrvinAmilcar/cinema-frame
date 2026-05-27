package br.com.cinema.frame.programacao;

import br.com.cinema.frame.domain.portal.programacao.ProgramacaoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/programacao")
public class ProgramacaoController {

    private final ProgramacaoService programacaoService;

    public ProgramacaoController(ProgramacaoService programacaoService) {
        this.programacaoService = programacaoService;
    }

    @GetMapping("/sessoes")
    public List<SessaoDisponivelResponse> listarSessoes() {
        return programacaoService.listarSessoesDisponiveis(LocalDateTime.now())
                .stream()
                .map(SessaoDisponivelResponse::from)
                .toList();
    }
}
