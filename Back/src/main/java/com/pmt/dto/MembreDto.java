package com.pmt.dto;

import com.pmt.entity.Role;

/**
 * Membre d'un projet et son role.
 *
 * @param utilisateur utilisateur membre du projet
 * @param role role détenu sur ce projet
 */
public record MembreDto(
        UtilisateurDto utilisateur,
        Role role
) {
}
