package com.fst.elearning.repository;

import com.fst.elearning.entity.Lecon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LeconRepository extends JpaRepository<Lecon, Long> {
    List<Lecon> findByModuleIdOrderByOrdre(Long moduleId);
}
