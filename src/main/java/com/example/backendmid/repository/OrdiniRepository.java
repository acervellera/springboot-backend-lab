package com.example.backendmid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backendmid.entity.Ordine;

public interface OrdiniRepository extends JpaRepository<Ordine, Long> {

}
