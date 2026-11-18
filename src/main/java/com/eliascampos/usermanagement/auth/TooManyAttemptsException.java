package com.eliascampos.usermanagement.auth;

import java.time.Duration;

public class TooManyAttemptsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyAttemptsException(Duration window) {
        super("Muitas tentativas de login");
        this.retryAfterSeconds = window.toSeconds();
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
