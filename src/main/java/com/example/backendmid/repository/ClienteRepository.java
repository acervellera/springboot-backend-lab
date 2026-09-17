package com.example.backendmid.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backendmid.entity.Cliente;

public interface ClienteRepository
                extends JpaRepository<Cliente, Long> {

        List<Cliente> findByNomeContainingIgnoreCase(String nome);

        @Query("""
                        SELECT c
                        FROM Cliente c
                        WHERE LOWER(c.email)
                        LIKE LOWER(CONCAT('%', :testo, '%'))

                                        """)

        List<Cliente> cercaPerEmail(@Param("testo") String testo);

        @Query("""
                        SELECT DISTINCT c
                        FROM Cliente c
                        LEFT JOIN FETCH c.ordini
                        """)
        List<Cliente> findAllConOrdini();

        @EntityGraph(attributePaths = "ordini")
        @Query("SELECT c FROM Cliente c")
        List<Cliente> findAllConOrdiniEntityGraph();

}