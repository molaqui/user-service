package com.bankati.userservice.config;

import com.bankati.userservice.service.CustomUserDetailsService;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    private final RsakeysConfig rsakeysConfig;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(RsakeysConfig rsakeysConfig, PasswordEncoder passwordEncoder, CustomUserDetailsService customUserDetailsService) {
        this.rsakeysConfig = rsakeysConfig;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserDetailsService(customUserDetailsService);
        return new ProviderManager(authProvider);
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
//        return httpSecurity
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/auth/login/**").permitAll()
//                        .anyRequest().authenticated()
//                       // .anyRequest().permitAll()
//
//                )
//                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
//                .httpBasic(Customizer.withDefaults())
//                .build();
//    }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
            return httpSecurity
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                    .build();
        }


    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsakeysConfig.publicKey()).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsakeysConfig.publicKey()).privateKey(rsakeysConfig.privateKey()).build();
        JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwkSource);
    }
}