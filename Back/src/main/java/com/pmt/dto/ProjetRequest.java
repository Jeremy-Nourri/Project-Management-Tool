package com.pmt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Données de création ou de mise a jour d'un projet.
 *
 * @param nom nom du projet, 2 à 100 caractères
 * @param description description libre, facultative
 * @param dateDebut date de démarrage du projet
 */
public record ProjetRequest(

        @NotBlank(message = "Le nom du projet est obligatoire")
        @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caracteres")
        String nom,

        String description,

        @NotNull(message = "La date de debut est obligatoire")
        LocalDate dateDebut
) {
}
