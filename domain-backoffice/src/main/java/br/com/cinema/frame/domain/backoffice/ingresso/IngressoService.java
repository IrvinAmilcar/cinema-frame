package br.com.cinema.frame.domain.backoffice.ingresso;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

import java.util.List;
import java.util.UUID;

public class IngressoService {

    private final IngressoRepository ingressoRepository;

    public IngressoService(IngressoRepository ingressoRepository) {
        if (ingressoRepository == null)
            throw new IllegalArgumentException("IngressoRepository não pode ser nulo");
        this.ingressoRepository = ingressoRepository;
    }

    public Ingresso emitir(Sessao sessao, TipoIngresso tipo) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        if (tipo == null)
            throw new IllegalArgumentException("Tipo do ingresso não pode ser nulo");

        Ingresso ingresso = new Ingresso(sessao, tipo);
        ingressoRepository.salvar(ingresso);
        return ingresso;
    }

    public Ingresso buscarPorId(UUID id) {
        return ingressoRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado: " + id));
    }

    public List<Ingresso> buscarPorSessao(Sessao sessao) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        return ingressoRepository.buscarPorSessao(sessao);
    }
}