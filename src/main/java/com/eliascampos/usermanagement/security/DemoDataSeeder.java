package com.eliascampos.usermanagement.security;

import com.eliascampos.usermanagement.user.User;
import com.eliascampos.usermanagement.user.UserRepository;
import com.eliascampos.usermanagement.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

/**
 * Popula a demo publica com usuarios de exemplo quando o banco esta vazio.
 * Todos usam a mesma senha de demo; como a demo e somente leitura, isso e proposital
 * e documentado: o visitante pode logar, mas nao altera nada.
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DemoDataSeeder(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.demo.admin.email}") String adminEmail,
                          @Value("${app.demo.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase(Locale.ROOT);
        this.adminPassword = adminPassword == null ? "" : adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            log.info("Dados de demonstracao ignorados: o banco ja tem usuarios");
            return;
        }

        String passwordHash = passwordEncoder.encode(adminPassword);
        List<User> users = List.of(
                new User("Demo Admin", adminEmail, UserRole.ADMIN, passwordHash),
                new User("Ana Beatriz Lima", "ana.lima@exemplo.com", UserRole.USER, passwordHash),
                new User("Bruno Carvalho", "bruno.carvalho@exemplo.com", UserRole.USER, passwordHash),
                new User("Camila Nogueira", "camila.nogueira@exemplo.com", UserRole.USER, passwordHash),
                new User("Diego Fontes", "diego.fontes@exemplo.com", UserRole.ADMIN, passwordHash),
                new User("Elisa Martins", "elisa.martins@exemplo.com", UserRole.USER, passwordHash),
                new User("Felipe Andrade", "felipe.andrade@exemplo.com", UserRole.USER, passwordHash)
        );

        // Um usuario inativo deixa o filtro ?active=false com resultado visivel.
        User inactive = users.get(users.size() - 1);
        inactive.changeStatus(false);

        userRepository.saveAll(users);
        log.warn("Dados de demonstracao criados: {} usuarios. Login: {}", users.size(), adminEmail);
    }
}
