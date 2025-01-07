package com.bankati.userservice.service;

import com.bankati.userservice.entities.User;
import com.bankati.userservice.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> userOptional;

        if (identifier.contains("@")) {
            // Pour Admin et Agent : Authentification par email
            userOptional = userRepository.findByEmail(identifier)
                    .filter(user -> user.getRole().name().equals("ADMIN") || user.getRole().name().equals("AGENT"));
        } else {
            // Pour Client : Authentification par numéro de téléphone
            userOptional = userRepository.findByNumeroTelephone(identifier)
                    .filter(user -> user.getRole().name().equals("CLIENT"));
        }

        User user = userOptional.orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));

        return new org.springframework.security.core.userdetails.User(
                identifier,
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }
}
