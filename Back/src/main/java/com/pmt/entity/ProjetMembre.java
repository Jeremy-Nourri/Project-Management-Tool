package com.pmt.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "projet_membre")
public class ProjetMembre {

    @EmbeddedId
    private ProjetMembreId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("projetId")
    @JoinColumn(name = "projet_id")
    private Projet projet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("utilisateurId")
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @Column(name = "date_ajout", nullable = false)
    private LocalDateTime dateAjout;

    public ProjetMembre(Projet projet, Utilisateur utilisateur, Role role) {
        this.projet = projet;
        this.utilisateur = utilisateur;
        this.role = role;
        this.id = new ProjetMembreId(projet.getId(), utilisateur.getId());
    }

    @PrePersist
    protected void onCreate() {
        if (dateAjout == null) {
            dateAjout = LocalDateTime.now();
        }
    }
}
