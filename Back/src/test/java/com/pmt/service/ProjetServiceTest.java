package com.pmt.service;

import com.pmt.dto.MembreRequest;
import com.pmt.dto.ProjetDto;
import com.pmt.dto.ProjetRequest;
import com.pmt.entity.Projet;
import com.pmt.entity.ProjetMembre;
import com.pmt.entity.Role;
import com.pmt.entity.Utilisateur;
import com.pmt.exception.AccesRefuseException;
import com.pmt.exception.ConflitException;
import com.pmt.exception.RessourceIntrouvableException;
import com.pmt.mapper.ProjetMapper;
import com.pmt.repository.ProjetMembreRepository;
import com.pmt.repository.ProjetRepository;
import com.pmt.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests unitaires de ProjetService : autorisations et regles metier. */
@ExtendWith(MockitoExtension.class)
class ProjetServiceTest {

    private static final Long PROJET_ID = 1L;
    private static final Long ALICE_ID = 10L;
    private static final Long BRUNO_ID = 20L;

    @Mock
    private ProjetRepository projetRepository;
    @Mock
    private ProjetMembreRepository projetMembreRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private ProjetMapper projetMapper;

    @InjectMocks
    private ProjetService projetService;

    private Utilisateur alice;
    private Utilisateur bruno;
    private Projet projet;

    @BeforeEach
    void preparer() {
        alice = utilisateur(ALICE_ID, "alice@codesolutions.fr");
        bruno = utilisateur(BRUNO_ID, "bruno@codesolutions.fr");

        projet = new Projet();
        projet.setId(PROJET_ID);
        projet.setNom("PMT");
        projet.setCreateur(alice);
    }

    // ---------------------------------------------------------------- verifierRole

    @Test
    void doitRefuserUnUtilisateurNonMembre() {
        when(projetMembreRepository.findByIdProjetIdAndIdUtilisateurId(PROJET_ID, BRUNO_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.verifierRole(PROJET_ID, BRUNO_ID, Role.ADMIN))
                .isInstanceOf(AccesRefuseException.class)
                .hasMessageContaining("pas membre");
    }

    @Test
    void doitRefuserUnRoleInsuffisant() {
        donnerRole(BRUNO_ID, bruno, Role.OBSERVATEUR);

        assertThatThrownBy(() ->
                projetService.verifierRole(PROJET_ID, BRUNO_ID, Role.ADMIN, Role.MEMBRE))
                .isInstanceOf(AccesRefuseException.class)
                .hasMessageContaining("OBSERVATEUR");
    }

    @Test
    void doitAccepterUnRoleAutoriseEtLeRetourner() {
        donnerRole(BRUNO_ID, bruno, Role.MEMBRE);

        Role role = projetService.verifierRole(PROJET_ID, BRUNO_ID, Role.ADMIN, Role.MEMBRE);

        assertThat(role).isEqualTo(Role.MEMBRE);
    }

    // ---------------------------------------------------------------- creerProjet

    @Test
    void doitCreerUnProjetEtInscrireLeCreateurCommeAdmin() {
        ProjetRequest requete = new ProjetRequest("PMT", "Outil collaboratif", LocalDate.now());

        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
        when(projetRepository.save(any(Projet.class))).thenReturn(projet);
        when(projetMapper.versDto(projet)).thenReturn(projetDto());

        ProjetDto resultat = projetService.creerProjet(requete, ALICE_ID);

        assertThat(resultat.nom()).isEqualTo("PMT");
        verify(projetMembreRepository).save(any(ProjetMembre.class));
    }

    @Test
    void doitRefuserLaCreationSiLeCreateurNExistePas() {
        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.creerProjet(
                new ProjetRequest("PMT", null, LocalDate.now()), ALICE_ID))
                .isInstanceOf(RessourceIntrouvableException.class);

        verify(projetRepository, never()).save(any());
    }

    // ---------------------------------------------------------------- inviterMembre

