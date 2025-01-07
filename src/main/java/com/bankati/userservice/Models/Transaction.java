package com.bankati.userservice.Models;


import lombok.*;


import java.math.BigDecimal;
import java.util.Date;
@Setter @Getter @ToString  @AllArgsConstructor @NoArgsConstructor
public class Transaction {


    private Long id;
    private BigDecimal montant;
    private String type; // "paiement", "reception", "transfert"
    private String description;
    private Date date;
    private String statut; // "effectuée", "échouée"

    private Long sourceUserId;       // ID de l'expéditeur
    private Long destinationUserId;  // ID du destinataire




}
