package com.fst.elearning.controller;

import com.fst.elearning.entity.Lecon;
import com.fst.elearning.service.LeconService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/lecons")
@CrossOrigin(origins = "*")
public class LeconController {

    private final LeconService leconService;

    public LeconController(LeconService leconService) {
        this.leconService = leconService;
    }

    @PostMapping
    public ResponseEntity<?> createLecon(@RequestBody Lecon lecon) {
        try {
            Lecon savedLecon = leconService.createLecon(lecon);
            return ResponseEntity.ok(savedLecon);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLeconById(@PathVariable Long id) {
        return leconService.getLeconById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<?> getLeconsDuModule(@PathVariable Long moduleId) {
        try {
            return ResponseEntity.ok(leconService.getLeconsDuModule(moduleId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLecon(@PathVariable Long id, @RequestBody Lecon leconDetails) {
        try {
            Lecon updated = leconService.updateLecon(id, leconDetails);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLecon(@PathVariable Long id) {
        leconService.deleteLecon(id);
        return ResponseEntity.ok(Map.of("message", "Leçon supprimée avec succès"));
    }
}
