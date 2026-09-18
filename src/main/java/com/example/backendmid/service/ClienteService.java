package com.example.backendmid.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backendmid.dto.ClienteDettaglioResponse;
import com.example.backendmid.dto.ClienteRequest;
import com.example.backendmid.dto.ClienteResponse;
import com.example.backendmid.dto.OrdineResponse;
import com.example.backendmid.entity.Cliente;
import com.example.backendmid.exception.ClienteNonTrovatoException;
import com.example.backendmid.repository.ClienteRepository;

@Service
public class ClienteService {

    private final Logger log = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public ClienteResponse crea(ClienteRequest clienteRequest) {

        Cliente cliente = new Cliente(
                clienteRequest.getNome(),
                clienteRequest.getEmail());

        Cliente salvato = clienteRepository.save(cliente);

        return new ClienteResponse(
                salvato.getId(),
                salvato.getNome(),
                salvato.getEmail(),
                salvato.getVersion());
    }

    public Page<ClienteResponse> trovaClienti(
            int pagina,
            int dimensione,
            String campo,
            String direzione) {

        Sort sort = direzione.equalsIgnoreCase("desc")
                ? Sort.by(campo).descending()
                : Sort.by(campo).ascending();

        Pageable pageable = PageRequest.of(
                pagina,
                dimensione,
                sort);

        return clienteRepository.findAll(pageable)
                .map(cliente -> new ClienteResponse(
                        cliente.getId(),
                        cliente.getNome(),
                        cliente.getEmail(),
                        cliente.getVersion()));
    }

    public ClienteResponse trovaPerId(Long id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNonTrovatoException(id));

        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getVersion());
    }

    @Transactional
    public ClienteResponse modificaCliente(
            Long id,
            ClienteRequest clienteRequest) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNonTrovatoException(id));

        cliente.setNome(clienteRequest.getNome());
        cliente.setEmail(clienteRequest.getEmail());

        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                cliente.getVersion());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void elimina(Long id) {

        log.info("Inizio eliminazione cliente id={}", id);

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNonTrovatoException(id));

        log.info(
                "Cliente id={} trovato, procedo con l'eliminazione",
                id);

        clienteRepository.delete(cliente);

        log.info(
                "Cliente eliminato correttamente, id={}",
                id);
    }

    public List<ClienteResponse> trovaPerStringa(String nome) {

        return clienteRepository
                .findByNomeContainingIgnoreCase(nome)
                .stream()
                .map(cliente -> new ClienteResponse(
                        cliente.getId(),
                        cliente.getNome(),
                        cliente.getEmail(),
                        cliente.getVersion()))
                .toList();
    }

    public List<ClienteResponse> trovaPerEmail(String email) {

        return clienteRepository
                .cercaPerEmail(email)
                .stream()
                .map(cliente -> new ClienteResponse(
                        cliente.getId(),
                        cliente.getNome(),
                        cliente.getEmail(),
                        cliente.getVersion()))
                .toList();
    }

    public ClienteDettaglioResponse trovaClienteConOrdini(Long id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteNonTrovatoException(id));

        List<OrdineResponse> ordini = cliente.getOrdini()
                .stream()
                .map(ordine -> new OrdineResponse(
                        ordine.getId(),
                        ordine.getStato(),
                        ordine.getCliente().getId()))
                .toList();

        return new ClienteDettaglioResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getEmail(),
                ordini);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> trovaTuttiConOrdini() {

        List<Cliente> clienti = clienteRepository.findAllConOrdiniEntityGraph();

        return clienti.stream()
                .map(cliente -> {

                    System.out.println(
                            cliente.getNome()
                                    + " -> ordini: "
                                    + cliente.getOrdini().size());

                    return new ClienteResponse(
                            cliente.getId(),
                            cliente.getNome(),
                            cliente.getEmail(),
                            cliente.getVersion());
                })
                .toList();
    }
}