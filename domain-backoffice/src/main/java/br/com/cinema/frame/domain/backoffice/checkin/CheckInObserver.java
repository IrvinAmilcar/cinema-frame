package br.com.cinema.frame.domain.backoffice.checkin;

public interface CheckInObserver {
    void onCheckInAprovado(RegistroDeEntrada registro);
}