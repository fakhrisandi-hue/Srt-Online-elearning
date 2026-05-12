package com.fst.elearning.service;

import com.fst.elearning.repository.InscriptionRepository;
import com.fst.elearning.repository.ProgressionLeconRepository;
import com.fst.elearning.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class InscriptionService {

    private final InscriptionRepository inscriptionRepository;
    private final ProgressionLeconRepository progressionRepository;

    public InscriptionService(InscriptionRepository inscriptionRepository,
            ProgressionLeconRepository progressionRepository) {
        this.inscriptionRepository = inscriptionRepository;
        this.progressionRepository = progressionRepository;
    }

    // ===== INSCRIPTION =====
    public Inscription inscrireApprenant(User apprenant, Cours cours) {
        Optional<Inscription> existante = inscriptionRepository.findByApprenantIdAndCoursId(apprenant.getId(),
                cours.getId());
        if (existante.isPresent()) {
            throw new RuntimeException("L'apprenant est déjà inscrit à ce cours");
        }

        Inscription inscription = new Inscription(apprenant, cours);
        inscription.setDateInscription(LocalDate.now());
        inscription.setStatut(StatutInscriptionEnum.EN_COURS);
        return inscriptionRepository.save(inscription);
    }

    public Optional<Inscription> getInscription(Long apprenantId, Long coursId) {
        return inscriptionRepository.findByApprenantIdAndCoursId(apprenantId, coursId);
    }

    public List<Inscription> getMesCours(Long apprenantId) {
        return inscriptionRepository.findByApprenantId(apprenantId);
    }

    public List<Inscription> getApprenantsDuCours(Long coursId) {
        return inscriptionRepository.findByCoursId(coursId);
    }

    public void updateStatutInscription(Long inscriptionId, StatutInscriptionEnum statut) {
        inscriptionRepository.findById(inscriptionId).ifPresent(inscription -> {
            inscription.setStatut(statut);
            inscriptionRepository.save(inscription);
        });
    }

    public void retirerInscription(Long inscriptionId) {
        inscriptionRepository.deleteById(inscriptionId);
    }

    // ===== STATISTIQUES =====
    public Long getNbInscrits(Long coursId) {
        return inscriptionRepository.countByCoursId(coursId);
    }

    public Long getNbTermines(Long coursId) {
        return inscriptionRepository.countByCoursIdAndStatut(coursId, StatutInscriptionEnum.TERMINE);
    }

    public Long getNbEnCours(Long coursId) {
        return inscriptionRepository.countByCoursIdAndStatut(coursId, StatutInscriptionEnum.EN_COURS);
    }

    public Long getNbAbandonnes(Long coursId) {
        return inscriptionRepository.countByCoursIdAndStatut(coursId, StatutInscriptionEnum.ABANDONNE);
    }
}
