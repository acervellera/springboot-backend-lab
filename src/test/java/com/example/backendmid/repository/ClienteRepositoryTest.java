package com.example.backendmid.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.example.backendmid.TestcontainersConfiguration;
import com.example.backendmid.entity.Cliente;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void cercaPerNome_restituisceCliente() {

        Cliente cliente = new Cliente("Mario", "mario@email.it");
        clienteRepository.save(cliente);

        List<Cliente> risultati =
                clienteRepository.findByNomeContainingIgnoreCase("mar");

        assertThat(risultati)
                .extracting(Cliente::getEmail)
                .contains("mario@email.it");
    }
}
