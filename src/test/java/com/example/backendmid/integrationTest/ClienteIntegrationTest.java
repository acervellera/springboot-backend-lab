package com.example.backendmid.integrationTest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.backendmid.dto.ClienteRequest;
import com.example.backendmid.dto.ClienteResponse;
import com.example.backendmid.service.ClienteService;

import jakarta.transaction.Transactional;

@Transactional
class ClienteIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ClienteService clienteService;

    @Test
    void creaCliente_ePoiLoRilegge() {

        ClienteRequest request = new ClienteRequest();
        request.setNome("Mario");
        request.setEmail("mario@test.it");

        ClienteResponse creato = clienteService.crea(request);

        ClienteResponse trovato = clienteService.trovaPerId(creato.id());

        assertThat(trovato.id()).isEqualTo(creato.id());
        assertThat(trovato.nome()).isEqualTo("Mario");
        assertThat(trovato.email()).isEqualTo("mario@test.it");
    }
}