    @Test
    void doitInviterUnMembre() {
        MembreRequest requete = new MembreRequest("bruno@codesolutions.fr", Role.MEMBRE);

        donnerRole(ALICE_ID, alice, Role.ADMIN);
        when(projetRepository.findById(PROJET_ID)).thenReturn(Optional.of(projet));
        when(utilisateurRepository.findByEmail("bruno@codesolutions.fr"))
                .thenReturn(Optional.of(bruno));
        when(projetMembreRepository.existsByIdProjetIdAndIdUtilisateurId(PROJET_ID, BRUNO_ID))
                .thenReturn(false);
        when(projetMembreRepository.save(any(ProjetMembre.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        projetService.inviterMembre(PROJET_ID, requete, ALICE_ID);

        verify(projetMembreRepository).save(any(ProjetMembre.class));
    }

    @Test
    void doitRefuserUneInvitationParUnNonAdmin() {
        donnerRole(BRUNO_ID, bruno, Role.MEMBRE);

        assertThatThrownBy(() -> projetService.inviterMembre(PROJET_ID,
                new MembreRequest("autre@codesolutions.fr", Role.MEMBRE), BRUNO_ID))
                .isInstanceOf(AccesRefuseException.class);

        verify(projetMembreRepository, never()).save(any());
    }

    @Test
    void doitRefuserDInviterUnMembreDejaPresent() {
        donnerRole(ALICE_ID, alice, Role.ADMIN);
        when(projetRepository.findById(PROJET_ID)).thenReturn(Optional.of(projet));
        when(utilisateurRepository.findByEmail("bruno@codesolutions.fr"))
                .thenReturn(Optional.of(bruno));
        when(projetMembreRepository.existsByIdProjetIdAndIdUtilisateurId(PROJET_ID, BRUNO_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> projetService.inviterMembre(PROJET_ID,
                new MembreRequest("bruno@codesolutions.fr", Role.MEMBRE), ALICE_ID))
                .isInstanceOf(ConflitException.class);
    }

    @Test
    void doitRefuserDInviterUnEmailInconnu() {
        donnerRole(ALICE_ID, alice, Role.ADMIN);
        when(projetRepository.findById(PROJET_ID)).thenReturn(Optional.of(projet));
        when(utilisateurRepository.findByEmail("inconnu@codesolutions.fr"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.inviterMembre(PROJET_ID,
                new MembreRequest("inconnu@codesolutions.fr", Role.MEMBRE), ALICE_ID))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ---------------------------------------------------------------- attribuerRole

    @Test
    void doitModifierLeRoleDUnMembre() {
        ProjetMembre membreBruno = membre(bruno, Role.MEMBRE);

        donnerRole(ALICE_ID, alice, Role.ADMIN);
        when(utilisateurRepository.findByEmail("bruno@codesolutions.fr"))
                .thenReturn(Optional.of(bruno));
        when(projetMembreRepository.findByIdProjetIdAndIdUtilisateurId(PROJET_ID, BRUNO_ID))
                .thenReturn(Optional.of(membreBruno));
        when(projetMembreRepository.save(membreBruno)).thenReturn(membreBruno);

        projetService.attribuerRole(PROJET_ID,
                new MembreRequest("bruno@codesolutions.fr", Role.ADMIN), ALICE_ID);

        assertThat(membreBruno.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void doitRefuserDeRetrograderLeDernierAdministrateur() {
        ProjetMembre membreAlice = membre(alice, Role.ADMIN);

        // Alice est a la fois le demandeur et la cible : une seule et meme ligne.
        when(projetMembreRepository.findByIdProjetIdAndIdUtilisateurId(PROJET_ID, ALICE_ID))
                .thenReturn(Optional.of(membreAlice));
        when(utilisateurRepository.findByEmail("alice@codesolutions.fr"))
                .thenReturn(Optional.of(alice));
        when(projetMembreRepository.findByIdProjetId(PROJET_ID))
                .thenReturn(List.of(membreAlice));

        assertThatThrownBy(() -> projetService.attribuerRole(PROJET_ID,
                new MembreRequest("alice@codesolutions.fr", Role.OBSERVATEUR), ALICE_ID))
                .isInstanceOf(ConflitException.class)
                .hasMessageContaining("dernier administrateur");

        assertThat(membreAlice.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void doitAutoriserLaRetrogradationSiUnAutreAdminExiste() {
        ProjetMembre membreAlice = membre(alice, Role.ADMIN);

        when(projetMembreRepository.findByIdProjetIdAndIdUtilisateurId(PROJET_ID, ALICE_ID))
                .thenReturn(Optional.of(membreAlice));
        when(utilisateurRepository.findByEmail("alice@codesolutions.fr"))
                .thenReturn(Optional.of(alice));
        when(projetMembreRepository.findByIdProjetId(PROJET_ID))
                .thenReturn(List.of(membreAlice, membre(bruno, Role.ADMIN)));
        when(projetMembreRepository.save(membreAlice)).thenReturn(membreAlice);

        projetService.attribuerRole(PROJET_ID,
                new MembreRequest("alice@codesolutions.fr", Role.MEMBRE), ALICE_ID);

        assertThat(membreAlice.getRole()).isEqualTo(Role.MEMBRE);
    }

    @Test
    void doitRefuserDeModifierLeRoleDUnNonMembre() {
        donnerRole(ALICE_ID, alice, Role.ADMIN);
        when(utilisateurRepository.findByEmail("bruno@codesolutions.fr"))
                .thenReturn(Optional.of(bruno));
        when(projetMembreRepository.findByIdProjetIdAndIdUtilisateurId(PROJET_ID, BRUNO_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> projetService.attribuerRole(PROJET_ID,
                new MembreRequest("bruno@codesolutions.fr", Role.ADMIN), ALICE_ID))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ---------------------------------------------------------------- lectures

    @Test
    void doitListerLesProjetsDeLUtilisateur() {
        when(projetRepository.findProjetsDeLUtilisateur(ALICE_ID)).thenReturn(List.of(projet));
        when(projetMapper.versDto(projet)).thenReturn(projetDto());

        assertThat(projetService.listerProjetsDeLUtilisateur(ALICE_ID)).hasSize(1);
    }

    @Test
    void doitLaisserUnObservateurConsulterLeProjet() {
        donnerRole(BRUNO_ID, bruno, Role.OBSERVATEUR);
        when(projetRepository.findById(PROJET_ID)).thenReturn(Optional.of(projet));
        when(projetMapper.versDto(projet)).thenReturn(projetDto());

        assertThat(projetService.consulterProjet(PROJET_ID, BRUNO_ID).nom()).isEqualTo("PMT");
    }

    @Test
    void doitListerLesMembresDuProjet() {
        donnerRole(ALICE_ID, alice, Role.ADMIN);
        when(projetMembreRepository.findByIdProjetId(PROJET_ID))
                .thenReturn(List.of(membre(alice, Role.ADMIN), membre(bruno, Role.MEMBRE)));

        assertThat(projetService.listerMembres(PROJET_ID, ALICE_ID)).hasSize(2);
    }

    // ---------------------------------------------------------------- utilitaires

    private Utilisateur utilisateur(Long id, String email) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setEmail(email);
        utilisateur.setNom(email.substring(0, email.indexOf('@')));
        return utilisateur;
    }

    private ProjetMembre membre(Utilisateur utilisateur, Role role) {
        ProjetMembre membre = new ProjetMembre();
        membre.setUtilisateur(utilisateur);
        membre.setProjet(projet);
        membre.setRole(role);
        return membre;
    }

    /** Simule la presence de l'utilisateur dans projet_membre avec le role donne. */
    private void donnerRole(Long utilisateurId, Utilisateur utilisateur, Role role) {
        when(projetMembreRepository.findByIdProjetIdAndIdUtilisateurId(PROJET_ID, utilisateurId))
                .thenReturn(Optional.of(membre(utilisateur, role)));
    }

    private ProjetDto projetDto() {
        return new ProjetDto(PROJET_ID, "PMT", "Outil collaboratif", LocalDate.now(), null);
    }
}
