package com.example.backendmid.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backendmid.dto.ClienteDettaglioResponse;
import com.example.backendmid.dto.ClienteRequest;
import com.example.backendmid.dto.ClienteResponse;
import com.example.backendmid.service.ClienteService;

import jakarta.validation.Valid;

@RestController
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/clienti/{id}")
    public ResponseEntity<ClienteResponse> trovaPerId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.trovaPerId(id));
    }

    @PostMapping("/clienti/salvaCliente")
    public ResponseEntity<ClienteResponse> creaCliente(@Valid @RequestBody ClienteRequest clienteRequest) {

        ClienteResponse clienteResponse = clienteService.crea(clienteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteResponse);
    }

    @GetMapping("/clienti/trovatutti")
    public ResponseEntity<Page<ClienteResponse>> trovaTutti(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "5") int dimensione,
            @RequestParam(defaultValue = "nome") String campo,
            @RequestParam(defaultValue = "asc") String direzione) {

        return ResponseEntity.ok(
                clienteService.trovaClienti(
                        pagina,
                        dimensione,
                        campo,
                        direzione));
    }

    @PutMapping("/clienti/aggiorna/{id}")
    public ResponseEntity<ClienteResponse> aggiornaUtente(@Valid @PathVariable Long id,
            @RequestBody ClienteRequest clienteRequest) {
        return ResponseEntity.ok(clienteService.modificaCliente(id, clienteRequest));

    }

    @DeleteMapping("/clienti/elimina/{id}")
    public ResponseEntity<Void> eliminaCliente(@PathVariable Long id) {
        clienteService.elimina(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/clienti/cercaPerNome")
    public ResponseEntity<List<ClienteResponse>> trovaPerNome(@RequestParam String nome) {

        return ResponseEntity.ok(clienteService.trovaPerStringa(nome));
    }

    @GetMapping("/clienti/cercaPerEmail")
    public ResponseEntity<List<ClienteResponse>> trovaPerEmail(@RequestParam String email) {
        return ResponseEntity.ok(clienteService.trovaPerEmail(email));
    }

    @GetMapping("/clienti/{id}/dettaglio")
    public ResponseEntity<ClienteDettaglioResponse> dettaglio(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.trovaClienteConOrdini(id));
    }

}