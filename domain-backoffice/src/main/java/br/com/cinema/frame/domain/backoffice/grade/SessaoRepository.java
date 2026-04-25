package br.com.cinema.frame.domain.backoffice.grade;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessaoRepository {

    void salvar(Sessao sessao);
    Optional<Sessao> buscarPorId(UUID id);
    List<Sessao> buscarPorFilme(UUID filmeId);
    List<Sessao> listarTodas();
    void remover(UUID id);
}