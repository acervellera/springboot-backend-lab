package com.example.backendmid.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import com.example.backendmid.entity.Cliente;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void cercaPerNome_restituisceCliente() {

        Cliente cliente = new Cliente("Mario", "mario@email.it");

        clienteRepository.save(cliente);

        List<Cliente> risultati = clienteRepository
                .findByNomeContainingIgnoreCase("mar");

        assertThat(risultati).isNotEmpty();
    }
}