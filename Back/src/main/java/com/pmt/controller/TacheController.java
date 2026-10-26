package com.pmt.controller;

import com.pmt.dto.HistoriqueDto;
import com.pmt.dto.TacheDto;
import com.pmt.dto.TacheRequest;
import com.pmt.entity.StatutTache;
import com.pmt.service.TacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "Taches", description = "Creation, suivi et historique des taches")
public class TacheController {

    private static final String EN_TETE_UTILISATEUR = "Authorization";

    private final TacheService tacheService;

    public TacheController(TacheService tacheService) {
        this.tacheService = tacheService;
    }

    @Operation(summary = "Creer une tache",
            description = "Reserve aux administrateurs et aux membres du projet.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tache creee"),
            @ApiResponse(responseCode = "403", description = "Role insuffisant")
    })
    @PostMapping("/projets/{projetId}/taches")
    public ResponseEntity<TacheDto> creerTache(
            @PathVariable Long projetId,
            @Valid @RequestBody TacheRequest requete,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tacheService.creerTache(projetId, requete, utilisateurId));
    }

    @Operation(summary = "Lister les taches d'un projet",
            description = "Alimente le tableau de bord. Le parametre statut permet de filtrer "
                    + "par colonne (A_FAIRE, EN_COURS, TERMINEE).")
    @GetMapping("/projets/{projetId}/taches")
    public ResponseEntity<List<TacheDto>> listerTaches(
            @PathVariable Long projetId,
            @Parameter(description = "Statut a filtrer, toutes les taches si absent")
            @RequestParam(required = false) StatutTache statut,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.ok(tacheService.listerTaches(projetId, statut, utilisateurId));
    }

    @Operation(summary = "Consulter une tache",
            description = "Accessible a tout membre du projet, observateur compris.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tache trouvee"),
            @ApiResponse(responseCode = "403", description = "Utilisateur non membre du projet"),
            @ApiResponse(responseCode = "404", description = "Tache inexistante")
    })
    @GetMapping("/taches/{tacheId}")
    public ResponseEntity<TacheDto> consulterTache(
            @PathVariable Long tacheId,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.ok(tacheService.consulterTache(tacheId, utilisateurId));
    }

    @Operation(summary = "Mettre a jour une tache",
            description = "Reserve aux administrateurs et aux membres. "
                    + "Les champs absents ou nuls ne sont pas modifies. "
                    + "Chaque champ reellement modifie est trace dans l'historique.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tache mise a jour"),
            @ApiResponse(responseCode = "403", description = "Role insuffisant"),
            @ApiResponse(responseCode = "404", description = "Tache inexistante")
    })
    @PutMapping("/taches/{tacheId}")
    public ResponseEntity<TacheDto> mettreAJourTache(
            @PathVariable Long tacheId,
            @Valid @RequestBody TacheRequest requete,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.ok(tacheService.mettreAJourTache(tacheId, requete, utilisateurId));
    }

    @Operation(summary = "Historique d'une tache",
            description = "Liste les modifications, de la plus recente a la plus ancienne.")
    @GetMapping("/taches/{tacheId}/historique")
    public ResponseEntity<List<HistoriqueDto>> listerHistorique(
            @PathVariable Long tacheId,
            @RequestHeader(EN_TETE_UTILISATEUR) Long utilisateurId) {

        return ResponseEntity.ok(tacheService.listerHistorique(tacheId, utilisateurId));
    }
}
