package com.example.backendmid.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backendmid.dto.OrdineRequest;
import com.example.backendmid.dto.OrdineResponse;
import com.example.backendmid.entity.Cliente;
import com.example.backendmid.entity.Ordine;
import com.example.backendmid.exception.ClienteNonTrovatoException;
import com.example.backendmid.repository.ClienteRepository;
import com.example.backendmid.repository.OrdiniRepository;

@Service
public class OrdineService {

    private final OrdiniRepository ordiniRepository;
    private final ClienteRepository clienteRepository;

    public OrdineService(OrdiniRepository ordiniRepository, ClienteRepository clienteRepository) {
        this.ordiniRepository = ordiniRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public OrdineResponse crea(OrdineRequest request) {

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ClienteNonTrovatoException(request.getClienteId()));

        Ordine ordine = new Ordine(
                request.getStato(),
                cliente);

        Ordine salvato = ordiniRepository.save(ordine);

        return new OrdineResponse(
                salvato.getId(),
                salvato.getStato(),
                salvato.getCliente().getId()

        );
    }

}
