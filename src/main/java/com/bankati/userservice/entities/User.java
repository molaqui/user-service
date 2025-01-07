package com.bankati.userservice.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import com.bankati.userservice.enums.Role;
import com.bankati.userservice.enums.TypePieceIdentite;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;
    @Enumerated(EnumType.STRING)
    private TypePieceIdentite typePieceIdentite;

    private String numeroPieceIdentite;
    private LocalDate dateDeNaissance;
    private String adresse;
    @Column(unique = true)
    private String email;
    @Column(unique = true, nullable = false)
    private String numeroTelephone;
    private String numeroImmatriculation;
    private String numeroPatente;
    @JsonIgnore
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    private String cardholder_id;
    private String imageRecto;
    private String imageVerso;




    private boolean isActive = true;
    private boolean passwordChanged = false; // Par défaut, non changé
    // Relation auto-référencée pour lier les clients à un agent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    @JsonIgnore
    private User agent;

    @OneToMany(mappedBy = "agent")
    @JsonManagedReference
    @JsonIgnore
    private List<User> clients = new ArrayList<>();

    // Relation Many-to-One avec Agence
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id") // Clé étrangère vers l'agence
    @JsonIgnore
    private Agence agence;

}
