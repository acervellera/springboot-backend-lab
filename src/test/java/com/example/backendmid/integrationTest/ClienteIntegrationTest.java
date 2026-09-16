package com.example.backendmid.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.backendmid.dto.ClienteRequest;
import com.example.backendmid.dto.ClienteResponse;
import com.example.backendmid.service.ClienteService;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class ClienteIntegrationTest {

    @Autowired
    private ClienteService clienteService;

    @Test
    void clienteTest() {

        ClienteRequest request = new ClienteRequest();
        request.setNome("Mario");
        request.setEmail("mario@test.it");

        ClienteResponse creato = clienteService.crea(request);

        Long id = creato.id();

        ClienteResponse trovato = clienteService.trovaPerId(id);

        assertThat(trovato.id()).isEqualTo(id);
        assertThat(trovato.nome()).isEqualTo("Mario");
    }
}