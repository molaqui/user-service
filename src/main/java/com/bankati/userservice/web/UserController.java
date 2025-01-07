
package com.bankati.userservice.web;

import com.bankati.userservice.Models.Transaction;
import com.bankati.userservice.entities.Agence;
import com.bankati.userservice.entities.User;
import com.bankati.userservice.enums.Role;
import com.bankati.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping("/add-agent")
    public ResponseEntity<User> addAgent(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String typePieceIdentite,
            @RequestParam String numeroPieceIdentite,
            @RequestParam String dateDeNaissance,
            @RequestParam String adresse,
            @RequestParam String email,
            @RequestParam String numeroTelephone,
            @RequestParam String numeroImmatriculation,
            @RequestParam String numeroPatente,
            @RequestParam Long agenceId,
            @RequestParam(required = false) MultipartFile imageRecto,
            @RequestParam(required = false) MultipartFile imageVerso
    ) {
        try {
            LocalDate birthDate = LocalDate.parse(dateDeNaissance);
            User savedUser = userService.addAgent(nom, prenom, typePieceIdentite, numeroPieceIdentite,
                    birthDate, adresse, email, numeroTelephone, numeroImmatriculation, numeroPatente, imageRecto, imageVerso,agenceId);
            return ResponseEntity.ok(savedUser);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }


    // Récupérer tous les agents
    @GetMapping("/agents")
    public ResponseEntity<List<User>> getAllAgents() {
        List<User> agents = userService.getAllAgents();
        return ResponseEntity.ok(agents);
    }

    // Récupérer un agent par ID
    @GetMapping("/agent/{id}")
    public ResponseEntity<User> getAgentById(@PathVariable Long id) {
        User agent = userService.getAgentById(id);
        if (agent != null) {
            return ResponseEntity.ok(agent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Mettre à jour un agent
    @PutMapping("/update-agent/{id}")
    public ResponseEntity<User> updateAgent(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String typePieceIdentite,
            @RequestParam String numeroPieceIdentite,
            @RequestParam String dateDeNaissance,
            @RequestParam String adresse,
            @RequestParam String email,
            @RequestParam String numeroTelephone,
            @RequestParam String numeroImmatriculation,
            @RequestParam String numeroPatente
    ) {
        try {
            LocalDate birthDate = LocalDate.parse(dateDeNaissance);
            User updatedUser = userService.updateAgent(id, nom, prenom, typePieceIdentite, numeroPieceIdentite,
                    birthDate, adresse, email, numeroTelephone, numeroImmatriculation, numeroPatente);
            return ResponseEntity.ok(updatedUser);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }


    @PutMapping("/toggle-active/{id}")
    public ResponseEntity<User> toggleUserActiveStatus(@PathVariable Long id) {
        User updatedUser = userService.toggleUserActiveStatus(id);
        return ResponseEntity.ok(updatedUser);
    }
    @GetMapping("/count/{role}")
    public long getTotalUsersByRole(@PathVariable Role role) {
        return userService.getTotalUsersByRole(role);
    }

    ///////////////////////////////////////////////CLIENT START ////////////////////////////////
    // Endpoint pour ajouter un client
    @PostMapping("/add-client")
    public ResponseEntity<User> addClient(
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String typePieceIdentite,
            @RequestParam String numeroPieceIdentite,
            @RequestParam LocalDate dateDeNaissance,
            @RequestParam String adresse,
            @RequestParam String email,
            @RequestParam String numeroTelephone,
            @RequestParam(required = false) MultipartFile imageRecto,
            @RequestParam(required = false) MultipartFile imageVerso,
            @RequestParam Long agentId,
            @RequestParam BigDecimal soldeInitial, // Nouveau paramètre pour le solde initial
            HttpServletRequest request // Inject HttpServletRequest to get IP and User-Agent
    ) throws IOException {

        // Pass the HttpServletRequest to the service layer
        User newUser = userService.addClient(nom, prenom, typePieceIdentite, numeroPieceIdentite, dateDeNaissance,
                adresse, email, numeroTelephone, imageRecto, imageVerso, agentId, soldeInitial, request);

        return ResponseEntity.ok(newUser);
    }

    // Endpoint pour récupérer tous les clients
    @GetMapping("/clients")
    public ResponseEntity<List<User>> getAllClients() {
        List<User> clients = userService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    // Endpoint pour récupérer un client par ID
    @GetMapping("/client/{id}")
    public ResponseEntity<User> getClientById(@PathVariable Long id) {
        User client = userService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    // Endpoint pour mettre à jour un client
    @PutMapping("/update-client/{id}")
    public ResponseEntity<User> updateClient(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam String prenom,
            @RequestParam String typePieceIdentite,
            @RequestParam String numeroPieceIdentite,
            @RequestParam LocalDate dateDeNaissance,
            @RequestParam String adresse,
            @RequestParam String email,
            @RequestParam String numeroTelephone

    ) throws IOException {
        User updatedUser = userService.updateClient(id, nom, prenom, typePieceIdentite, numeroPieceIdentite,
                dateDeNaissance, adresse, email, numeroTelephone );
        return ResponseEntity.ok(updatedUser);
    }
    @GetMapping("/clients-by-agent/{agentId}")
    public ResponseEntity<List<User>> getClientsByAgent(@PathVariable Long agentId) {
        List<User> clients = userService.getClientsByAgent(agentId);
        return ResponseEntity.ok(clients);
    }
    @GetMapping("/{userId}/transactions")
    public ResponseEntity<List<Transaction>> getTransactionsByUserId(@PathVariable Long userId) {
        List<Transaction> transactions = userService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/{userId}/ajouter-solde")
    public ResponseEntity<BigDecimal> ajouterSolde(@PathVariable Long userId, @RequestParam BigDecimal montant) {
        BigDecimal nouveauSolde = userService.ajouterSolde(userId, montant);
        return ResponseEntity.ok(nouveauSolde);
    }

    @GetMapping("/client/{id}/details")
    public ResponseEntity<Map<String, Object>> getClientDetailsById(@PathVariable Long id) {
        User client = userService.getClientById(id);
        if (client != null) {
            // Construire une réponse sous forme de Map
            Map<String, Object> clientDetails = new HashMap<>();
            clientDetails.put("nom", client.getNom());
            clientDetails.put("prenom", client.getPrenom());
            return ResponseEntity.ok(clientDetails);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Client avec l'ID " + id + " non trouvé"));
        }
    }


    @PutMapping("/{id}/update-password")
    public ResponseEntity<?> updatePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {

        String newPassword = requestBody.get("newPassword");

        if (newPassword == null || newPassword.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe est requis"));
        }

        try {
            // Mettre à jour le mot de passe et marquer comme changé
            userService.updatePassword(id, newPassword);
            return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/client/{clientId}/agence")
    public ResponseEntity<Agence> getAgenceByClientId(@PathVariable Long clientId) {
        return userService.getAgenceByClientId(clientId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    //  trouver un agent par userId
    @GetMapping("/{id}/agence")
    public ResponseEntity<Agence> getAgenceByAgentId(@PathVariable Long id) {
        return userService.getAgenceByAgentId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    // Récupérer les clients par agence
    @GetMapping("/agence/{agenceId}/clients")
    public ResponseEntity<List<User>> getClientsByAgence(@PathVariable Long agenceId) {
        List<User> clients = userService.getClientsByAgence(agenceId);
        if (clients.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clients);
    }
    // Récupérer les agents par agence
    @GetMapping("/agence/{agenceId}/agents")
    public ResponseEntity<List<User>> getAgentsByAgence(@PathVariable Long agenceId) {
        List<User> agents = userService.getAgentsByAgence(agenceId);
        if (agents.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(agents);
    }



    @GetMapping("/email/{email}/isActive")
    public ResponseEntity<Boolean> checkUserActiveByEmail(@PathVariable String email) {
        boolean isActive = userService.isUserActiveByEmail(email);
        return ResponseEntity.ok(isActive);
    }

    @PutMapping("/agent/{agentId}/update-agence")
    public ResponseEntity<User> updateAgentAgence(
            @PathVariable Long agentId,
            @RequestParam Long newAgenceId) {
        User updatedAgent = userService.updateAgentAgence(agentId, newAgenceId);
        return ResponseEntity.ok(updatedAgent);
    }

    @GetMapping("/clients/{userId}/cardholderId")
    public String getCardholderId(@PathVariable Long userId) {
        return userService.getCardHolderIdByUserId(userId);
    }

    @GetMapping("/is-active-by-telephone")
    public ResponseEntity<Boolean> isUserActiveByNumeroTelephone(@RequestParam String numeroTelephone) {
        boolean isActive = userService.isUserActiveByNumeroTelephone(numeroTelephone);
        return ResponseEntity.ok(isActive);
    }

}


