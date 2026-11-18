package com.eliascampos.usermanagement.security;

import com.eliascampos.usermanagement.user.User;
import com.eliascampos.usermanagement.user.UserRepository;
import com.eliascampos.usermanagement.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Cria um admin de desenvolvimento quando o banco esta vazio, para que o login
 * funcione logo apos subir a aplicacao. Fica fora dos perfis prod e test.
 */
@Component
@Profile("dev")
public class DevAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevAdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevAdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.save(new User("Admin Dev", "admin@email.com", UserRole.ADMIN,
                passwordEncoder.encode("admin12345")));
        log.warn("Admin de desenvolvimento criado: admin@email.com / admin12345");
    }
}
