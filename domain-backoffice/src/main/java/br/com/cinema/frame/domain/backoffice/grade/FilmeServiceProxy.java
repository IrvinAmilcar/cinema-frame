package br.com.cinema.frame.domain.backoffice.grade;

import br.com.cinema.frame.domain.shared.classificacao.ClassificacaoIndicativa;
import br.com.cinema.frame.domain.shared.filme.GeneroFilme;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Proxy (F7) — Protection Proxy para FilmeServiceInterface.
 *
 * Controla o acesso à operação remover(): intercepta a chamada e verifica se
 * o filme possui sessões futuras cadastradas antes de delegar ao serviço real.
 * Todas as outras operações são delegadas diretamente sem interceptação.
 */
public class FilmeServiceProxy implements FilmeServiceInterface {

    private final FilmeServiceInterface real;
    private final SessaoRepository sessaoRepository;

    public FilmeServiceProxy(FilmeServiceInterface real, SessaoRepository sessaoRepository) {
        if (real == null) throw new IllegalArgumentException("FilmeServiceInterface não pode ser nulo");
        if (sessaoRepository == null) throw new IllegalArgumentException("SessaoRepository não pode ser nulo");
        this.real = real;
        this.sessaoRepository = sessaoRepository;
    }

    @Override
    public void remover(UUID id) {
        boolean temSessoesFuturas = !sessaoRepository.buscarPorFilme(id).isEmpty();
        if (temSessoesFuturas)
            throw new IllegalStateException("Filme possui sessões futuras cadastradas e não pode ser removido");
        real.remover(id);
    }

    @Override public void cadastrar(Filme filme)                        { real.cadastrar(filme); }
    @Override public Filme buscarPorId(UUID id)                        { return real.buscarPorId(id); }
    @Override public List<Filme> listarTodos()                         { return real.listarTodos(); }
    @Override public void desativar(UUID id)                           { real.desativar(id); }
    @Override public void ativar(UUID id)                              { real.ativar(id); }

    @Override
    public Filme atualizar(UUID id, String novoTitulo, Duration novaDuracao,
                           ClassificacaoIndicativa novaClassificacao, GeneroFilme novoGenero,
                           String novoTrailerURL, String sinopse, Double nota) {
        return real.atualizar(id, novoTitulo, novaDuracao, novaClassificacao,
                novoGenero, novoTrailerURL, sinopse, nota);
    }
}
