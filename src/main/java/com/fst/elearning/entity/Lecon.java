package com.fst.elearning.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "lecons")
public class Lecon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String contenu;

    @Column(nullable = false)
    private Integer ordre;

    @Column(nullable = false)
    private Integer dureeMin;

    @ManyToOne
    @JoinColumn(name = "module_id", nullable = false)
    private Module module;

    // Relations OneToMany
    @OneToMany(mappedBy = "lecon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProgressionLecon> progressions;

    // Constructeurs
    public Lecon() {
    }

    public Lecon(String titre, String contenu, Integer ordre, Integer dureeMin, Module module) {
        this.titre = titre;
        this.contenu = contenu;
        this.ordre = ordre;
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

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public Integer getDureeMin() {
        return dureeMin;
    }

    public void setDureeMin(Integer dureeMin) {
        this.dureeMin = dureeMin;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public List<ProgressionLecon> getProgressions() {
        return progressions;
    }

    public void setProgressions(List<ProgressionLecon> progressions) {
        this.progressions = progressions;
    }
}
