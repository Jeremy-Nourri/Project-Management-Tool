package com.pmt.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Donnees d'inscription d'un nouvel utilisateur.
 *
 * @param nom nom d'utilisateur affiche, 2 a 50 caracteres
 * @param email adresse e-mail, unique sur la plateforme
 * @param motDePasse mot de passe en clair, 8 caracteres minimum
 */
public record InscriptionRequest(

        @NotBlank(message = "Le nom est obligatoire")
        @Size(min = 2, max = 50, message = "Le nom doit contenir entre 2 et 50 caracteres")
        String nom,

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        @Size(max = 255)
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caracteres")
        String motDePasse
) {
}
