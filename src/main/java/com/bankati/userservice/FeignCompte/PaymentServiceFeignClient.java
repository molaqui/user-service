package com.bankati.userservice.FeignCompte;

import com.bankati.userservice.Models.Compte;
import com.bankati.userservice.Models.Transaction;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "payment-service", url = "http://localhost:8085")
public interface PaymentServiceFeignClient {

    // Méthodes pour le contrôleur de paiements
    @GetMapping("/paiements/user/{userId}")
    List<Transaction> listerTransactionsParUserId(@PathVariable("userId") Long userId);

    @PostMapping("/paiements/{compteId}/payer-facture/{creancierId}")
    ResponseEntity<Transaction> payerFacture(@PathVariable("compteId") Long compteId,
                                             @PathVariable("creancierId") Long creancierId,
                                             @RequestParam BigDecimal montant);

    // Méthodes pour le contrôleur des comptes
    @PostMapping("/comptes/creer")
    ResponseEntity<Compte> creerCompte(@RequestParam Long userId, @RequestParam BigDecimal soldeInitial);

    @GetMapping("/comptes/{compteId}/solde")
    ResponseEntity<BigDecimal> consulterSolde(@PathVariable("compteId") Long compteId);

    @PostMapping("/paiements/user/{userId}/ajouter-solde")
    ResponseEntity<BigDecimal> ajouterMontantAuSolde(@PathVariable("userId") Long userId,
                                                     @RequestParam BigDecimal montant);
  

}

