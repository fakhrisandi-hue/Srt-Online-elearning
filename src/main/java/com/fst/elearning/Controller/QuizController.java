package com.fst.elearning.controller;

import com.fst.elearning.entity.ChoixQuestion;
import com.fst.elearning.entity.Question;
import com.fst.elearning.entity.Quiz;
import com.fst.elearning.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "*")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // ===== QUIZ =====
    @PostMapping
    public ResponseEntity<?> createQuiz(@RequestBody Quiz quiz) {
        try {
            Quiz savedQuiz = quizService.createQuiz(quiz);
            return ResponseEntity.ok(savedQuiz);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<?> getQuizzesParModule(@PathVariable Long moduleId) {
        try {
            return ResponseEntity.ok(quizService.getQuizzesParModule(moduleId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateQuiz(@PathVariable Long id, @RequestBody Quiz quizDetails) {
        try {
            Quiz updated = quizService.updateQuiz(id, quizDetails);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(Map.of("message", "Quiz supprimé avec succès"));
    }

    // ===== QUESTIONS =====
    @PostMapping("/{quizId}/questions")
    public ResponseEntity<?> createQuestion(
            @PathVariable Long quizId,
            @RequestBody Question question) {
        try {
            var quiz = quizService.getQuizById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz non trouvé"));
            question.setQuiz(quiz);
            Question savedQuestion = quizService.createQuestion(question);
            return ResponseEntity.ok(savedQuestion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<?> getQuestionsDuQuiz(@PathVariable Long quizId) {
        try {
            return ResponseEntity.ok(quizService.getQuestionsDuQuiz(quizId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/questions/{questionId}")
    public ResponseEntity<?> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody Question questionDetails) {
        try {
            Question updated = quizService.updateQuestion(questionId, questionDetails);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/questions/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long questionId) {
        quizService.deleteQuestion(questionId);
        return ResponseEntity.ok(Map.of("message", "Question supprimée avec succès"));
    }

    // ===== CHOIX =====
    @PostMapping("/questions/{questionId}/choix")
    public ResponseEntity<?> createChoix(
            @PathVariable Long questionId,
            @RequestBody ChoixQuestion choix) {
        try {
            var question = quizService.getQuestionsDuQuiz(questionId).stream()
                    .filter(q -> q.getId().equals(questionId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Question non trouvée"));
            choix.setQuestion(question);
            ChoixQuestion savedChoix = quizService.createChoix(choix);
            return ResponseEntity.ok(savedChoix);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/questions/{questionId}/choix")
    public ResponseEntity<?> getChoixDeLaQuestion(@PathVariable Long questionId) {
        try {
            return ResponseEntity.ok(quizService.getChoixDeLaQuestion(questionId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/choix/{choixId}")
    public ResponseEntity<?> updateChoix(
            @PathVariable Long choixId,
            @RequestBody ChoixQuestion choixDetails) {
        try {
            ChoixQuestion updated = quizService.updateChoix(choixId, choixDetails);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/choix/{choixId}")
    public ResponseEntity<?> deleteChoix(@PathVariable Long choixId) {
        quizService.deleteChoix(choixId);
        return ResponseEntity.ok(Map.of("message", "Choix supprimé avec succès"));
    }

    // ===== LOGIQUE DE QUIZ =====
    @GetMapping("/{quizId}/total-questions")
    public ResponseEntity<?> getTotalQuestions(@PathVariable Long quizId) {
        try {
            Integer total = quizService.getTotalQuestions(quizId);
            return ResponseEntity.ok(Map.of("total", total));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/questions/{questionId}/bonne-reponse")
    public ResponseEntity<?> getBonneReponse(@PathVariable Long questionId) {
        try {
            ChoixQuestion bonneReponse = quizService.getBonneReponse(questionId);
            if (bonneReponse == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(bonneReponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/verifier-reponse")
    public ResponseEntity<?> verifierReponse(
            @RequestParam Long questionId,
            @RequestParam Long choixId) {
        try {
            Boolean correcte = quizService.verifierReponse(questionId, choixId);
            return ResponseEntity.ok(Map.of("correcte", correcte));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
