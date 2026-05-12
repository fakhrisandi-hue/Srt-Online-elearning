package com.fst.elearning.repository;

import com.fst.elearning.entity.ChoixQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChoixQuestionRepository extends JpaRepository<ChoixQuestion, Long> {
    List<ChoixQuestion> findByQuestionIdOrderByOrdre(Long questionId);
}
