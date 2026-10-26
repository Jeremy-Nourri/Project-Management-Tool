package com.pmt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Identifiants de connexion.
 * @param email adresse e-mail du compte
 * @param motDePasse mot de passe en clair
 */
public record ConnexionRequest(

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String motDePasse
) {
}
