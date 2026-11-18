package com.eliascampos.usermanagement.auth;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Janela fixa de tentativas de login por chave (e-mail + IP), em memoria.
 * Por instancia: com mais de uma replica cada uma conta separadamente.
 */
@Component
public class LoginRateLimiter {

    static final int MAX_ATTEMPTS = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public void check(String key) {
        Attempts current = attempts.get(key);
        if (current != null && !current.expired() && current.count() >= MAX_ATTEMPTS) {
            throw new TooManyAttemptsException(WINDOW);
        }
    }

    public void recordFailure(String key) {
        if (attempts.size() > MAX_TRACKED_KEYS) {
            attempts.values().removeIf(Attempts::expired);
        }
        attempts.compute(key, (ignored, previous) -> previous == null || previous.expired()
                ? new Attempts(Instant.now(), 1)
                : new Attempts(previous.start(), previous.count() + 1));
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    /** Zera o estado; usado pelos testes para isolar cenarios. */
    public void clear() {
        attempts.clear();
    }

    private record Attempts(Instant start, int count) {

        boolean expired() {
            return start.plus(WINDOW).isBefore(Instant.now());
        }
    }
}
