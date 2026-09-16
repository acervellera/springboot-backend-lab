package com.example.backendmid.exception;

public class ClienteNonTrovatoException extends RuntimeException {

    public ClienteNonTrovatoException(long id) {
        super("Cliente non trovato con id: " + id);
    }

    public ClienteNonTrovatoException(String string) {
        //TODO Auto-generated constructor stub
    }
}