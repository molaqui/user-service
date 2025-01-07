package com.bankati.userservice.repository;



import com.bankati.userservice.entities.Agence;
import com.bankati.userservice.entities.User;
import com.bankati.userservice.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

        Optional<User> findByEmail(String email);
        Optional<User> findByNumeroTelephone(String numeroTelephone);

        List<User> findByRole(Role role);

        Optional<User> findByIdAndRole(Long id, Role role);

        boolean existsByIdAndRole(Long id, Role role);
        long countByRole(Role role);
        List<User> findByAgent(User agent);
        @Query("SELECT u.agence FROM User u WHERE u.id = :id AND u.role = 'AGENT'")
        Optional<Agence> findAgenceByAgentId(@Param("id") Long id);



        @Query("SELECT u.agence FROM User u WHERE u.id = :id AND u.role = 'CLIENT'")
        Optional<Agence> findAgenceByClientId(@Param("id") Long id);

        // Récupérer tous les clients d'une agence
        @Query("SELECT u FROM User u WHERE u.agence.id = :agenceId AND u.role = 'CLIENT'")
        List<User> findClientsByAgenceId(@Param("agenceId") Long agenceId);

        // Récupérer tous les agents d'une agence
        @Query("SELECT u FROM User u WHERE u.agence.id = :agenceId AND u.role = 'AGENT'")
        List<User> findAgentsByAgenceId(@Param("agenceId") Long agenceId);

        @Query("SELECT u.cardholder_id FROM User u WHERE u.id = :userId")
        String findCardholderIdByUserId(@Param("userId") Long userId);


}
