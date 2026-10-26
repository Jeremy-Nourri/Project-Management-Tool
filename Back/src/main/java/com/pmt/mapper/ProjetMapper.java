package com.pmt.mapper;

import com.pmt.dto.MembreDto;
import com.pmt.dto.ProjetDto;
import com.pmt.entity.Projet;
import com.pmt.entity.ProjetMembre;
import org.springframework.stereotype.Component;

@Component
public class ProjetMapper {

    private final UtilisateurMapper utilisateurMapper;

    public ProjetMapper(UtilisateurMapper utilisateurMapper) {
        this.utilisateurMapper = utilisateurMapper;
    }

    /** Convertit un projet en DTO, createur inclus. */
    public ProjetDto versDto(Projet projet) {
        return new ProjetDto(
                projet.getId(),
                projet.getNom(),
                projet.getDescription(),
                projet.getDateDebut(),
                utilisateurMapper.versDto(projet.getCreateur()));
    }

    /** Convertit une ligne projet_membre en DTO. */
    public MembreDto versMembreDto(ProjetMembre membre) {
        return new MembreDto(
                utilisateurMapper.versDto(membre.getUtilisateur()),
                membre.getRole());
    }
}
