package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface FilmeServiceInterface {
    void cadastrar(Filme filme);
    Filme buscarPorId(UUID id);
    List<Filme> listarTodos();
    Filme atualizar(UUID id, String novoTitulo, Duration novaDuracao,
                    ClassificacaoIndicativa novaClassificacao, GeneroFilme novoGenero,
                    String novoTrailerURL, String sinopse, Double nota);
    void desativar(UUID id);
    void ativar(UUID id);
    void remover(UUID id);
}
