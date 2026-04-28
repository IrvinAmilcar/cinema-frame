package br.com.cinema.frame.domain.backoffice.grade;


import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FilmeService {

    private final FilmeRepository filmeRepository;
    private final SessaoRepository sessaoRepository;

    public FilmeService(FilmeRepository filmeRepository, SessaoRepository sessaoRepository) {
        if (filmeRepository == null)
            throw new IllegalArgumentException("FilmeRepository não pode ser nulo");
        if (sessaoRepository == null)
            throw new IllegalArgumentException("SessaoRepository não pode ser nulo");
        this.filmeRepository = filmeRepository;
        this.sessaoRepository = sessaoRepository;
    }

    public void cadastrar(Filme filme) {
        if (filme == null)
            throw new IllegalArgumentException("Filme não pode ser nulo");
        filmeRepository.salvar(filme);
    }

    public Filme buscarPorId(UUID id) {
        return filmeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + id));
    }

    public List<Filme> listarTodos() {
        return filmeRepository.listarTodos();
    }

    public Filme atualizar(UUID id, String novoTitulo, Duration novaDuracao,
                            ClassificacaoIndicativa novaClassificacao, GeneroFilme novoGenero, String novoTrailerURL) {
        Filme filme = filmeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + id));
        filme.atualizar(novoTitulo, novaDuracao, novaClassificacao, novoGenero, novoTrailerURL);
        filmeRepository.salvar(filme);
        return filme;
    }

    public void desativar(UUID id) {
        Filme filme = filmeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + id));
        filme.desativar();
        filmeRepository.salvar(filme);
    }

    public void ativar(UUID id) {
        Filme filme = filmeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + id));
        filme.ativar();
        filmeRepository.salvar(filme);
    }

    public void remover(UUID id) {
        filmeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + id));

        LocalDateTime agora = LocalDateTime.now();
        boolean temSessoesFuturas = sessaoRepository.buscarPorFilme(id)
            .stream()
            .anyMatch(s -> s.getInicio().isAfter(agora));

        if (temSessoesFuturas)
            throw new IllegalStateException("Filme possui sessões futuras cadastradas e não pode ser removido");

        filmeRepository.remover(id);
    }
}
