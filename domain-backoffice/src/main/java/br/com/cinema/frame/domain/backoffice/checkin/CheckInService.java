package br.com.cinema.frame.domain.backoffice.checkin;

import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.rbac.Permissao;
import br.com.cinema.frame.domain.backoffice.rbac.RbacService;

import java.time.LocalDateTime;
import java.util.UUID;

public class CheckInService {

    private static final int MINUTOS_ANTES_PERMITIDOS = 30;

    private final IngressoRepository ingressoRepository;
    private final RegistroDeEntradaRepository registroRepository;
    private final RbacService rbacService;

    public CheckInService(IngressoRepository ingressoRepository,
                          RegistroDeEntradaRepository registroRepository,
                          RbacService rbacService) {
        if (ingressoRepository == null)
            throw new IllegalArgumentException("IngressoRepository não pode ser nulo");
        if (registroRepository == null)
            throw new IllegalArgumentException("RegistroDeEntradaRepository não pode ser nulo");
        if (rbacService == null)
            throw new IllegalArgumentException("RbacService não pode ser nulo");

        this.ingressoRepository = ingressoRepository;
        this.registroRepository = registroRepository;
        this.rbacService = rbacService;
    }

    public RegistroDeEntrada realizar(UUID funcionarioId, UUID ingressoId, UUID sessaoId, LocalDateTime agora) {
        if (funcionarioId == null)
            throw new IllegalArgumentException("ID do funcionário não pode ser nulo");
        if (ingressoId == null)
            throw new IllegalArgumentException("ID do ingresso não pode ser nulo");
        if (sessaoId == null)
            throw new IllegalArgumentException("ID da sessão não pode ser nulo");
        if (agora == null)
            throw new IllegalArgumentException("Horário não pode ser nulo");

        rbacService.verificar(funcionarioId, Permissao.REALIZAR_CHECKIN);

        Ingresso ingresso = ingressoRepository.buscarPorId(ingressoId)
            .orElseThrow(() -> new IllegalArgumentException("QR Code inválido: ingresso não encontrado"));

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

        RegistroDeEntrada registro = new RegistroDeEntrada(ingressoId, sessaoId, agora);
        registroRepository.salvar(registro);

        return registro;
    }
}