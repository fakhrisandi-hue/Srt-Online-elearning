package com.fst.elearning.controller;

import com.fst.elearning.entity.Inscription;
import com.fst.elearning.entity.StatutInscriptionEnum;
import com.fst.elearning.service.CoursService;
import com.fst.elearning.service.InscriptionService;
import com.fst.elearning.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/inscriptions")
@CrossOrigin(origins = "*")
public class InscriptionController {

    private final InscriptionService inscriptionService;
    private final UserService userService;
    private final CoursService coursService;

    public InscriptionController(InscriptionService inscriptionService,
            UserService userService,
            CoursService coursService) {
        this.inscriptionService = inscriptionService;
        this.userService = userService;
        this.coursService = coursService;
    }

    @PostMapping
    public ResponseEntity<?> inscrireApprenant(
            @RequestParam Long apprenantId,
            @RequestParam Long coursId) {
        try {
            var apprenant = userService.getUserById(apprenantId)
                    .orElseThrow(() -> new RuntimeException("Apprenant non trouvé"));
            var cours = coursService.getCoursById(coursId)
                    .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

            Inscription inscription = inscriptionService.inscrireApprenant(apprenant, cours);
            return ResponseEntity.ok(inscription);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/{apprenantId}/{coursId}")
    public ResponseEntity<?> getInscription(
            @PathVariable Long apprenantId,
            @PathVariable Long coursId) {
        return inscriptionService.getInscription(apprenantId, coursId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/apprenant/{apprenantId}")
    public ResponseEntity<?> getMesCours(@PathVariable Long apprenantId) {
        try {
            return ResponseEntity.ok(inscriptionService.getMesCours(apprenantId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/cours/{coursId}")
    public ResponseEntity<?> getApprenantsDuCours(@PathVariable Long coursId) {
        try {
            return ResponseEntity.ok(inscriptionService.getApprenantsDuCours(coursId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/{inscriptionId}/statut")
    public ResponseEntity<?> updateStatutInscription(
            @PathVariable Long inscriptionId,
            @RequestParam StatutInscriptionEnum statut) {
        try {
            inscriptionService.updateStatutInscription(inscriptionId, statut);
            return ResponseEntity.ok(Map.of("message", "Statut mis à jour avec succès"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/{inscriptionId}")
    public ResponseEntity<?> retirerInscription(@PathVariable Long inscriptionId) {
        inscriptionService.retirerInscription(inscriptionId);
        return ResponseEntity.ok(Map.of("message", "Inscription supprimée avec succès"));
    }

    // ===== STATISTIQUES =====
    @GetMapping("/stats/{coursId}")
    public ResponseEntity<?> getStatistiques(@PathVariable Long coursId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalInscrits", inscriptionService.getNbInscrits(coursId));
            stats.put("termines", inscriptionService.getNbTermines(coursId));
            stats.put("enCours", inscriptionService.getNbEnCours(coursId));
            stats.put("abandonnes", inscriptionService.getNbAbandonnes(coursId));
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
