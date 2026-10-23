package com.pmt.repository;

import com.pmt.entity.ProjetMembre;
import com.pmt.entity.ProjetMembreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjetMembreRepository extends JpaRepository<ProjetMembre, ProjetMembreId> {

    List<ProjetMembre> findByIdProjetId(Long projetId);

    Optional<ProjetMembre> findByIdProjetIdAndIdUtilisateurId(Long projetId, Long utilisateurId);

    boolean existsByIdProjetIdAndIdUtilisateurId(Long projetId, Long utilisateurId);
}
