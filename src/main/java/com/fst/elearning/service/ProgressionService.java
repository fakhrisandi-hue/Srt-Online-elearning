package com.fst.elearning.service;

import com.fst.elearning.repository.LeconRepository;
import com.fst.elearning.repository.ModuleRepository;
import com.fst.elearning.repository.ProgressionLeconRepository;
import com.fst.elearning.entity.Lecon;
import com.fst.elearning.entity.Module;
import com.fst.elearning.entity.ProgressionLecon;
import com.fst.elearning.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProgressionService {

    private final ProgressionLeconRepository progressionRepository;
    private final LeconRepository leconRepository;
    private final ModuleRepository moduleRepository;

    public ProgressionService(ProgressionLeconRepository progressionRepository,
            LeconRepository leconRepository,
            ModuleRepository moduleRepository) {
        this.progressionRepository = progressionRepository;
        this.leconRepository = leconRepository;
        this.moduleRepository = moduleRepository;
    }

    // ===== PROGRESSION PAR LEÇON =====
    public ProgressionLecon marquerLeconCompletee(User apprenant, Lecon lecon) {
        Optional<ProgressionLecon> existante = progressionRepository.findByApprenantIdAndLeconId(apprenant.getId(),
                lecon.getId());

        ProgressionLecon progression;
        if (existante.isPresent()) {
            progression = existante.get();
            if (!progression.getCompletee()) {
                progression.setCompletee(true);
                progression.setDateCompletion(LocalDateTime.now());
            }
        } else {
            progression = new ProgressionLecon(apprenant, lecon);
            progression.setCompletee(true);
            progression.setDateCompletion(LocalDateTime.now());
        }

        return progressionRepository.save(progression);
    }

    public ProgressionLecon marquerLeconNonCompletee(Long apprenantId, Long leconId) {
        return progressionRepository.findByApprenantIdAndLeconId(apprenantId, leconId).map(progression -> {
            progression.setCompletee(false);
            progression.setDateCompletion(null);
            return progressionRepository.save(progression);
        }).orElseThrow(() -> new RuntimeException("Progression non trouvée"));
    }

    public Optional<ProgressionLecon> getProgressionLecon(Long apprenantId, Long leconId) {
        return progressionRepository.findByApprenantIdAndLeconId(apprenantId, leconId);
    }

    // ===== PROGRESSION GLOBALE =====
    public Double getProgressionPourcentageCours(Long apprenantId, Long coursId) {
        List<ProgressionLecon> progressions = progressionRepository.findByApprenantIdAndLeconModuleCoursId(apprenantId,
                coursId);

        if (progressions.isEmpty()) {
            return 0.0;
        }

        Long completed = progressions.stream().filter(ProgressionLecon::getCompletee).count();
        return (completed * 100.0) / progressions.size();
    }

    public Double getProgressionPourcentageModule(Long apprenantId, Long moduleId) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new RuntimeException("Module non trouvé"));

        List<Lecon> lecons = leconRepository.findByModuleIdOrderByOrdre(moduleId);

        if (lecons.isEmpty()) {
            return 100.0;
        }

        long completees = 0;
        for (Lecon lecon : lecons) {
            Optional<ProgressionLecon> progression = progressionRepository.findByApprenantIdAndLeconId(apprenantId,
                    lecon.getId());
            if (progression.isPresent() && progression.get().getCompletee()) {
                completees++;
            }
        }

        return (completees * 100.0) / lecons.size();
    }

    public List<ProgressionLecon> getProgressionApprenant(Long apprenantId) {
        return progressionRepository.findByApprenantId(apprenantId);
    }

    public Long getNbLeconsCoursesPourCours(Long apprenantId, Long coursId) {
        return progressionRepository.countByApprenantIdAndLeconModuleCoursIdAndCompleteeTrue(apprenantId, coursId);
    }

    public Long getNbTotalLeconsPourCours(Long coursId) {
        List<Module> modules = moduleRepository.findByCoursIdOrderByOrdre(coursId);
        return modules.stream()
                .mapToLong(m -> leconRepository.findByModuleIdOrderByOrdre(m.getId()).size())
                .sum();
    }
}
