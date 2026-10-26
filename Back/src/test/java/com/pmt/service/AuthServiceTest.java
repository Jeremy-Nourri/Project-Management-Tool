package com.pmt.service;

import com.pmt.dto.ConnexionRequest;
import com.pmt.dto.InscriptionRequest;
import com.pmt.dto.UtilisateurDto;
import com.pmt.entity.Utilisateur;
import com.pmt.exception.ConflitException;
import com.pmt.exception.RessourceIntrouvableException;
import com.pmt.mapper.UtilisateurMapper;
import com.pmt.repository.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires d'AuthService : aucune base de donnees,
 * les dependances sont simulees par Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UtilisateurMapper utilisateurMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void doitInscrireUnNouvelUtilisateur() {
        InscriptionRequest requete =
                new InscriptionRequest("Alice Martin", "alice@codesolutions.fr", "motdepasse123");

        when(utilisateurRepository.existsByEmail("alice@codesolutions.fr")).thenReturn(false);
        when(passwordEncoder.encode("motdepasse123")).thenReturn("$2a$10$hash");
        when(utilisateurRepository.save(any(Utilisateur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(utilisateurMapper.versDto(any(Utilisateur.class)))
                .thenReturn(new UtilisateurDto(1L, "Alice Martin", "alice@codesolutions.fr", null));

        UtilisateurDto resultat = authService.inscrire(requete);

        assertThat(resultat.email()).isEqualTo("alice@codesolutions.fr");
        verify(passwordEncoder).encode("motdepasse123");
    }

    @Test
    void doitRefuserUneInscriptionAvecUnEmailDejaUtilise() {
        InscriptionRequest requete =
                new InscriptionRequest("Alice Martin", "alice@codesolutions.fr", "motdepasse123");

        when(utilisateurRepository.existsByEmail("alice@codesolutions.fr")).thenReturn(true);

        assertThatThrownBy(() -> authService.inscrire(requete))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("alice@codesolutions.fr");

        verify(utilisateurRepository, never()).save(any());
    }

    @Test
    void doitConnecterUnUtilisateurAvecLesBonsIdentifiants() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("alice@codesolutions.fr");
        utilisateur.setMotDePasse("$2a$10$hash");

        when(utilisateurRepository.findByEmail("alice@codesolutions.fr"))
                .thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("motdepasse123", "$2a$10$hash")).thenReturn(true);
        when(utilisateurMapper.versDto(utilisateur))
                .thenReturn(new UtilisateurDto(1L, "Alice Martin", "alice@codesolutions.fr", null));

        UtilisateurDto resultat = authService.connecter(
                new ConnexionRequest("alice@codesolutions.fr", "motdepasse123"));

        assertThat(resultat.id()).isEqualTo(1L);
    }

    @Test
    void doitRefuserLaConnexionAvecUnEmailInconnu() {
        when(utilisateurRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.connecter(
                new ConnexionRequest("inconnu@codesolutions.fr", "motdepasse123")))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    @Test
    void doitRefuserLaConnexionAvecUnMauvaisMotDePasse() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("alice@codesolutions.fr");
        utilisateur.setMotDePasse("$2a$10$hash");

        when(utilisateurRepository.findByEmail("alice@codesolutions.fr"))
                .thenReturn(Optional.of(utilisateur));
        when(passwordEncoder.matches("mauvais", "$2a$10$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.connecter(
                new ConnexionRequest("alice@codesolutions.fr", "mauvais")))
                .isInstanceOf(RessourceIntrouvableException.class);

        verify(utilisateurMapper, never()).versDto(any());
    }
}
