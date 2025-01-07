package com.bankati.userservice.config;

import com.vonage.client.VonageClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VonageConfig {
    @Bean
    public VonageClient vonageClient() {
        return VonageClient.builder()
                .apiKey("b5813842")
                .apiSecret("pDcUfRrQBiEAbd7s")
                .build();
    }
}
