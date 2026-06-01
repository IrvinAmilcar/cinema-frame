package br.com.cinema.frame.infrastructure.rbac;

import br.com.cinema.frame.domain.backoffice.rbac.Funcionario;
import br.com.cinema.frame.domain.backoffice.rbac.FuncionarioRepository;
import br.com.cinema.frame.domain.backoffice.rbac.RoleFuncionario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.UUID;

@Configuration
public class RbacConfig {

    @Bean
    CommandLineRunner initRbacDatabase(FuncionarioRepository repository) {
        return args -> {
            
            if (repository.listarTodos().isEmpty()) {
                
                Funcionario admin = new Funcionario(
                        UUID.randomUUID(),
                        "Administrador do Sistema",
                        "admin@cinema.com.br",
                        RoleFuncionario.GERENTE, 
                        true,
                        new HashSet<>() 
                );

                repository.salvar(admin);
                System.out.println("Seed executado: Usuário Admin padrão criado com sucesso.");
            }
        };
    }
}