package com.pmt.controller;

import com.pmt.dto.MembreDto;
import com.pmt.dto.MembreRequest;
import com.pmt.dto.ProjetDto;
import com.pmt.dto.ProjetRequest;
import com.pmt.service.ProjetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
@Tag(name = "Projets", description = "Creation de projets et gestion des membres")
public class ProjetController {

    private static final String EN_TETE_UTILISATEUR = "Authorization";

    private final ProjetService projetService;

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }

    @Operation(summary = "Creer un projet",
            description = "Le createur devient automatiquement administrateur du projet.")
    @ApiResponse(responseCode = "201", description = "Projet cree")
    @PostMapping
    public ResponseEntity<ProjetDto> creerProjet(
            @Valid @RequestBody ProjetRequest requete,
            @Parameter(description = "Identifiant de l'utilisateur connecte")
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projetService.creerProjet(requete, utilisateurId));
    }

    @Operation(summary = "Lister mes projets",
            description = "Retourne les projets dont l'utilisateur est membre, quel que soit son role.")
    @GetMapping
    public ResponseEntity<List<ProjetDto>> listerMesProjets(
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.ok(projetService.listerProjetsDeLUtilisateur(utilisateurId));
    }

    @Operation(summary = "Consulter un projet")
    @ApiResponse(responseCode = "200", description = "Projet trouve")
    @ApiResponse(responseCode = "403", description = "Utilisateur non membre du projet")
    @ApiResponse(responseCode = "404", description = "Projet inexistant")
    @GetMapping("/{projetId}")
    public ResponseEntity<ProjetDto> consulterProjet(
            @PathVariable Long projetId,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.ok(projetService.consulterProjet(projetId, utilisateurId));
    }

    @Operation(summary = "Lister les membres d'un projet")
    @GetMapping("/{projetId}/membres")
    public ResponseEntity<List<MembreDto>> listerMembres(
            @PathVariable Long projetId,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.ok(projetService.listerMembres(projetId, utilisateurId));
    }

    @Operation(summary = "Inviter un membre",
            description = "Reserve a l'administrateur du projet. L'invite doit deja avoir un compte.")
    @ApiResponse(responseCode = "201", description = "Membre ajoute")
    @ApiResponse(responseCode = "403", description = "Reserve a l'administrateur")
    @ApiResponse(responseCode = "404", description = "Aucun compte avec cet email")
    @ApiResponse(responseCode = "409", description = "Deja membre du projet")
    @PostMapping("/{projetId}/membres")
    public ResponseEntity<MembreDto> inviterMembre(
            @PathVariable Long projetId,
            @Valid @RequestBody MembreRequest requete,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projetService.inviterMembre(projetId, requete, utilisateurId));
    }

    @Operation(summary = "Modifier le role d'un membre",
            description = "Reserve a l'administrateur. Le dernier administrateur ne peut pas etre retrograde.")
    @ApiResponse(responseCode = "200", description = "Role modifie")
    @ApiResponse(responseCode = "403", description = "Reserve a l'administrateur")
    @ApiResponse(responseCode = "409", description = "Dernier administrateur du projet")
    @PutMapping("/{projetId}/membres")
    public ResponseEntity<MembreDto> attribuerRole(
            @PathVariable Long projetId,
            @Valid @RequestBody MembreRequest requete,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.ok(projetService.attribuerRole(projetId, requete, utilisateurId));
    }
}
