package com.fst.elearning.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "inscriptions", uniqueConstraints = { @UniqueConstraint(columnNames = { "apprenant_id", "cours_id" }) })
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "apprenant_id", nullable = false)
    private User apprenant;

    @ManyToOne
    @JoinColumn(name = "cours_id", nullable = false)
    private Cours cours;

    @Column(nullable = false)
    private LocalDate dateInscription = LocalDate.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutInscriptionEnum statut = StatutInscriptionEnum.EN_COURS;

    // Constructeurs
    public Inscription() {
    }

    public Inscription(User apprenant, Cours cours) {
        this.apprenant = apprenant;
        this.cours = cours;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public User getApprenant() {
        return apprenant;
    }

    public void setApprenant(User apprenant) {
        this.apprenant = apprenant;
    }

    public Cours getCours() {
        return cours;
    }

    public void setCours(Cours cours) {
        this.cours = cours;
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public StatutInscriptionEnum getStatut() {
        return statut;
    }

    public void setStatut(StatutInscriptionEnum statut) {
        this.statut = statut;
    }
}
