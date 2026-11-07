package com.eliascampos.usermanagement.exception;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Ja existe um usuario cadastrado com o e-mail " + email);
    }
}
