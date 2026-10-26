package com.pmt.dto;

import com.pmt.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Invitation d'un membre sur un projet, ou modification de son role.
 *
 * @param email adresse e-mail de l'utilisateur a inviter
 * @param role  role attribue sur le projet
 */
public record MembreRequest(

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "Le format de l'email est invalide")
        String email,

        @NotNull(message = "Le role est obligatoire")
        Role role
) {
}
