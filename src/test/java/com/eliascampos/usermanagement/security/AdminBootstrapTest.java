package com.eliascampos.usermanagement.security;

import com.eliascampos.usermanagement.user.User;
import com.eliascampos.usermanagement.user.UserRepository;
import com.eliascampos.usermanagement.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminBootstrapTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final List<User> saved = new ArrayList<>();

    @BeforeEach
    void captureSavedUsers() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            saved.add(invocation.getArgument(0));
            return invocation.getArgument(0);
        });
    }

    @Test
    void shouldCreateAdminWhenDatabaseIsEmpty() {
        when(userRepository.count()).thenReturn(0L);

        new AdminBootstrap(userRepository, passwordEncoder, " Admin@Email.com ", "senha12345").run(null);

        assertEquals(1, saved.size());
        User admin = saved.get(0);
        assertEquals("admin@email.com", admin.getEmail());
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertTrue(admin.isActive());
        assertTrue(passwordEncoder.matches("senha12345", admin.getPasswordHash()));
    }

    @Test
    void shouldNotCreateAdminWhenUsersAlreadyExist() {
        when(userRepository.count()).thenReturn(2L);

        new AdminBootstrap(userRepository, passwordEncoder, "admin@email.com", "senha12345").run(null);

        assertTrue(saved.isEmpty());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldDoNothingWithoutBootstrapCredentials() {
        new AdminBootstrap(userRepository, passwordEncoder, "", "").run(null);

        verify(userRepository, never()).count();
        verify(userRepository, never()).save(any(User.class));
    }
}
