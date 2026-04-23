package br.com.cinema.frame.domain.backoffice.checkin;

import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class CheckInService {

    private static final int MINUTOS_ANTES_PERMITIDOS = 30;

    private final IngressoRepository ingressoRepository;

    public CheckInService(IngressoRepository ingressoRepository) {
        if (ingressoRepository == null)
            throw new IllegalArgumentException("IngressoRepository não pode ser nulo");
        this.ingressoRepository = ingressoRepository;
    }

    public void realizar(UUID ingressoId, UUID sessaoId, LocalDateTime agora) {
        if (ingressoId == null)
            throw new IllegalArgumentException("ID do ingresso não pode ser nulo");
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");
        if (agora == null)
            throw new IllegalArgumentException("Horário não pode ser nulo");

        Ingresso ingresso = ingressoRepository.buscarPorId(ingressoId)
            .orElseThrow(() -> new IllegalArgumentException("Ingresso não encontrado: " + ingressoId));

        if (!ingresso.getSessao().getId().equals(sessaoId))
            throw new IllegalStateException("Ingresso não pertence a esta sessão");

        if (ingresso.isUtilizado())
            throw new IllegalStateException("Ingresso já utilizado");

        LocalDateTime inicioSessao = ingresso.getSessao().getInicio();
        LocalDateTime aberturaPortas = inicioSessao.minusMinutes(MINUTOS_ANTES_PERMITIDOS);
        LocalDateTime fechamentoPortas = inicioSessao.plusMinutes(MINUTOS_ANTES_PERMITIDOS);

        if (agora.isBefore(aberturaPortas) || agora.isAfter(fechamentoPortas))
            throw new IllegalStateException("Fora do horário permitido de entrada");

        ingresso.marcarComoUtilizado();
        ingressoRepository.salvar(ingresso);
    }
}