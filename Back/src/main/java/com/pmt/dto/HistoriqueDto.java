package com.pmt.dto;

import java.time.LocalDateTime;

/**
 * Une modification apportée a une tache.
 * @param id identifiant
 * @param auteur utilisateur à l'origine de la modification
 * @param nomChamp champ modifie
 * @param ancienneValeur valeur avant modification, null si le champ était vide
 * @param nouvelleValeur valeur apres modification
 * @param dateModif horodatage
 */
public record HistoriqueDto(
        Long id,
        UtilisateurDto auteur,
        String nomChamp,
        String ancienneValeur,
        String nouvelleValeur,
        LocalDateTime dateModif
) {
}
