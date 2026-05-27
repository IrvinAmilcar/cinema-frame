package br.com.cinema.frame.pedido;

import br.com.cinema.frame.domain.portal.promocao.Cupom;
import br.com.cinema.frame.domain.portal.promocao.CupomRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cupom")
public class CupomController {

    private final CupomRepository cupomRepository;

    public CupomController(CupomRepository cupomRepository) {
        this.cupomRepository = cupomRepository;
    }

    @GetMapping
    public List<CupomResponse> listar() {
        return cupomRepository.listarTodos().stream()
                .map(CupomResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CupomResponse cadastrar(@RequestBody CupomRequest req) {
        Cupom cupom = new Cupom(req.codigo(), req.tipo(), req.cumulativo(), req.validade());
        cupomRepository.salvar(cupom);
        return CupomResponse.from(cupom);
    }
}
