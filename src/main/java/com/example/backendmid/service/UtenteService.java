package com.example.backendmid.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backendmid.dto.RegistrazioneUtenteRequest;
import com.example.backendmid.entity.Ruolo;
import com.example.backendmid.entity.Utente;
import com.example.backendmid.exception.UsernameGiaEsistenteException;
import com.example.backendmid.repository.UtenteRepository;

@Service
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(
            UtenteRepository utenteRepository,
            PasswordEncoder passwordEncoder) {

        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registra(RegistrazioneUtenteRequest request) {

        if (utenteRepository.existsByUsername(request.getUsername())) {
            throw new UsernameGiaEsistenteException("Username già esistente: " + request.getUsername());
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        Utente utente = new Utente(
                request.getUsername(),
                passwordHash,
                Ruolo.USER);

        utenteRepository.save(utente);
    }
}
