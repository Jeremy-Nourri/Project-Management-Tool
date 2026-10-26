package com.pmt.dto;

import com.pmt.entity.PrioriteTache;
import com.pmt.entity.StatutTache;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Donnees de création ou de mise à jour d'une tache.
 * Les champs facultatifs laisses a null ne sont pas modifiés lors d'une mise à jour.
 *
 * @param nom intitule de la tache
 * @param description description libre
 * @param dateEcheance date limite prévue
 * @param dateFin date de realisation effective
 * @param priorite niveau de priorité
 * @param statut avancement de la tâche
 * @param assigneEmail email du membre à qui la tâche est confiée
 */
public record TacheRequest(

        @NotBlank(message = "Le nom de la tache est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caracteres")
        String nom,

        String description,

        LocalDate dateEcheance,

        LocalDate dateFin,

        PrioriteTache priorite,

        StatutTache statut,

        String assigneEmail
) {
}
