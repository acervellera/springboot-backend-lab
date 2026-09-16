package com.example.backendmid.dto;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public record ClienteDettaglioResponse(Long id, String nome, String email, List<OrdineResponse> ordineRes) {
}
