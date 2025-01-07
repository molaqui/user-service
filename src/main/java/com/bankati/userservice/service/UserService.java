
package com.bankati.userservice.service;

import com.bankati.userservice.FeignCompte.PaymentServiceFeignClient;

import com.bankati.userservice.Models.Compte;

import com.bankati.userservice.Models.Transaction;
import com.bankati.userservice.entities.Agence;
import com.bankati.userservice.web.SmsController;
import com.stripe.Stripe;
import com.stripe.model.issuing.Cardholder;
import com.stripe.param.issuing.CardholderCreateParams;
import jakarta.annotation.PostConstruct;
import com.bankati.userservice.entities.User;
import com.bankati.userservice.enums.Role;
import com.bankati.userservice.enums.TypePieceIdentite;
import com.bankati.userservice.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PaymentServiceFeignClient compteClient;

    @Autowired
    private PaymentServiceFeignClient paymentFeignClient;

    @Autowired
    private SmsController smsController;
    @Autowired
    private AgenceService agenceService;
    @Autowired
    private StripeService stripeService;



    // Déclarer le chemin d'upload comme un Path dynamique
    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");

    // Méthode init pour créer le dossier si nécessaire
    @PostConstruct
    public void init() {
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create upload directory", e);
        }
    }

    //Agent
    public User addAgent(String nom,
                         String prenom,
                         String typePieceIdentite,
                         String numeroPieceIdentite,
                         LocalDate dateDeNaissance,
                         String adresse,
                         String email,
                         String numeroTelephone,
                         String numeroImmatriculation,
                         String numeroPatente,
                         MultipartFile imageRecto,
                         MultipartFile imageVerso,
                          Long agenceId) throws IOException {


        Agence agence = agenceService.getAgenceById(agenceId)
                .orElseThrow(() -> new IllegalArgumentException("Agence not found with ID: " + agenceId));

        // Générer un mot de passe aléatoire
        String generatedPassword = generateRandomPassword(8);

        // Créer une instance de l'utilisateur
        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setTypePieceIdentite(Enum.valueOf(TypePieceIdentite.class, typePieceIdentite));
        user.setNumeroPieceIdentite(numeroPieceIdentite);
        user.setDateDeNaissance(dateDeNaissance);
        user.setAdresse(adresse);
        user.setEmail(email);
        user.setNumeroTelephone(numeroTelephone);
        user.setNumeroImmatriculation(numeroImmatriculation);
        user.setNumeroPatente(numeroPatente);
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(Role.AGENT);
        user.setAgence(agence);

        // Sauvegarder l'image recto avec un nom unique
        if (imageRecto != null && !imageRecto.isEmpty()) {
            String uniqueRectoName = System.currentTimeMillis() + "_" + imageRecto.getOriginalFilename();
            Path rectoPath = uploadDir.resolve(uniqueRectoName);
            imageRecto.transferTo(rectoPath.toFile());
            user.setImageRecto("/uploads/" + uniqueRectoName);
        }

        // Sauvegarder l'image verso avec un nom unique
        if (imageVerso != null && !imageVerso.isEmpty()) {
            String uniqueVersoName = System.currentTimeMillis() + "_" + imageVerso.getOriginalFilename();
            Path versoPath = uploadDir.resolve(uniqueVersoName);
            imageVerso.transferTo(versoPath.toFile());
            user.setImageVerso("/uploads/" + uniqueVersoName);
        }


        // Enregistrer l'utilisateur
        User savedUser = userRepository.save(user);

        // Envoyer l'email avec le mot de passe généré
        String subject = "Bienvenue ! Voici vos identifiants de connexion";
        String message = "Bonjour " + prenom + " " + nom + ",\n\n" +
                "Votre compte a été créé avec succès.\n" +
                "Voici vos identifiants de connexion :\n" +
                "Login : " + email + "\n" +
                "Mot de passe : " + generatedPassword + "\n\n" +
                "Veuillez vous connecter et changer votre mot de passe dès que possible.\n\n" +
                "Cordialement,\nL'équipe de support.";

        emailService.sendEmail(email, subject, message);

        return savedUser;
    }

    // Récupérer tous les agents
    public List<User> getAllAgents() {
        return userRepository.findByRole(Role.AGENT);
    }

    // Récupérer un agent par son ID
    public User getAgentById(Long id) {
        return userRepository.findByIdAndRole(id, Role.AGENT).orElse(null);
    }

    // Mettre à jour un agent
    public User updateAgent(Long id, String nom, String prenom, String typePieceIdentite, String numeroPieceIdentite,
                            LocalDate dateDeNaissance, String adresse, String email, String numeroTelephone,
                            String numeroImmatriculation, String numeroPatente) throws IOException {

        // Récupérer l'agent existant
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Agent non trouvé avec l'ID : " + id));

        // Mettre à jour les champs
        existingUser.setNom(nom);
        existingUser.setPrenom(prenom);
        existingUser.setTypePieceIdentite(TypePieceIdentite.valueOf(typePieceIdentite));
        existingUser.setNumeroPieceIdentite(numeroPieceIdentite);
        existingUser.setDateDeNaissance(dateDeNaissance);
        existingUser.setAdresse(adresse);
        existingUser.setEmail(email);
        existingUser.setNumeroTelephone(numeroTelephone);
        existingUser.setNumeroImmatriculation(numeroImmatriculation);
        existingUser.setNumeroPatente(numeroPatente);
        return userRepository.save(existingUser);
    }

    @Transactional
    public User toggleUserActiveStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID : " + id));

        user.setActive(!user.isActive());

        return userRepository.save(user);
    }


    public String generateRandomPassword(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }

        return password.toString();
    }

    // Méthode pour calculer le nombre total d'utilisateurs selon le rôle
    public long getTotalUsersByRole(Role role) {
        return userRepository.countByRole(role);
    }

    /////////////////////client start ////////////////////////
    // Récupérer tous les clients
    public List<User> getAllClients() {
        return userRepository.findByRole(Role.CLIENT);
    }

    // Récupérer un client par son ID
    public User getClientById(Long id) {
        return userRepository.findByIdAndRole(id, Role.CLIENT).orElse(null);
    }

    // Mettre à jour un client
    public User updateClient(Long id, String nom, String prenom, String typePieceIdentite, String numeroPieceIdentite,
                             LocalDate dateDeNaissance, String adresse, String email, String numeroTelephone) throws IOException {

        // Récupérer le client existant
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé avec l'ID : " + id));

        // Mettre à jour les champs

        stripeService.updateCardholder(existingUser.getCardholder_id(),nom,prenom,email);

        existingUser.setNom(nom);
        existingUser.setPrenom(prenom);
        existingUser.setTypePieceIdentite(TypePieceIdentite.valueOf(typePieceIdentite));
        existingUser.setNumeroPieceIdentite(numeroPieceIdentite);
        existingUser.setDateDeNaissance(dateDeNaissance);
        existingUser.setAdresse(adresse);
        existingUser.setEmail(email);
        existingUser.setNumeroTelephone(numeroTelephone);

        return userRepository.save(existingUser);
    }

    // Bascule du statut actif/inactif d’un client
    @Transactional
    public User toggleClientActiveStatus(Long id) {
        User user = userRepository.findByIdAndRole(id, Role.CLIENT)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé avec l'ID : " + id));

        user.setActive(!user.isActive());

        return userRepository.save(user);
    }

    public User addClient(String nom,
                          String prenom,
                          String typePieceIdentite,
                          String numeroPieceIdentite,
                          LocalDate dateDeNaissance,
                          String adresse,
                          String email,
                          String numeroTelephone,
                          MultipartFile imageRecto,
                          MultipartFile imageVerso,
                          Long agentId,
                          BigDecimal soldeInitial,
                          HttpServletRequest request) throws IOException {

        //BigDecimal soldeInitial= BigDecimal.valueOf(0.0);
        // Trouver l'agent qui ajoute ce client
        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found with ID: " + agentId));
        // Trouver l'agence associée à l'agent via agentId
        Agence agence =getAgenceByAgentId(agentId)
                .orElseThrow(() -> new IllegalArgumentException("No agency found for the specified agent."));

        // Générer un mot de passe aléatoire
        String generatedPassword = generateRandomPassword(8);

        Cardholder cardholder = stripeService.createCardholder(nom,prenom,email,request.getHeader("X-Forwarded-For"),request.getHeader("User-Agent"));


        // Créer une instance de l'utilisateur (client)
        User user = new User();
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setTypePieceIdentite(Enum.valueOf(TypePieceIdentite.class, typePieceIdentite));
        user.setNumeroPieceIdentite(numeroPieceIdentite);
        user.setDateDeNaissance(dateDeNaissance);
        user.setAdresse(adresse);
        user.setEmail(email);
        user.setNumeroTelephone(numeroTelephone);
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setRole(Role.CLIENT);

        // Assigner l'agent
        user.setAgent(agent);
        user.setCardholder_id(cardholder.getId());
            user.setAgence(agence);


        // Sauvegarder l'image recto avec un nom unique
        if (imageRecto != null && !imageRecto.isEmpty()) {
            String uniqueRectoName = System.currentTimeMillis() + "_" + imageRecto.getOriginalFilename();
            Path rectoPath = uploadDir.resolve(uniqueRectoName);
            imageRecto.transferTo(rectoPath.toFile());
            user.setImageRecto("/uploads/" + uniqueRectoName);
        }

        // Sauvegarder l'image verso avec un nom unique
        if (imageVerso != null && !imageVerso.isEmpty()) {
            String uniqueVersoName = System.currentTimeMillis() + "_" + imageVerso.getOriginalFilename();
            Path versoPath = uploadDir.resolve(uniqueVersoName);
            imageVerso.transferTo(versoPath.toFile());
            user.setImageVerso("/uploads/" + uniqueVersoName);
        }

        // Enregistrer le client
        User savedUser = userRepository.save(user);

        // Appeler le service de paiement pour créer un compte pour ce client
        try {
            ResponseEntity<Compte> response = compteClient.creerCompte(savedUser.getId(), soldeInitial);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Compte créé avec succès pour le client ID: " + savedUser.getId());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du compte pour le client ID: " + savedUser.getId());
        }


        // Envoyer l'email avec le mot de passe généré
        String subject = "Bienvenue ! Voici vos identifiants de connexion";
        String message = "Bonjour " + prenom + " " + nom + ",\n\n" +
                "Votre compte a été créé avec succès.\n" +
                "Voici vos identifiants de connexion :\n" +
                "Login : " + numeroTelephone + "\n" +
                "Mot de passe : " + generatedPassword + "\n\n" +
                "Veuillez vous connecter et changer votre mot de passe dès que possible.\n\n" +
                "Cordialement,\nL'équipe de support.";

        emailService.sendEmail(email, subject, message);

        // Envoyer le SMS
