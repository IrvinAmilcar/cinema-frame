package br.com.cinema.frame.domain.backoffice.classificacao;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;

import java.time.LocalDate;
import java.util.UUID;

public class ClassificacaoDeCompraService {

    private final ClassificacaoService classificacaoService = new ClassificacaoService();
    private final FilmeRepository filmeRepository;

    public ClassificacaoDeCompraService(FilmeRepository filmeRepository) {
        if (filmeRepository == null)
            throw new IllegalArgumentException("FilmeRepository não pode ser nulo");
        this.filmeRepository = filmeRepository;
    }

    public void validarCompra(LocalDate dataNascimento, UUID filmeId) {
        if (dataNascimento == null)
            throw new IllegalArgumentException("Data de nascimento não pode ser nula");

        Filme filme = filmeRepository.buscarPorId(filmeId)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        boolean permitido = classificacaoService.validar(
            dataNascimento,
            filme.getClassificacaoIndicativa()
        );

        if (!permitido)
            throw new IllegalStateException(
                "Cliente não tem idade permitida para assistir este filme. " +
                "Classificação: " + filme.getClassificacaoIndicativa()
            );
    }
}
