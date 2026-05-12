package com.fst.elearning.controller;

import com.fst.elearning.service.InscriptionService;
import com.fst.elearning.service.ProgressionService;
import com.fst.elearning.service.ReponseApprenantService;
import com.fst.elearning.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/apprenant")
@CrossOrigin(origins = "*")
public class ApprenantController {

    private final UserService userService;
    private final InscriptionService inscriptionService;
    private final ProgressionService progressionService;
    private final ReponseApprenantService reponseService;

    public ApprenantController(UserService userService,
            InscriptionService inscriptionService,
            ProgressionService progressionService,
            ReponseApprenantService reponseService) {
        this.userService = userService;
        this.inscriptionService = inscriptionService;
        this.progressionService = progressionService;
        this.reponseService = reponseService;
    }

    // ===== MES COURS =====
    @GetMapping("/{apprenantId}/mes-cours")
    public ResponseEntity<?> getMesCours(@PathVariable Long apprenantId) {
        try {
            var inscriptions = inscriptionService.getMesCours(apprenantId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (var inscription : inscriptions) {
                var cours = inscription.getCours();
                Double progression = progressionService.getProgressionPourcentageCours(apprenantId, cours.getId());

                Map<String, Object> courseData = new HashMap<>();
                courseData.put("inscriptionId", inscription.getId());
                courseData.put("coursId", cours.getId());
                courseData.put("titre", cours.getTitre());
                courseData.put("description", cours.getDescription());
                courseData.put("imageUrl", cours.getImageUrl());
                courseData.put("formateur", cours.getFormateur().getName());
                courseData.put("niveau", cours.getNiveau());
                courseData.put("progression", progression);
                courseData.put("statut", inscription.getStatut());
                courseData.put("dateInscription", inscription.getDateInscription());

                result.add(courseData);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== MA PROGRESSION =====
    @GetMapping("/{apprenantId}/ma-progression/{coursId}")
    public ResponseEntity<?> maProgressionCours(
            @PathVariable Long apprenantId,
            @PathVariable Long coursId) {
        try {
            Double pourcentage = progressionService.getProgressionPourcentageCours(apprenantId, coursId);
            Long nbCompletees = progressionService.getNbLeconsCoursesPourCours(apprenantId, coursId);
            Long nbTotal = progressionService.getNbTotalLeconsPourCours(coursId);

            Map<String, Object> result = new HashMap<>();
            result.put("pourcentage", pourcentage);
            result.put("nbCompletees", nbCompletees);
            result.put("nbTotal", nbTotal);
            result.put("apprenantId", apprenantId);
            result.put("coursId", coursId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== MES RÉSULTATS DE QUIZ =====
    @GetMapping("/{apprenantId}/mes-resultats")
    public ResponseEntity<?> mesResultats(@PathVariable Long apprenantId) {
        try {
            var toutesReponses = reponseService.getToutesReponsesApprenant(apprenantId);
            Map<Long, Map<String, Object>> resultatsParQuiz = new HashMap<>();

            for (var reponse : toutesReponses) {
                Long quizId = reponse.getQuiz().getId();

                if (!resultatsParQuiz.containsKey(quizId)) {
                    var quiz = reponse.getQuiz();
                    Integer score = reponseService.calculerScoreQuiz(apprenantId, quizId);
                    Long bonnes = reponseService.getNbReponsesCorrectes(apprenantId, quizId);
                    Long total = reponseService.getNbReponsesTotal(apprenantId, quizId);

                    Map<String, Object> quizResult = new HashMap<>();
                    quizResult.put("quizId", quizId);
                    quizResult.put("quizTitre", quiz.getTitre());
                    quizResult.put("moduleTitre", quiz.getModule().getTitre());
                    quizResult.put("score", score);
                    quizResult.put("bonnesReponses", bonnes);
                    quizResult.put("totalQuestions", total);
                    quizResult.put("reussi", score >= 60);
                    quizResult.put("derniereAttempt", reponse.getDateReponse());

                    resultatsParQuiz.put(quizId, quizResult);
                }
            }

            return ResponseEntity.ok(new ArrayList<>(resultatsParQuiz.values()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/{apprenantId}/resultats/{quizId}")
    public ResponseEntity<?> getResultatQuiz(
            @PathVariable Long apprenantId,
            @PathVariable Long quizId) {
        try {
            Integer score = reponseService.calculerScoreQuiz(apprenantId, quizId);
            Long bonnes = reponseService.getNbReponsesCorrectes(apprenantId, quizId);
            Long total = reponseService.getNbReponsesTotal(apprenantId, quizId);

            Map<String, Object> result = new HashMap<>();
            result.put("score", score);
            result.put("bonnesReponses", bonnes);
            result.put("totalQuestions", total);
            result.put("reussi", score >= 60);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== TABLEAU DE BORD =====
    @GetMapping("/{apprenantId}/tableau-bord")
    public ResponseEntity<?> getTableauBord(@PathVariable Long apprenantId) {
        try {
            var apprenant = userService.getUserById(apprenantId)
                    .orElseThrow(() -> new RuntimeException("Apprenant non trouvé"));
            var mesCours = inscriptionService.getMesCours(apprenantId);
            var mesResultats = reponseService.getToutesReponsesApprenant(apprenantId);

            // Statistiques globales
            long coursEnCours = mesCours.stream()
                    .filter(i -> i.getStatut().toString().equals("EN_COURS"))
                    .count();
            long coursTermines = mesCours.stream()
                    .filter(i -> i.getStatut().toString().equals("TERMINE"))
                    .count();

            Double progressionMoyenne = mesCours.stream()
                    .mapToDouble(
                            i -> progressionService.getProgressionPourcentageCours(apprenantId, i.getCours().getId()))
                    .average()
                    .orElse(0.0);

            Long quizReussis = mesResultats.stream()
                    .map(r -> r.getQuiz().getId())
                    .distinct()
                    .filter(quizId -> reponseService.calculerScoreQuiz(apprenantId, quizId) >= 60)
                    .count();

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("nomApprenant", apprenant.getName());
            dashboard.put("email", apprenant.getEmail());
            dashboard.put("totalCours", mesCours.size());
            dashboard.put("coursEnCours", coursEnCours);
            dashboard.put("coursTermines", coursTermines);
            dashboard.put("progressionMoyenne", progressionMoyenne);
            dashboard.put("totalQuiz", mesResultats.stream().map(r -> r.getQuiz().getId()).distinct().count());
            dashboard.put("quizReussis", quizReussis);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
