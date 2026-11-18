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

import java.util.Locale;

/**
 * Cria o primeiro ADMIN em um banco vazio fora dos perfis dev e test, para que
 * a API nao nasca sem nenhum usuario capaz de administrar os demais.
 * Sem APP_BOOTSTRAP_ADMIN_EMAIL e APP_BOOTSTRAP_ADMIN_PASSWORD nao faz nada.
 */
@Component
@Profile("!dev & !test")
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap.admin.email:}") String email,
                          @Value("${app.bootstrap.admin.password:}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email == null ? "" : email.trim();
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty() || password.isEmpty()) {
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            log.warn("Admin inicial ignorado: a senha de bootstrap precisa de pelo menos {} caracteres",
                    MIN_PASSWORD_LENGTH);
            return;
        }
        if (userRepository.count() > 0) {
            log.info("Admin inicial ignorado: ja existem usuarios cadastrados");
            return;
        }

        String normalizedEmail = email.toLowerCase(Locale.ROOT);
        userRepository.save(new User("Admin", normalizedEmail, UserRole.ADMIN,
                passwordEncoder.encode(password)));
        log.warn("Admin inicial criado por bootstrap: {}", normalizedEmail);
    }
}
