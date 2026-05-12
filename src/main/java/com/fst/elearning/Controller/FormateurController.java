package com.fst.elearning.controller;

import com.fst.elearning.entity.RoleEnum;
import com.fst.elearning.service.CoursService;
import com.fst.elearning.service.InscriptionService;
import com.fst.elearning.service.ProgressionService;
import com.fst.elearning.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/formateur")
@CrossOrigin(origins = "*")
public class FormateurController {

    private final CoursService coursService;
    private final UserService userService;
    private final InscriptionService inscriptionService;
    private final ProgressionService progressionService;

    public FormateurController(CoursService coursService,
            UserService userService,
            InscriptionService inscriptionService,
            ProgressionService progressionService) {
        this.coursService = coursService;
        this.userService = userService;
        this.inscriptionService = inscriptionService;
        this.progressionService = progressionService;
    }

    // ===== MES COURS =====
    @GetMapping("/{formateurId}/mes-cours")
    public ResponseEntity<?> mesCours(@PathVariable Long formateurId) {
        try {
            var formateur = userService.getUserById(formateurId)
                    .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

            if (!formateur.getRole().equals(RoleEnum.FORMATEUR) && !formateur.getRole().equals(RoleEnum.ADMIN)) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Accès refusé: vous n'êtes pas formateur"));
            }

            return ResponseEntity.ok(coursService.getCoursParFormateur(formateurId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== APPRENANTS DU COURS =====
    @GetMapping("/{formateurId}/cours/{coursId}/apprenants")
    public ResponseEntity<?> getApprenantsDuCours(
            @PathVariable Long formateurId,
            @PathVariable Long coursId) {
        try {
            var cours = coursService.getCoursById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

            if (!cours.getFormateur().getId().equals(formateurId)) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Vous n'êtes pas le formateur de ce cours"));
            }

            var inscriptions = inscriptionService.getApprenantsDuCours(coursId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (var inscription : inscriptions) {
                var apprenant = inscription.getApprenant();
                Double progression = progressionService.getProgressionPourcentageCours(apprenant.getId(), coursId);

                Map<String, Object> apprenantData = new HashMap<>();
                apprenantData.put("inscriptionId", inscription.getId());
                apprenantData.put("apprenantId", apprenant.getId());
                apprenantData.put("nom", apprenant.getName());
                apprenantData.put("email", apprenant.getEmail());
                apprenantData.put("statut", inscription.getStatut());
                apprenantData.put("dateInscription", inscription.getDateInscription());
                apprenantData.put("progression", progression);

                result.add(apprenantData);
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== STATISTIQUES PROGRESSION APPRENANTS =====
    @GetMapping("/{formateurId}/cours/{coursId}/statistiques")
    public ResponseEntity<?> getStatistiquesProgression(
            @PathVariable Long formateurId,
            @PathVariable Long coursId) {
        try {
            var cours = coursService.getCoursById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

            if (!cours.getFormateur().getId().equals(formateurId)) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Vous n'êtes pas le formateur de ce cours"));
            }

            var inscriptions = inscriptionService.getApprenantsDuCours(coursId);

            List<Double> progressions = new ArrayList<>();
            for (var inscription : inscriptions) {
                Double prog = progressionService.getProgressionPourcentageCours(
                        inscription.getApprenant().getId(), coursId);
                progressions.add(prog);
            }

            Double progressionMoyenne = progressions.isEmpty() ? 0.0
                    : progressions.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

            Double progressionMin = progressions.isEmpty() ? 0.0
                    : progressions.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);

            Double progressionMax = progressions.isEmpty() ? 0.0
                    : progressions.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalInscrits", inscriptionService.getNbInscrits(coursId));
            stats.put("enCours", inscriptionService.getNbEnCours(coursId));
            stats.put("termines", inscriptionService.getNbTermines(coursId));
            stats.put("abandonnes", inscriptionService.getNbAbandonnes(coursId));
            stats.put("progressionMoyenne", progressionMoyenne);
            stats.put("progressionMin", progressionMin);
            stats.put("progressionMax", progressionMax);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== DÉTAILS PROGRESSION D'UN APPRENANT =====
    @GetMapping("/{formateurId}/cours/{coursId}/apprenant/{apprenantId}")
    public ResponseEntity<?> getDetailProgressionApprenant(
            @PathVariable Long formateurId,
            @PathVariable Long coursId,
            @PathVariable Long apprenantId) {
        try {
            var cours = coursService.getCoursById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

            if (!cours.getFormateur().getId().equals(formateurId)) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Vous n'êtes pas le formateur de ce cours"));
            }

            var apprenant = userService.getUserById(apprenantId)
                    .orElseThrow(() -> new RuntimeException("Apprenant non trouvé"));

            Double progression = progressionService.getProgressionPourcentageCours(apprenantId, coursId);
            Long nbCompletees = progressionService.getNbLeconsCoursesPourCours(apprenantId, coursId);
            Long nbTotal = progressionService.getNbTotalLeconsPourCours(coursId);

            Map<String, Object> result = new HashMap<>();
            result.put("apprenantNom", apprenant.getName());
            result.put("apprenantEmail", apprenant.getEmail());
            result.put("progression", progression);
            result.put("nbCompletees", nbCompletees);
            result.put("nbTotal", nbTotal);
            result.put("coursId", coursId);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== TABLEAU DE BORD FORMATEUR =====
    @GetMapping("/{formateurId}/tableau-bord")
    public ResponseEntity<?> getTableauBordFormateur(@PathVariable Long formateurId) {
        try {
            var formateur = userService.getUserById(formateurId)
                    .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));

            if (!formateur.getRole().equals(RoleEnum.FORMATEUR) && !formateur.getRole().equals(RoleEnum.ADMIN)) {
                return ResponseEntity.badRequest().body(Map.of("erreur", "Accès refusé"));
            }

            var mesCours = coursService.getCoursParFormateur(formateurId);

            long totalApprenants = 0;
            long totalCours = mesCours.size();
            double progressionGlobale = 0.0;

            for (var cours : mesCours) {
                long inscrits = inscriptionService.getNbInscrits(cours.getId());
                totalApprenants += inscrits;

                if (inscrits > 0) {
                    var inscriptions = inscriptionService.getApprenantsDuCours(cours.getId());
                    for (var inscription : inscriptions) {
                        progressionGlobale += progressionService.getProgressionPourcentageCours(
                                inscription.getApprenant().getId(), cours.getId());
                    }
                    progressionGlobale /= inscrits;
                }
            }

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("nomFormateur", formateur.getName());
            dashboard.put("totalCours", totalCours);
            dashboard.put("totalApprenants", totalApprenants);
            dashboard.put("progressionGlobale", totalApprenants > 0 ? progressionGlobale : 0.0);

            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
