package br.com.cinema.frame.domain.backoffice.rbac;
 
import java.util.UUID;
 
public class Funcionario {
 
    private final UUID id;
    private final String nome;
    private final RoleFuncionario role;
 
    public Funcionario(String nome, RoleFuncionario role) {
        if (nome == null || nome.isBlank())
            throw new IllegalArgumentException("Nome do funcionário não pode ser vazio");
        if (role == null)
            throw new IllegalArgumentException("Role do funcionário não pode ser nula");
 
        this.id = UUID.randomUUID();
        this.nome = nome;
        this.role = role;
    }
 
    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public RoleFuncionario getRole() { return role; }
}
