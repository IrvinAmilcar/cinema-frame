package br.com.cinema.frame.domain.backoffice.checkin;

public class LotaCargaObserver implements CheckInObserver {
    
    @Override
    public void onCheckInAprovado(RegistroDeEntrada registro) {
        // Apenas Java puro aqui, sem dependências externas!
        System.out.println("[OBSERVER] Lotação atualizada! Ingresso " + 
                           registro.getIngressoId() + " validado para a sessão " + 
                           registro.getSessaoId());
    }
}