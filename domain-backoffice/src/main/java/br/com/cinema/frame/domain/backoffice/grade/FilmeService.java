package br.com.cinema.frame.domain.backoffice.grade;


import java.util.List;
import java.util.UUID;

public class FilmeService {

    private final FilmeRepository filmeRepository;

    public FilmeService(FilmeRepository filmeRepository) {
        if (filmeRepository == null)
            throw new IllegalArgumentException("FilmeRepository não pode ser nulo");
        this.filmeRepository = filmeRepository;
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

    public void remover(UUID id) {
        filmeRepository.buscarPorId(id)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + id));
        filmeRepository.remover(id);
    }
}