package com.fst.elearning.service;

import com.fst.elearning.repository.ChoixQuestionRepository;
import com.fst.elearning.repository.ReponseApprenantRepository;
import com.fst.elearning.entity.ChoixQuestion;
import com.fst.elearning.entity.Question;
import com.fst.elearning.entity.Quiz;
import com.fst.elearning.entity.ReponseApprenant;
import com.fst.elearning.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReponseApprenantService {

    private final ReponseApprenantRepository reponseRepository;
    private final ChoixQuestionRepository choixRepository;

    public ReponseApprenantService(ReponseApprenantRepository reponseRepository,
            ChoixQuestionRepository choixRepository) {
        this.reponseRepository = reponseRepository;
        this.choixRepository = choixRepository;
    }

    // ===== SOUMISSION ET CORRECTION =====
    public ReponseApprenant soumettreReponse(User apprenant, Quiz quiz, Question question, Long choixId) {
        ChoixQuestion choix = choixRepository.findById(choixId)
                .orElseThrow(() -> new RuntimeException("Choix non trouvé"));

        ReponseApprenant reponse = new ReponseApprenant(apprenant, quiz, question, choix);
        reponse.setDateReponse(LocalDateTime.now());

        // Correction automatique côté serveur
        reponse.setCorrecte(choix.getCorrect());

        return reponseRepository.save(reponse);
    }

    public ReponseApprenant soumettreReponseNulle(User apprenant, Quiz quiz, Question question) {
        ReponseApprenant reponse = new ReponseApprenant(apprenant, quiz, question, null);
        reponse.setDateReponse(LocalDateTime.now());
        reponse.setCorrecte(false);

        return reponseRepository.save(reponse);
    }

    // ===== RÉCUPÉRATION =====
    public List<ReponseApprenant> getReponsesApprenant(Long apprenantId, Long quizId) {
        return reponseRepository.findByApprenantIdAndQuizId(apprenantId, quizId);
    }

    public List<ReponseApprenant> getToutesReponsesApprenant(Long apprenantId) {
        return reponseRepository.findByApprenantId(apprenantId);
    }

    // ===== CALCUL SCORE =====
    public Integer calculerScoreQuiz(Long apprenantId, Long quizId) {
        Long bonnesReponses = reponseRepository.countCorrectAnswers(apprenantId, quizId);
        Long totalReponses = reponseRepository.countTotalAnswers(apprenantId, quizId);

        if (totalReponses == 0) {
            return 0;
        }

        return Math.toIntExact((bonnesReponses * 100) / totalReponses);
    }

    public Double calculerScoreQuizDecimal(Long apprenantId, Long quizId) {
        Long bonnesReponses = reponseRepository.countCorrectAnswers(apprenantId, quizId);
        Long totalReponses = reponseRepository.countTotalAnswers(apprenantId, quizId);

        if (totalReponses == 0) {
            return 0.0;
        }

        return (bonnesReponses * 100.0) / totalReponses;
    }

    public Long getNbReponsesCorrectes(Long apprenantId, Long quizId) {
        return reponseRepository.countCorrectAnswers(apprenantId, quizId);
    }

    public Long getNbReponsesTotal(Long apprenantId, Long quizId) {
        return reponseRepository.countTotalAnswers(apprenantId, quizId);
    }

    // ===== RÉSULTATS =====
    public Boolean estReussi(Long apprenantId, Long quizId, Integer scoreMinimum) {
        Integer score = calculerScoreQuiz(apprenantId, quizId);
        return score >= scoreMinimum;
    }
}
