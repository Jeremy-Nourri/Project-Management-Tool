package com.pmt.dto;

import com.pmt.entity.PrioriteTache;
import com.pmt.entity.StatutTache;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representation publique d'une tache.
 *
 * @param id identifiant
 * @param nom intitule
 * @param description description
 * @param dateEcheance date limite
 * @param dateFin date de realisation
 * @param priorite niveau de priorité
 * @param statut avancement
 * @param projetId projet auquel la tache appartient
 * @param createur auteur de la tache
 * @param assigne membre en charge, null si non assignee
 * @param dateCreation date de creation
 */
public record TacheDto(
        Long id,
        String nom,
        String description,
        LocalDate dateEcheance,
        LocalDate dateFin,
        PrioriteTache priorite,
        StatutTache statut,
        Long projetId,
        UtilisateurDto createur,
        UtilisateurDto assigne,
        LocalDateTime dateCreation
) {
}
