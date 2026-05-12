package com.fst.elearning.controller;

import com.fst.elearning.entity.ProgressionLecon;
import com.fst.elearning.service.LeconService;
import com.fst.elearning.service.ProgressionService;
import com.fst.elearning.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/progression")
@CrossOrigin(origins = "*")
public class ProgressionController {

    private final ProgressionService progressionService;
    private final UserService userService;
    private final LeconService leconService;

    public ProgressionController(ProgressionService progressionService,
            UserService userService,
            LeconService leconService) {
        this.progressionService = progressionService;
        this.userService = userService;
        this.leconService = leconService;
    }

    @PostMapping("/marquer-completee")
    public ResponseEntity<?> marquerLeconCompletee(
            @RequestParam Long apprenantId,
            @RequestParam Long leconId) {
        try {
            var apprenant = userService.getUserById(apprenantId)
                    .orElseThrow(() -> new RuntimeException("Apprenant non trouvé"));
            var lecon = leconService.getLeconById(leconId)
                    .orElseThrow(() -> new RuntimeException("Leçon non trouvée"));

            ProgressionLecon progression = progressionService.marquerLeconCompletee(apprenant, lecon);
            return ResponseEntity.ok(progression);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PostMapping("/marquer-non-completee")
    public ResponseEntity<?> marquerLeconNonCompletee(
            @RequestParam Long apprenantId,
            @RequestParam Long leconId) {
        try {
            ProgressionLecon progression = progressionService.marquerLeconNonCompletee(apprenantId, leconId);
            return ResponseEntity.ok(progression);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/{apprenantId}/{leconId}")
    public ResponseEntity<?> getProgressionLecon(
            @PathVariable Long apprenantId,
            @PathVariable Long leconId) {
        return progressionService.getProgressionLecon(apprenantId, leconId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ===== PROGRESSION GLOBALE =====
    @GetMapping("/cours/{apprenantId}/{coursId}")
    public ResponseEntity<?> getProgressionCours(
            @PathVariable Long apprenantId,
            @PathVariable Long coursId) {
        try {
            Double progression = progressionService.getProgressionPourcentageCours(apprenantId, coursId);
            Map<String, Object> result = new HashMap<>();
            result.put("pourcentage", progression);
            result.put("nbCompletees", progressionService.getNbLeconsCoursesPourCours(apprenantId, coursId));
            result.put("nbTotal", progressionService.getNbTotalLeconsPourCours(coursId));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/module/{apprenantId}/{moduleId}")
    public ResponseEntity<?> getProgressionModule(
            @PathVariable Long apprenantId,
            @PathVariable Long moduleId) {
        try {
            Double progression = progressionService.getProgressionPourcentageModule(apprenantId, moduleId);
            Map<String, Object> result = new HashMap<>();
            result.put("pourcentage", progression);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/apprenant/{apprenantId}")
    public ResponseEntity<?> getProgressionApprenant(@PathVariable Long apprenantId) {
        try {
            return ResponseEntity.ok(progressionService.getProgressionApprenant(apprenantId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }
}
