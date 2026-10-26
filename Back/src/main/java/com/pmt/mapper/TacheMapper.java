package com.pmt.mapper;

import com.pmt.dto.TacheDto;
import com.pmt.entity.Tache;
import org.springframework.stereotype.Component;

@Component
public class TacheMapper {

    private final UtilisateurMapper utilisateurMapper;

    public TacheMapper(UtilisateurMapper utilisateurMapper) {
        this.utilisateurMapper = utilisateurMapper;
    }

    public TacheDto versDto(Tache tache) {
        return new TacheDto(
                tache.getId(),
                tache.getNom(),
                tache.getDescription(),
                tache.getDateEcheance(),
                tache.getDateFin(),
                tache.getPriorite(),
                tache.getStatut(),
                tache.getProjet().getId(),
                utilisateurMapper.versDto(tache.getCreateur()),
                tache.getAssigne() == null ? null : utilisateurMapper.versDto(tache.getAssigne()),
                tache.getDateCreation());
    }
}
