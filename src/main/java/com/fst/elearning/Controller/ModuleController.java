package com.fst.elearning.controller;

import com.fst.elearning.entity.Module;
import com.fst.elearning.service.ModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "*")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @PostMapping
    public ResponseEntity<?> createModule(@RequestBody Module module) {
        try {
            Module savedModule = moduleService.createModule(module);
            return ResponseEntity.ok(savedModule);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getModuleById(@PathVariable Long id) {
        return moduleService.getModuleById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/cours/{coursId}")
    public ResponseEntity<?> getModulesDuCours(@PathVariable Long coursId) {
        try {
            return ResponseEntity.ok(moduleService.getModulesDuCours(coursId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateModule(@PathVariable Long id, @RequestBody Module moduleDetails) {
        try {
            Module updated = moduleService.updateModule(id, moduleDetails);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.ok(Map.of("message", "Module supprimé avec succès"));
    }
}
