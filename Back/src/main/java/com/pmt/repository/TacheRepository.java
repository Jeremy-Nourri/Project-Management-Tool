package com.pmt.repository;

import com.pmt.entity.StatutTache;
import com.pmt.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TacheRepository extends JpaRepository<Tache, Long> {

    List<Tache> findByProjetId(Long projetId);

    List<Tache> findByProjetIdAndStatut(Long projetId, StatutTache statut);

    List<Tache> findByAssigneId(Long assigneId);
}
