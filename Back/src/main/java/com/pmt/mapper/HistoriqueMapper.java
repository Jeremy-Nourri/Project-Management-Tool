package com.pmt.mapper;

import com.pmt.dto.HistoriqueDto;
import com.pmt.entity.HistoriqueTache;
import org.springframework.stereotype.Component;

@Component
public class HistoriqueMapper {

    private final UtilisateurMapper utilisateurMapper;

    public HistoriqueMapper(UtilisateurMapper utilisateurMapper) {
        this.utilisateurMapper = utilisateurMapper;
    }

    /** Convertit une ligne d'historique en DTO. */
    public HistoriqueDto versDto(HistoriqueTache historique) {
        return new HistoriqueDto(
                historique.getId(),
                utilisateurMapper.versDto(historique.getAuteur()),
                historique.getNomChamp(),
                historique.getAncienneValeur(),
                historique.getNouvelleValeur(),
                historique.getDateModif());
    }
}
