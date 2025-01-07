package com.bankati.userservice.repository;

import com.bankati.userservice.entities.Agence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgenceRepository extends JpaRepository<Agence, Long> {

    Optional<Agence> findById(Long id);
}
