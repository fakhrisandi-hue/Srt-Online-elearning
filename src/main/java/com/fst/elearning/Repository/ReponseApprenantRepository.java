package com.fst.elearning.repository;

import com.fst.elearning.entity.ReponseApprenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReponseApprenantRepository extends JpaRepository<ReponseApprenant, Long> {

    List<ReponseApprenant> findByApprenantIdAndQuizId(Long apprenantId, Long quizId);

    List<ReponseApprenant> findByApprenantId(Long apprenantId);

    @Query("SELECT COUNT(r) FROM ReponseApprenant r WHERE r.apprenant.id = :apprenantId AND r.quiz.id = :quizId AND r.correcte = true")
    Long countCorrectAnswers(Long apprenantId, Long quizId);

    @Query("SELECT COUNT(r) FROM ReponseApprenant r WHERE r.apprenant.id = :apprenantId AND r.quiz.id = :quizId")
    Long countTotalAnswers(Long apprenantId, Long quizId);
}