//        String smsMessage = "Bonjour " + prenom + ", votre compte a été créé. Login : " + numeroTelephone + ", Mot de passe : " + generatedPassword;
//        String smsResponse = smsController.sendSms(numeroTelephone, smsMessage);
//        System.out.println("SMS Response: " + smsResponse);

        return savedUser;
    }

    // Récupérer les clients pour un agent spécifique
    public List<User> getClientsByAgent(Long agentId) {
        // Vérifier si l'agent existe
        User agent = userRepository.findByIdAndRole(agentId, Role.AGENT)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found with ID: " + agentId));

        // Retourner les clients associés à cet agent
        return userRepository.findByAgent(agent);
    }


    public List<Transaction> getTransactionsByUserId(Long userId) {
        return paymentFeignClient.listerTransactionsParUserId(userId);
    }

    public BigDecimal ajouterSolde(Long userId, BigDecimal montant) {
        return paymentFeignClient.ajouterMontantAuSolde(userId, montant).getBody();
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChanged(true); // Indiquer que le mot de passe a été changé
        userRepository.save(user);
    }


    public Optional<Agence> getAgenceByAgentId(Long agentId) {
        return userRepository.findAgenceByAgentId(agentId);
    }

    public Optional<Agence> getAgenceByClientId(Long clientId) {
        return userRepository.findAgenceByClientId(clientId);
    }

    // Récupérer les clients par agence
    public List<User> getClientsByAgence(Long agenceId) {
        return userRepository.findClientsByAgenceId(agenceId);
    }

    // Récupérer les agents par agence
    public List<User> getAgentsByAgence(Long agenceId) {
        return userRepository.findAgentsByAgenceId(agenceId);
    }



    public boolean isUserActiveByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'email : " + email));
        return user.isActive();
    }

    public boolean isUserActiveByNumeroTelephone(String numeroTelephone) {
        User user = userRepository.findByNumeroTelephone(numeroTelephone)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec le numéro de téléphone : " + numeroTelephone));
        return user.isActive();
    }

    @Transactional
    public User updateAgentAgence(Long agentId, Long newAgenceId) {
        // Vérifier si l'agent existe
        User agent = userRepository.findByIdAndRole(agentId, Role.AGENT)
                .orElseThrow(() -> new IllegalArgumentException("Agent non trouvé avec l'ID : " + agentId));

        // Vérifier si la nouvelle agence existe
        Agence newAgence = agenceService.getAgenceById(newAgenceId)
                .orElseThrow(() -> new IllegalArgumentException("Agence non trouvée avec l'ID : " + newAgenceId));

        // Mettre à jour l'agence de l'agent
        agent.setAgence(newAgence);

        // Sauvegarder les modifications
        return userRepository.save(agent);
    }


    public String getCardHolderIdByUserId(Long userId){
        return userRepository.findCardholderIdByUserId(userId);
    }

}
