package com.fst.elearning.repository;

import com.fst.elearning.entity.Inscription;
import com.fst.elearning.entity.StatutInscriptionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, Long> {

    Optional<Inscription> findByApprenantIdAndCoursId(Long apprenantId, Long coursId);

    List<Inscription> findByApprenantId(Long apprenantId);

    List<Inscription> findByCoursId(Long coursId);

    List<Inscription> findByCoursIdAndStatut(Long coursId, StatutInscriptionEnum statut);

    Long countByCoursId(Long coursId);

    Long countByCoursIdAndStatut(Long coursId, StatutInscriptionEnum statut);
}
