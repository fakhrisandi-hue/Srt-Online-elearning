package com.fst.elearning.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(nullable = false)
    private Integer dureeMin = 15;

    @Column(nullable = false)
    private Boolean publique = false;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    // Relations OneToMany
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReponseApprenant> reponses;

    // Constructeurs
    public Quiz() {
    }

    public Quiz(String titre, Integer dureeMin, Module module) {
        this.titre = titre;
        this.dureeMin = dureeMin;
        this.module = module;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDureeMin() {
        return dureeMin;
    }

    public void setDureeMin(Integer dureeMin) {
        this.dureeMin = dureeMin;
    }

    public Boolean getPublique() {
        return publique;
    }

    public void setPublique(Boolean publique) {
        this.publique = publique;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<ReponseApprenant> getReponses() {
        return reponses;
    }

    public void setReponses(List<ReponseApprenant> reponses) {
        this.reponses = reponses;
    }
}
