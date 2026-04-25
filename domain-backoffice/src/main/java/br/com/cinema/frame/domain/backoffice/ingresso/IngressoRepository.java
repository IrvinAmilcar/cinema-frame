package br.com.cinema.frame.domain.backoffice.ingresso;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;

public interface IngressoRepository {

    void salvar(Ingresso ingresso);
    Optional<Ingresso> buscarPorId(UUID id);
    List<Ingresso> buscarPorSessao(Sessao sessao);
    void remover(UUID id);
}