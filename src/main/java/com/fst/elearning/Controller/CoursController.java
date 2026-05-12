package com.fst.elearning.controller;

import com.fst.elearning.entity.Cours;
import com.fst.elearning.entity.NiveauEnum;
import com.fst.elearning.entity.User;
import com.fst.elearning.service.CoursService;
import com.fst.elearning.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cours")
@CrossOrigin(origins = "*")
public class CoursController {

    private final CoursService coursService;
    private final UserService userService;

    public CoursController(CoursService coursService, UserService userService) {
        this.coursService = coursService;
        this.userService = userService;
    }

    // ===== CRUD =====
    @PostMapping
    public ResponseEntity<?> createCours(@RequestBody Cours cours, @RequestParam Long formateurId) {
        try {
            User formateur = userService.getUserById(formateurId)
                    .orElseThrow(() -> new RuntimeException("Formateur non trouvé"));
            Cours savedCours = coursService.createCours(cours, formateur);
            return ResponseEntity.ok(savedCours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCoursById(@PathVariable Long id) {
        return coursService.getCoursById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCours(@PathVariable Long id, @RequestBody Cours coursDetails) {
        try {
            Cours updated = coursService.updateCours(id, coursDetails);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCours(@PathVariable Long id) {
        coursService.deactiverCours(id);
        return ResponseEntity.ok(Map.of("message", "Cours désactivé avec succès"));
    }

    @PutMapping("/{id}/activer")
    public ResponseEntity<?> activerCours(@PathVariable Long id) {
        try {
            Cours cours = coursService.activerCours(id);
            return ResponseEntity.ok(cours);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== RECHERCHE & PAGINATION =====
    @GetMapping("/public/catalogue")
    public ResponseEntity<Page<Cours>> getCataloguePublic(
            @PageableDefault(size = 12, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Cours> cours = coursService.getAllCoursPublics(pageable);
        return ResponseEntity.ok(cours);
    }

    @GetMapping("/public/search")
    public ResponseEntity<Page<Cours>> searchCours(
            @RequestParam(required = false) String titre,
            @RequestParam(required = false) String categorie,
            @RequestParam(required = false) NiveauEnum niveau,
            @PageableDefault(size = 12, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Cours> results;

        if ((titre != null && !titre.isEmpty()) || categorie != null || niveau != null) {
            results = coursService.searchWithFilters(titre != null ? titre : "", categorie, niveau, pageable);
        } else {
            results = coursService.getAllCoursPublics(pageable);
        }

        return ResponseEntity.ok(results);
    }

    @GetMapping("/public/search/titre")
    public ResponseEntity<Page<Cours>> searchByTitre(
            @RequestParam String titre,
            @PageableDefault(size = 12, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Cours> courses = coursService.searchCoursByTitre(titre, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/public/filter/categorie")
    public ResponseEntity<Page<Cours>> filterByCategorie(
            @RequestParam String categorie,
            @PageableDefault(size = 12, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Cours> courses = coursService.filterByCategorie(categorie, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/public/filter/niveau")
    public ResponseEntity<Page<Cours>> filterByNiveau(
            @RequestParam NiveauEnum niveau,
            @PageableDefault(size = 12, sort = "dateCreation", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Cours> courses = coursService.filterByNiveau(niveau, pageable);
        return ResponseEntity.ok(courses);
    }

    @GetMapping("/formateur/{formateurId}")
    public ResponseEntity<?> getCoursParFormateur(@PathVariable Long formateurId) {
        try {
            return ResponseEntity.ok(coursService.getCoursParFormateur(formateurId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== GESTION D'IMAGES =====
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile file) {
        try {
            String imageUrl = coursService.uploadImage(file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Erreur lors de l'upload: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/update-image")
    public ResponseEntity<?> updateImageForCours(
            @PathVariable Long id,
            @RequestParam MultipartFile file) {
        try {
            String imageUrl = coursService.updateImageForCours(id, file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", "Erreur lors de l'upload: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("erreur", e.getMessage()));
        }
    }

    // ===== UTILITAIRE =====
    @GetMapping("/niveaux")
    public ResponseEntity<?> getNiveaux() {
        return ResponseEntity.ok(NiveauEnum.values());
    }
}
