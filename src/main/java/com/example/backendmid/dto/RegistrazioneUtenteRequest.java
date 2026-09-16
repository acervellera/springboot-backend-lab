package com.example.backendmid.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrazioneUtenteRequest {

    @NotBlank
    @Size(min = 3, max = 30)
    String username;

    @NotBlank
    @Size(min = 8, max = 100)
    String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}