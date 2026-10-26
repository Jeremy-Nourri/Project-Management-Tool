package com.pmt.controller;

import com.pmt.dto.ConnexionRequest;
import com.pmt.dto.InscriptionRequest;
import com.pmt.dto.UtilisateurDto;
import com.pmt.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentification", description = "Creation de compte et connexion")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Inscrire un nouvel utilisateur",
            description = "Cree un compte. L'email doit etre unique et le mot de passe faire 8 caracteres minimum.")
    @ApiResponse(responseCode = "201", description = "Compte cree")
    @ApiResponse(responseCode = "400", description = "Donnees invalides")
    @ApiResponse(responseCode = "409", description = "Email deja utilise")
    @PostMapping("/inscription")
    public ResponseEntity<UtilisateurDto> inscrire(@Valid @RequestBody InscriptionRequest requete) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.inscrire(requete));
    }

    @Operation(
            summary = "Connecter un utilisateur",
            description = "Verifie les identifiants et retourne le profil. "
                    + "L'identifiant retourne est a transmettre dans l'en-tete Authorization "
                    + "pour les appels suivants.")
    @ApiResponse(responseCode = "200", description = "Connexion reussie")
    @ApiResponse(responseCode = "404", description = "Email ou mot de passe incorrect")
    @PostMapping("/connexion")
    public ResponseEntity<UtilisateurDto> connecter(@Valid @RequestBody ConnexionRequest requete) {
        return ResponseEntity.ok(authService.connecter(requete));
    }
}
