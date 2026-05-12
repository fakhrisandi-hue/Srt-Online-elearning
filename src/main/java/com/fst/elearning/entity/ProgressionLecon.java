package com.fst.elearning.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progression_lecons", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "apprenant_id", "lecon_id" }) })
public class ProgressionLecon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "apprenant_id", nullable = false)
    private User apprenant;

    @ManyToOne
    @JoinColumn(name = "lecon_id", nullable = false)
    private Lecon lecon;

    @Column(nullable = false)
    private Boolean completee = false;

    private LocalDateTime dateCompletion;

    // Constructeurs
    public ProgressionLecon() {
    }

    public ProgressionLecon(User apprenant, Lecon lecon) {
        this.apprenant = apprenant;
        this.lecon = lecon;
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

    public Lecon getLecon() {
        return lecon;
    }

    public void setLecon(Lecon lecon) {
        this.lecon = lecon;
    }

    public Boolean getCompletee() {
        return completee;
    }

    public void setCompletee(Boolean completee) {
        this.completee = completee;
    }

    public LocalDateTime getDateCompletion() {
        return dateCompletion;
    }

    public void setDateCompletion(LocalDateTime dateCompletion) {
        this.dateCompletion = dateCompletion;
    }
}
