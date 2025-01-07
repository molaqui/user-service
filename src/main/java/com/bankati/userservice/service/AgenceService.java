package com.bankati.userservice.service;

import com.bankati.userservice.entities.Agence;
import com.bankati.userservice.repository.AgenceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AgenceService {

    private final AgenceRepository agenceRepository;

    public AgenceService(AgenceRepository agenceRepository) {
        this.agenceRepository = agenceRepository;
    }

    // Créer une nouvelle agence
    public Agence createAgence(Agence agence) {
        return agenceRepository.save(agence);
    }

    // Récupérer toutes les agences
    public List<Agence> getAllAgences() {
        return agenceRepository.findAll();
    }

    // Récupérer une agence par son ID
    public Optional<Agence> getAgenceById(Long id) {
        return agenceRepository.findById(id);
    }

    // Mettre à jour une agence
    public Agence updateAgence(Long id, Agence updatedAgence) {
        return agenceRepository.findById(id).map(existingAgence -> {
            existingAgence.setNom(updatedAgence.getNom());
            existingAgence.setAdresse(updatedAgence.getAdresse());
            existingAgence.setTelephone(updatedAgence.getTelephone());
            return agenceRepository.save(existingAgence);
        }).orElse(null);
    }


    // Supprimer une agence
    public boolean deleteAgence(Long id) {
        if (agenceRepository.existsById(id)) {
            agenceRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Agence not found with ID: " + id);
        }
        return false;
    }

    // Calculer le total des agences
    public long getTotalAgences() {
        return agenceRepository.count();
    }



}
