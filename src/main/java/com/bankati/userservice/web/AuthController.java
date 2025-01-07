package com.bankati.userservice.web;

import com.bankati.userservice.entities.User;
import com.bankati.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")

public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtEncoder jwtEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthController(JwtEncoder jwtEncoder, AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.jwtEncoder = jwtEncoder;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> authRequest) {
        String identifier = authRequest.get("identifier");
        String password = authRequest.get("password");

        Optional<User> userOptional;

        // Vérifier si l'identifiant est un email ou un numéro de téléphone
        if (identifier.contains("@")) {
            userOptional = userRepository.findByEmail(identifier);
        } else {
            userOptional = userRepository.findByNumeroTelephone(identifier);
        }

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Utilisateur non trouvé"));
        }

        User user = userOptional.get();

        try {
            // Authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, password)
            );

            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(" "));

            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .subject(identifier)
                    .issuedAt(now)
                    .expiresAt(now.plus(5, ChronoUnit.MINUTES))
                    .claim("scope", scope)
                    .build();

            String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

            // Réponse
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", token);
            response.put("user", Map.of(
                    "id", user.getId(),
                    "name", user.getNom() + " " + user.getPrenom(),
                    "email", user.getEmail(),
                    "role", user.getRole(),
                    "agenceId", user.getAgence() != null ? user.getAgence().getId() : null
            ));
            response.put("passwordChanged", user.isPasswordChanged());

            return ResponseEntity.ok(response);

        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Mot de passe incorrect"));
        }
    }
}

