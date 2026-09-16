package com.example.backendmid.exception;

public class UsernameGiaEsistenteException extends RuntimeException {

    public UsernameGiaEsistenteException(String message) {
        super(message);
    }
}