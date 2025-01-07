package com.bankati.userservice.service;

import com.stripe.Stripe;
import com.stripe.model.issuing.Cardholder;
import com.stripe.param.issuing.CardholderCreateParams;
import com.stripe.param.issuing.CardholderUpdateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class StripeService {

    // Inject API key from application.properties or application.yml
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    public Cardholder createCardholder(String nom, String prenom, String email, String userIp, String userAgent) {
        // Set the Stripe API key
        Stripe.apiKey = stripeApiKey;

        try {
            // Default values for IP and User Agent if not provided
            String defaultIp = "154.72.37.45";
            String defaultUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)";

            // If no IP or User Agent is provided, use the default ones
            String ipToUse = (userIp != null && !userIp.isEmpty()) ? userIp : defaultIp;
            String userAgentToUse = (userAgent != null && !userAgent.isEmpty()) ? userAgent : defaultUserAgent;

            // Define parameters for cardholder creation
            CardholderCreateParams params = CardholderCreateParams.builder()
                    .setType(CardholderCreateParams.Type.INDIVIDUAL) // Cardholder type
                    .setName(nom + " " + prenom)
                    .setEmail(email)
                    .setBilling(
                            CardholderCreateParams.Billing.builder()
                                    .setAddress(
                                            CardholderCreateParams.Billing.Address.builder()
                                                    .setLine1("456 Elm Street") // Updated address
                                                    .setCity("Chicago")
                                                    .setState("IL")
                                                    .setCountry("US")
                                                    .setPostalCode("60616")
                                                    .build()
                                    )
                                    .build()
                    )
                    .setIndividual(
                            CardholderCreateParams.Individual.builder()
                                    .setFirstName(prenom) // First name
                                    .setLastName(nom) // Last name
                                    .setCardIssuing(
                                            CardholderCreateParams.Individual.CardIssuing.builder()
                                                    .setUserTermsAcceptance(
                                                            CardholderCreateParams.Individual.CardIssuing.UserTermsAcceptance.builder()
                                                                    .setDate(Instant.now().getEpochSecond()) // Set current date (Unix timestamp)
                                                                    .setIp(ipToUse) // IP address (either provided or default)
                                                                    .setUserAgent(userAgentToUse) // User agent (either provided or default)
                                                                    .build()
                                                    )
                                                    .build()
                                    )
                                    .build()
                    )
                    .build();

            // Create the cardholder using Stripe API
            return Cardholder.create(params);
        } catch (Exception e) {
            // Handle exceptions (logging, custom error messages, etc.)
            throw new RuntimeException("Error creating cardholder", e);
        }
    }


    public Cardholder updateCardholder(String cardholderId, String nom, String prenom, String email) {
        // Set the Stripe API key
        Stripe.apiKey = stripeApiKey;

        try {
            // Define parameters for cardholder update
            CardholderUpdateParams params = CardholderUpdateParams.builder()
                    .setIndividual(
                            CardholderUpdateParams.Individual.builder()
                                    .setFirstName(prenom)  // Update first name
                                    .setLastName(nom)  // Update last name
                                    .build()
                    )
                    .setEmail(email)  // Update email
                    .build();

            // Retrieve and update the cardholder using Stripe API
            return Cardholder.retrieve(cardholderId).update(params);
        } catch (Exception e) {
            throw new RuntimeException("Error updating cardholder", e);
        }
    }

}
