package br.com.cinema.frame.domain.backoffice.bomboniere;

public class AlertaEstoqueObserver implements EstoqueObserver {
    
    @Override
    public void notificarEstoqueCritico(Insumo insumo) {
        // Aqui pode expandir no futuro para enviar um email, salvar no banco de dados, ou disparar um evento spring
        System.out.println("ALERTA: O insumo '" + insumo.getNome() + 
                           "' atingiu o nível crítico! Estoque atual: " + 
                           insumo.getQuantidadeEmEstoque());
    }
}