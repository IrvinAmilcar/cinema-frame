package br.com.cinema.frame.infrastructure.cliente;

import br.com.cinema.frame.domain.portal.cliente.Cliente;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class ClienteJpa {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String senha;

    protected ClienteJpa() {}

    public static ClienteJpa fromDomain(Cliente c, String senha) {
        ClienteJpa e = new ClienteJpa();
        e.id = c.getId().getValor();
        e.nome = c.getNome();
        e.email = c.getEmail();
        e.dataNascimento = c.getDataNascimento();
        e.senha = senha;
        return e;
    }

    public Cliente toDomain() {
        return Cliente.reconstituir(id, nome, email, dataNascimento);
    }

    public UUID getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public String getSenha() { return senha; }
}
