package com.fst.elearning.service;

import com.fst.elearning.repository.CoursRepository;
import com.fst.elearning.entity.Cours;
import com.fst.elearning.entity.NiveauEnum;
import com.fst.elearning.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CoursService {

    private final CoursRepository coursRepository;
    private final String uploadDir = "uploads/images/";

    public CoursService(CoursRepository coursRepository) {
        this.coursRepository = coursRepository;
        initializeUploadDir();
    }

    private void initializeUploadDir() {
        try {
            Path path = Paths.get(uploadDir);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Impossible de créer le répertoire d'upload", e);
        }
    }

    // ===== CRUD =====
    public Cours createCours(Cours cours, User formateur) {
        cours.setFormateur(formateur);
        cours.setDateCreation(LocalDateTime.now());
        cours.setActif(true);
        return coursRepository.save(cours);
    }

    public Cours updateCours(Long id, Cours coursDetails) {
        return coursRepository.findById(id).map(cours -> {
            cours.setTitre(coursDetails.getTitre());
            cours.setDescription(coursDetails.getDescription());
            cours.setCategorie(coursDetails.getCategorie());
            cours.setNiveau(coursDetails.getNiveau());
            if (coursDetails.getImageUrl() != null) {
                cours.setImageUrl(coursDetails.getImageUrl());
            }
            cours.setActif(coursDetails.getActif());
            cours.setDateModification(LocalDateTime.now());
            return coursRepository.save(cours);
        }).orElseThrow(() -> new RuntimeException("Cours non trouvé"));
    }

    public Optional<Cours> getCoursById(Long id) {
        return coursRepository.findById(id);
    }

    public Cours activerCours(Long id) {
        return coursRepository.findById(id).map(cours -> {
            cours.setActif(true);
            return coursRepository.save(cours);
        }).orElseThrow(() -> new RuntimeException("Cours non trouvé"));
    }

    public void deactiverCours(Long id) {
        coursRepository.findById(id).ifPresent(cours -> {
            cours.setActif(false);
            coursRepository.save(cours);
        });
    }

    // ===== RECHERCHE & PAGINATION =====
    public Page<Cours> getAllCoursPublics(Pageable pageable) {
        return coursRepository.findByActifTrue(pageable);
    }

    public Page<Cours> searchCoursByTitre(String titre, Pageable pageable) {
        return coursRepository.findByTitreContainingIgnoreCaseAndActifTrue(titre, pageable);
    }

    public Page<Cours> filterByCategorie(String categorie, Pageable pageable) {
        return coursRepository.findByCategorieAndActifTrue(categorie, pageable);
    }

    public Page<Cours> filterByNiveau(NiveauEnum niveau, Pageable pageable) {
        return coursRepository.findByNiveauAndActifTrue(niveau, pageable);
    }

    public Page<Cours> searchWithFilters(String titre, String categorie, NiveauEnum niveau, Pageable pageable) {
        return coursRepository.searchCoursFiltered(titre, categorie, niveau, pageable);
    }

    public List<Cours> getCoursParFormateur(Long formateurId) {
        return coursRepository.findByFormateurIdAndActifTrue(formateurId);
    }

    // ===== GESTION D'IMAGES =====
    public String uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier ne peut pas être vide");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.copy(file.getInputStream(), filePath);

        return "/images/" + fileName;
    }

    public String updateImageForCours(Long coursId, MultipartFile file) throws IOException {
        Cours cours = coursRepository.findById(coursId)
                .orElseThrow(() -> new RuntimeException("Cours non trouvé"));

        // Supprimer l'ancienne image si elle existe
        if (cours.getImageUrl() != null && !cours.getImageUrl().isEmpty()) {
            deleteImage(cours.getImageUrl());
        }

        String imageUrl = uploadImage(file);
        cours.setImageUrl(imageUrl);
        coursRepository.save(cours);

        return imageUrl;
    }

    public void deleteImage(String imageUrl) {
        try {
            if (imageUrl.startsWith("/images/")) {
                String fileName = imageUrl.substring("/images/".length());
                Path filePath = Paths.get(uploadDir, fileName);
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression de l'image: " + e.getMessage());
        }
    }
}
