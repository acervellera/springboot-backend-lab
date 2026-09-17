package com.example.backendmid.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backendmid.dto.OrdineRequest;
import com.example.backendmid.dto.OrdineResponse;
import com.example.backendmid.service.OrdineService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/ordini")
public class OrdineController {

    private final OrdineService ordineService;

    public OrdineController(OrdineService ordineService) {
        this.ordineService = ordineService;
    }

    @PostMapping
    public ResponseEntity<OrdineResponse> creaOrdine(
            @Valid @RequestBody OrdineRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ordineService.crea(request));
    }
}
