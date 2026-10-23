package com.pmt.repository;

import com.pmt.entity.HistoriqueTache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueTacheRepository extends JpaRepository<HistoriqueTache, Long> {

    List<HistoriqueTache> findByTacheIdOrderByDateModifDesc(Long tacheId);
}
