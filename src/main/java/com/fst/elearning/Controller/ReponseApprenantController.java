package com.fst.elearning.controller;

import com.fst.elearning.entity.Question;
import com.fst.elearning.entity.Quiz;
import com.fst.elearning.entity.ReponseApprenant;
import com.fst.elearning.service.QuizService;
import com.fst.elearning.service.ReponseApprenantService;
import com.fst.elearning.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reponses")
@CrossOrigin(origins = "*")
public class ReponseApprenantController {

    private final ReponseApprenantService reponseService;
    private final UserService userService;
    private final QuizService quizService;

    public ReponseApprenantController(ReponseApprenantService reponseService,
            UserService userService,
            QuizService quizService) {
        this.reponseService = reponseService;
        this.userService = userService;
        this.quizService = quizService;
    }

    // ===== SOUMISSION & CORRECTION =====
    @PostMapping("/soumettre")
    public ResponseEntity<?> soumettreReponse(
            @RequestParam Long apprenantId,
            @RequestParam Long quizId,
            @RequestParam Long questionId,
            @RequestParam(required = false) Long choixId) {
        try {
            var apprenant = userService.getUserById(apprenantId)
                    .orElseThrow(() -> new RuntimeException("Apprenant non trouvé"));
            var quiz = quizService.getQuizById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

            // Chercher la question dans la liste des questions du quiz
            var question = quizService.getQuestionsDuQuiz(quizId).stream()
                    .filter(q -> q.getId().equals(questionId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Question non trouvée"));

            ReponseApprenant reponse;
            if (choixId != null && choixId > 0) {
                reponse = reponseService.soumettreReponse(apprenant, quiz, question, choixId);
            } else {
                reponse = reponseService.soumettreReponseNulle(apprenant, quiz, question);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("reponse", reponse);
            result.put("correcte", reponse.getCorrecte());
            result.put("bonneReponse", quizService.getBonneReponse(questionId));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/quiz/{apprenantId}/{quizId}")
    public ResponseEntity<?> getReponsesApprenant(
            @PathVariable Long apprenantId,
            @PathVariable Long quizId) {
        try {
            return ResponseEntity.ok(reponseService.getReponsesApprenant(apprenantId, quizId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/apprenant/{apprenantId}")
    public ResponseEntity<?> getToutesReponsesApprenant(@PathVariable Long apprenantId) {
        try {
            return ResponseEntity.ok(reponseService.getToutesReponsesApprenant(apprenantId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== CALCUL SCORE =====
    @GetMapping("/score/{apprenantId}/{quizId}")
    public ResponseEntity<?> getScoreQuiz(
            @PathVariable Long apprenantId,
            @PathVariable Long quizId) {
        try {
            Integer score = reponseService.calculerScoreQuiz(apprenantId, quizId);
            Double scoreDecimal = reponseService.calculerScoreQuizDecimal(apprenantId, quizId);
            Long bonnes = reponseService.getNbReponsesCorrectes(apprenantId, quizId);
            Long total = reponseService.getNbReponsesTotal(apprenantId, quizId);

            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("scoreDecimal", scoreDecimal);
            result.put("bonnesReponses", bonnes);
            result.put("totalQuestions", total);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/reussi/{apprenantId}/{quizId}")
    public ResponseEntity<?> estReussi(
            @PathVariable Long apprenantId,
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "60") Integer scoreMinimum) {
        try {
            Boolean reussi = reponseService.estReussi(apprenantId, quizId, scoreMinimum);
            Integer score = reponseService.calculerScoreQuiz(apprenantId, quizId);

            Map<String, Object> result = new HashMap<>();
            result.put("reussi", reussi);
            result.put("score", score);
            result.put("scoreMinimum", scoreMinimum);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== RÉSUMÉ DE RÉSULTATS =====
    @GetMapping("/resultat-quiz/{apprenantId}/{quizId}")
    public ResponseEntity<?> getResultatQuiz(
            @PathVariable Long apprenantId,
            @PathVariable Long quizId) {
        try {
            Quiz quiz = quizService.getQuizById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));

            Integer score = reponseService.calculerScoreQuiz(apprenantId, quizId);
            Long bonnes = reponseService.getNbReponsesCorrectes(apprenantId, quizId);
            Long total = reponseService.getNbReponsesTotal(apprenantId, quizId);

            Map<String, Object> result = new HashMap<>();
            result.put("quizId", quizId);
            result.put("quizTitre", quiz.getTitre());
            result.put("score", score);
            result.put("bonnesReponses", bonnes);
            result.put("totalQuestions", total);
            result.put("reussi", score >= 60);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
