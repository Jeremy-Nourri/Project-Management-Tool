package com.pmt.dto;

import java.time.LocalDate;

/**
 * Representation publique d'un projet.
 *
 * @param id identifiant
 * @param nom nom du projet
 * @param description description
 * @param dateDebut date de démarrage
 * @param createur utilisateur ayant créé le projet
 */
public record ProjetDto(
        Long id,
        String nom,
        String description,
        LocalDate dateDebut,
        UtilisateurDto createur
) {
}
