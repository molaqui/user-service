package com.bankati.userservice.web;

import com.bankati.userservice.entities.Agence;

import com.bankati.userservice.service.AgenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/agences")
public class AgenceController {

    private final AgenceService agenceService;

    public AgenceController(AgenceService agenceService) {
        this.agenceService = agenceService;
    }

    // 1. Récupérer toutes les agences
    @GetMapping
    public ResponseEntity<List<Agence>> getAllAgences() {
        List<Agence> agences = agenceService.getAllAgences();
        return ResponseEntity.ok(agences);
    }

    // 2. Récupérer une agence par ID
    @GetMapping("/{id}")
    public ResponseEntity<Agence> getAgenceById(@PathVariable Long id) {
        return agenceService.getAgenceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Ajouter une nouvelle agence
    @PostMapping
    public ResponseEntity<Agence> createAgence(@RequestBody Agence agence) {
        Agence newAgence = agenceService.createAgence(agence);
        return ResponseEntity.ok(newAgence);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agence> updateAgence(@PathVariable Long id, @RequestBody Agence updatedAgence) {
        Agence agence = agenceService.updateAgence(id, updatedAgence);
        if (agence != null) {
            return ResponseEntity.ok(agence);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    // 5. Supprimer une agence par ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgence(@PathVariable Long id) {
        if (agenceService.deleteAgence(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping("/total")
    public ResponseEntity<Long> getTotalAgences() {
        long totalAgences = agenceService.getTotalAgences();
        return ResponseEntity.ok(totalAgences);
    }

}
