package com.example.backendmid.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.backendmid.dto.ClienteRequest;
import com.example.backendmid.dto.ClienteResponse;
import com.example.backendmid.exception.ClienteNonTrovatoException;
import com.example.backendmid.service.ClienteService;

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void trovaCliente_restituisce200() throws Exception {

        ClienteResponse response = new ClienteResponse(
                1L,
                "Mario",
                "mario@email.it");

        when(clienteService.trovaPerId(1L))
                .thenReturn(response);

        mockMvc.perform(get("/clienti/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mario"))
                .andExpect(jsonPath("$.email").value("mario@email.it"));
    }

    @Test
    void trovaCliente_nonEsiste_restituisce404() throws Exception {

        when(clienteService.trovaPerId(999L))
                .thenThrow(new ClienteNonTrovatoException(999L));

        mockMvc.perform(get("/clienti/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(
                        "Cliente non trovato con id: 999"));
    }

    @Test
    void creaCliente_restituisce201() throws Exception {

        ClienteResponse response = new ClienteResponse(
                1L,
                "Mario",
                "mario@email.it");

        when(clienteService.crea(any(ClienteRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/clienti/salvaCliente")
                .contentType("application/json")
                .content("""
                        {
                          "nome": "Mario",
                          "email": "mario@email.it"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Mario"))
                .andExpect(jsonPath("$.email").value("mario@email.it"));
    }

    @Test
    void creaCliente_datiNonValidi_restituisce400() throws Exception {

        mockMvc.perform(post("/clienti/salvaCliente")
                .contentType("application/json")
                .content("""
                        {
                          "nome": "",
                          "email": ""
                        }
                        """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clienteService);
    }
}