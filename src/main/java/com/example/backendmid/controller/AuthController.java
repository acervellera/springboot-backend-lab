package com.example.backendmid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backendmid.dto.LoginRequest;
import com.example.backendmid.dto.LoginResponse;
import com.example.backendmid.dto.RegistrazioneUtenteRequest;
import com.example.backendmid.service.JwtService;
import com.example.backendmid.service.UtenteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UtenteService utenteService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(
            UtenteService utenteService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.utenteService = utenteService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/registrazione")
    public ResponseEntity<Void> registra(
            @Valid @RequestBody RegistrazioneUtenteRequest request) {

        utenteService.registra(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest) {

        Authentication authenticationReq = UsernamePasswordAuthenticationToken.unauthenticated(
                loginRequest.username(),
                loginRequest.password());

        authenticationManager.authenticate(authenticationReq);

        String token = jwtService.generaToken(loginRequest.username());

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
