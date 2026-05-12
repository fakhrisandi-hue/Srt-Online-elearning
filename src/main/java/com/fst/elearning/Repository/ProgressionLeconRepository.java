package com.fst.elearning.repository;

import com.fst.elearning.entity.ProgressionLecon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressionLeconRepository extends JpaRepository<ProgressionLecon, Long> {

    Optional<ProgressionLecon> findByApprenantIdAndLeconId(Long apprenantId, Long leconId);

    List<ProgressionLecon> findByApprenantId(Long apprenantId);

    List<ProgressionLecon> findByApprenantIdAndLeconModuleCoursId(Long apprenantId, Long coursId);

    Long countByApprenantIdAndCompleteeTrue(Long apprenantId);

    Long countByApprenantIdAndLeconModuleCoursIdAndCompleteeTrue(Long apprenantId, Long coursId);
}
