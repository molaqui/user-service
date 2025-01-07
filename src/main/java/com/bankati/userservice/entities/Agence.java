package com.bankati.userservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String adresse;
    private String telephone;


    // Relation One-to-Many avec les agents
    @OneToMany(mappedBy = "agence", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<User> agents = new ArrayList<>();
}
