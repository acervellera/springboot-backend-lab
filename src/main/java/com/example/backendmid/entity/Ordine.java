package com.example.backendmid.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ordini_jpa")
public class Ordine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String stato;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    Cliente cliente;

    public Ordine() {
    }

    public Ordine(String stato, Cliente cliente) {
        this.stato = stato;
        this.cliente = cliente;
    }

    public Long getId() {
        return id;
    }

    public String getStato() {
        return stato;
    }

    public Cliente getCliente() {
        return cliente;
    }

}
