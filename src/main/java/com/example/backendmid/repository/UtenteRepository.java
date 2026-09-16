package com.example.backendmid.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backendmid.entity.Utente;

public interface UtenteRepository
        extends JpaRepository<Utente, Long> {

    Optional<Utente> findByUsername(String username);

    boolean existsByUsername(String username);
}