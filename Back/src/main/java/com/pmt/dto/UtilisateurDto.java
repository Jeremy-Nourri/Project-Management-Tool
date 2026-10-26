package com.pmt.dto;

import java.time.LocalDateTime;

/**
 * Representation publique d'un utilisateur.
 * Ne contient jamais le mot de passe.
 *
 * @param id identifiant
 * @param nom nom affiche
 * @param email adresse e-mail
 * @param dateCreation date d'inscription
 */
public record UtilisateurDto(
        Long id,
        String nom,
        String email,
        LocalDateTime dateCreation
) {
}
