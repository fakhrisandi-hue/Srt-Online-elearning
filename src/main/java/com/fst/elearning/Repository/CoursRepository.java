package com.fst.elearning.repository;

import com.fst.elearning.entity.Cours;
import com.fst.elearning.entity.NiveauEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CoursRepository extends JpaRepository<Cours, Long> {

    Page<Cours> findByActifTrue(Pageable pageable);

    Page<Cours> findByTitreContainingIgnoreCaseAndActifTrue(String titre, Pageable pageable);

    Page<Cours> findByCategorieAndActifTrue(String categorie, Pageable pageable);

    Page<Cours> findByNiveauAndActifTrue(NiveauEnum niveau, Pageable pageable);

    @Query("SELECT c FROM Cours c WHERE c.actif = true " +
            "AND (LOWER(c.titre) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(c.categorie) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Cours> searchCours(String searchTerm, Pageable pageable);

    @Query("SELECT c FROM Cours c WHERE c.actif = true " +
            "AND LOWER(c.titre) LIKE LOWER(CONCAT('%', :titre, '%')) " +
            "AND (:categorie IS NULL OR c.categorie = :categorie) " +
            "AND (:niveau IS NULL OR c.niveau = :niveau)")
    Page<Cours> searchCoursFiltered(String titre, String categorie, NiveauEnum niveau, Pageable pageable);

    List<Cours> findByFormateurIdAndActifTrue(Long formateurId);
}
