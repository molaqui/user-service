package com.bankati.userservice.Models;

import lombok.*;

import java.math.BigDecimal;


@Setter  @Getter @ToString @NoArgsConstructor @AllArgsConstructor
public class Compte {

    private Long userId; // ID de l'utilisateur associé au compte
    private BigDecimal solde;


}
