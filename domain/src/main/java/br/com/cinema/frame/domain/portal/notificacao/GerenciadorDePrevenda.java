package br.com.cinema.frame.domain.portal.notificacao;

import br.com.cinema.frame.domain.backoffice.grade.Sessao;
import br.com.cinema.frame.domain.portal.cliente.Cliente;

import java.util.List;

public class GerenciadorDePrevenda {

    private final ServicoDeNotificacao servicoDeNotificacao;

    public GerenciadorDePrevenda(ServicoDeNotificacao servicoDeNotificacao) {
        if (servicoDeNotificacao == null)
            throw new IllegalArgumentException("Serviço de notificação não pode ser nulo");

        this.servicoDeNotificacao = servicoDeNotificacao;
    }

    public void notificarClientesInteressados(Sessao sessao, List<Cliente> clientes) {
        if (sessao == null)
            throw new IllegalArgumentException("Sessão não pode ser nula");
        if (clientes == null)
            throw new IllegalArgumentException("Lista de clientes não pode ser nula");

        clientes.stream()
            .filter(cliente -> clienteTemInteresse(cliente, sessao))
            .forEach(cliente -> {
                NotificacaoDePrevenda notificacao = new NotificacaoDePrevenda(
                    cliente,
                    sessao.getFilme(),
                    sessao
                );
                servicoDeNotificacao.notificar(notificacao);
            });
    }

    private boolean clienteTemInteresse(Cliente cliente, Sessao sessao) {
        return cliente.getFilmesFavoritos().contains(sessao.getFilme());
    }
}
