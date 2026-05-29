package br.com.cinema.frame.domain.backoffice.checkin;

import br.com.cinema.frame.domain.backoffice.ingresso.Ingresso;
import br.com.cinema.frame.domain.backoffice.ingresso.IngressoRepository;
import br.com.cinema.frame.domain.backoffice.rbac.Permissao;
import br.com.cinema.frame.domain.backoffice.rbac.RbacService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CheckInService {  // ← estava faltando isso

    private final IngressoRepository ingressoRepository;
    private final RegistroDeEntradaRepository registroRepository;
    private final RbacService rbacService;
    private final List<CheckInObserver> observers;

    public CheckInService(IngressoRepository ingressoRepository,
                          RegistroDeEntradaRepository registroRepository,
                          RbacService rbacService,
                          List<CheckInObserver> observers) {
        if (ingressoRepository == null)
            throw new IllegalArgumentException("IngressoRepository não pode ser nulo");
        if (registroRepository == null)
            throw new IllegalArgumentException("RegistroDeEntradaRepository não pode ser nulo");
        if (rbacService == null)
            throw new IllegalArgumentException("RbacService não pode ser nulo");

        this.ingressoRepository = ingressoRepository;
        this.registroRepository = registroRepository;
        this.rbacService = rbacService;
        this.observers = observers != null ? observers : new ArrayList<>();
    }

    public RegistroDeEntrada realizar(UUID funcionarioId, UUID ingressoId, UUID sessaoId, LocalDateTime agora) {
        rbacService.verificar(funcionarioId, Permissao.REALIZAR_CHECKIN); // ← estava faltando isso

        var ingressoOpt = ingressoRepository.buscarPorId(ingressoId);

        if (ingressoOpt.isEmpty()) {
            throw new IllegalArgumentException("QR Code inválido");
        }

        Ingresso ingresso = ingressoOpt.get();

        if (!ingresso.getSessao().getId().equals(sessaoId)) {
            throw new IllegalStateException("Ingresso não pertence a esta sessão");
        }

        if (ingresso.isUtilizado()) {
            throw new IllegalStateException("Tentativa de reutilização: Ingresso já utilizado");
        }

        LocalDate dataHoje = agora.toLocalDate();
        LocalDateTime fimSessao = dataHoje.atTime(ingresso.getSessao().getFim());

        
LocalDateTime inicioSessao = dataHoje.atTime(ingresso.getSessao().getInicio());


// janela permitida: até 30 minutos antes do início até o fim da sessão
LocalDateTime aberturaPortas = inicioSessao.minusMinutes(30);

if (agora.isBefore(aberturaPortas) || agora.isAfter(fimSessao)) {
    throw new IllegalStateException("Check-in fora do horário permitido de entrada");
}

        ingresso.marcarComoUtilizado();
        ingressoRepository.salvar(ingresso);

        RegistroDeEntrada sucesso = new RegistroDeEntrada(ingressoId, sessaoId, agora, true, null);
        registroRepository.salvar(sucesso);

        for (CheckInObserver obs : observers) {
            obs.onCheckInAprovado(sucesso);
        }

        return sucesso;
    }

    private RegistroDeEntrada registrarFalha(UUID ingressoId, UUID sessaoId, LocalDateTime agora, String motivo) {
        RegistroDeEntrada falha = new RegistroDeEntrada(ingressoId, sessaoId, agora, false, motivo);
        registroRepository.salvar(falha);
        return falha;
    }
}