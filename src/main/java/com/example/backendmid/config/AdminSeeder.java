package com.example.backendmid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.backendmid.entity.Ruolo;
import com.example.backendmid.entity.Utente;
import com.example.backendmid.repository.UtenteRepository;

@Configuration
@Profile("dev")
public class AdminSeeder {

    @Bean
    CommandLineRunner creaAdmin(
            UtenteRepository utenteRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:}") String adminUsername,
            @Value("${app.admin.password:}") String adminPassword) {

        return args -> {
            if (adminUsername.isBlank() || adminPassword.isBlank()) {
                return;
            }

            if (utenteRepository.findByUsername(adminUsername).isEmpty()) {
                Utente admin = new Utente(
                        adminUsername,
                        passwordEncoder.encode(adminPassword),
                        Ruolo.ADMIN);

                utenteRepository.save(admin);
            }
        };
    }
}
