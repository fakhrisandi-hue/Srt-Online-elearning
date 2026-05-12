package com.fst.elearning.repository;

import com.fst.elearning.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    List<Module> findByCoursIdOrderByOrdre(Long coursId);
}
