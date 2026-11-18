package com.eliascampos.usermanagement.exception;

public class InvalidPasswordException extends RuntimeException {

    public InvalidPasswordException() {
        super("A senha atual nao confere");
    }
}
