package br.com.cinema.frame.domain.backoffice.classificacao;

import br.com.cinema.frame.domain.backoffice.grade.Filme;
import br.com.cinema.frame.domain.backoffice.grade.FilmeRepository;
import br.com.cinema.frame.domain.portal.cliente.Cliente;
import br.com.cinema.frame.domain.portal.cliente.ClienteRepository;

import java.util.UUID;

public class ClassificacaoDeCompraService {

    private final ClassificacaoService classificacaoService = new ClassificacaoService();
    private final ClienteRepository clienteRepository;
    private final FilmeRepository filmeRepository;

    public ClassificacaoDeCompraService(ClienteRepository clienteRepository, FilmeRepository filmeRepository) {
        if (clienteRepository == null)
            throw new IllegalArgumentException("ClienteRepository não pode ser nulo");

        if (filmeRepository == null)
            throw new IllegalArgumentException("FilmeRepository não pode ser nulo");

        this.clienteRepository = clienteRepository;
        this.filmeRepository = filmeRepository;
    }

    public void validarCompra(UUID clienteId, UUID filmeId) {
        Cliente cliente = clienteRepository.buscarPorId(clienteId)
            .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + clienteId));

        Filme filme = filmeRepository.buscarPorId(filmeId)
            .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado: " + filmeId));

        boolean permitido = classificacaoService.validar(
            cliente.getDataNascimento(),
            filme.getClassificacaoIndicativa()
        );

        if (!permitido)
            throw new IllegalStateException(
                "Cliente " + cliente.getNome() +
                " não tem idade permitida para assistir este filme. " +
                "Classificação: " + filme.getClassificacaoIndicativa()
            );
    }
}