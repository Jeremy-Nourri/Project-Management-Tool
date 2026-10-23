package com.pmt.repository;

import com.pmt.entity.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    @Query("""
            SELECT pm.projet FROM ProjetMembre pm
            WHERE pm.utilisateur.id = :utilisateurId
            ORDER BY pm.projet.dateDebut DESC
            """)
    List<Projet> findProjetsDeLUtilisateur(@Param("utilisateurId") Long utilisateurId);
}
