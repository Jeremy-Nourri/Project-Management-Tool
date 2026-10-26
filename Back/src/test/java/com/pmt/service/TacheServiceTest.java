package com.pmt.service;

import com.pmt.dto.TacheDto;
import com.pmt.dto.TacheRequest;
import com.pmt.entity.ChampTache;
import com.pmt.entity.HistoriqueTache;
import com.pmt.entity.PrioriteTache;
import com.pmt.entity.Projet;
import com.pmt.entity.Role;
import com.pmt.entity.StatutTache;
import com.pmt.entity.Tache;
import com.pmt.entity.Utilisateur;
import com.pmt.exception.AccesRefuseException;
import com.pmt.exception.RessourceIntrouvableException;
import com.pmt.mapper.HistoriqueMapper;
import com.pmt.mapper.TacheMapper;
import com.pmt.repository.HistoriqueTacheRepository;
import com.pmt.repository.ProjetRepository;
import com.pmt.repository.TacheRepository;
import com.pmt.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Tests unitaires de TacheService : autorisations, tracage et notifications. */
@ExtendWith(MockitoExtension.class)
class TacheServiceTest {

    private static final Long PROJET_ID = 1L;
    private static final Long TACHE_ID = 100L;
    private static final Long ALICE_ID = 10L;
    private static final Long BRUNO_ID = 20L;

    @Mock
    private TacheRepository tacheRepository;
    @Mock
    private ProjetRepository projetRepository;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private HistoriqueTacheRepository historiqueTacheRepository;
    @Mock
    private ProjetService projetService;
    @Mock
    private NotificationService notificationService;
    @Mock
    private TacheMapper tacheMapper;
    @Mock
    private HistoriqueMapper historiqueMapper;

    @InjectMocks
    private TacheService tacheService;

    private Utilisateur alice;
    private Utilisateur bruno;
    private Projet projet;
    private Tache tache;

    @BeforeEach
    void preparer() {
        alice = utilisateur(ALICE_ID, "alice@codesolutions.fr");
        bruno = utilisateur(BRUNO_ID, "bruno@codesolutions.fr");

        projet = new Projet();
        projet.setId(PROJET_ID);
        projet.setNom("PMT");

        tache = new Tache();
        tache.setId(TACHE_ID);
        tache.setProjet(projet);
        tache.setCreateur(alice);
        tache.setNom("Concevoir le schema");
        tache.setPriorite(PrioriteTache.MOYENNE);
        tache.setStatut(StatutTache.A_FAIRE);
    }

    // ---------------------------------------------------------------- creerTache

    @Test
    void doitCreerUneTacheSansAssignation() {
        when(projetRepository.findById(PROJET_ID)).thenReturn(Optional.of(projet));
        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        TacheDto resultat = tacheService.creerTache(PROJET_ID, requete(null), ALICE_ID);

        assertThat(resultat.nom()).isEqualTo("Concevoir le schema");
        verify(notificationService, never()).notifierAssignation(any(), any());
    }

    @Test
    void doitNotifierLAssigneALaCreation() {
        when(projetRepository.findById(PROJET_ID)).thenReturn(Optional.of(projet));
        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
        when(utilisateurRepository.findByEmail("bruno@codesolutions.fr"))
                .thenReturn(Optional.of(bruno));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        tacheService.creerTache(PROJET_ID, requete("bruno@codesolutions.fr"), ALICE_ID);

        verify(notificationService).notifierAssignation(tache, bruno);
    }

    @Test
    void doitRefuserLaCreationAUnObservateur() {
        doThrow(new AccesRefuseException("Role insuffisant"))
                .when(projetService).verifierRole(PROJET_ID, BRUNO_ID, Role.ADMIN, Role.MEMBRE);

        assertThatThrownBy(() -> tacheService.creerTache(PROJET_ID, requete(null), BRUNO_ID))
                .isInstanceOf(AccesRefuseException.class);

        verify(tacheRepository, never()).save(any());
    }

    @Test
    void doitRefuserLaCreationSurUnProjetInexistant() {
        when(projetRepository.findById(PROJET_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tacheService.creerTache(PROJET_ID, requete(null), ALICE_ID))
                .isInstanceOf(RessourceIntrouvableException.class);
    }

    // ---------------------------------------------------------------- mettreAJourTache

    @Test
    void doitTracerChaqueChampModifie() {
        when(tacheRepository.findById(TACHE_ID)).thenReturn(Optional.of(tache));
        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
        when(tacheRepository.save(tache)).thenReturn(tache);
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        TacheRequest requete = new TacheRequest("Nouveau nom", null, null, null,
                PrioriteTache.HAUTE, StatutTache.EN_COURS, null);

        tacheService.mettreAJourTache(TACHE_ID, requete, ALICE_ID);

        ArgumentCaptor<List<HistoriqueTache>> captor = ArgumentCaptor.forClass(List.class);
        verify(historiqueTacheRepository).saveAll(captor.capture());

        assertThat(captor.getValue())
                .extracting(HistoriqueTache::getNomChamp)
                .containsExactlyInAnyOrder(
                        ChampTache.NOM, ChampTache.PRIORITE, ChampTache.STATUT);

        assertThat(tache.getStatut()).isEqualTo(StatutTache.EN_COURS);
    }

    @Test
    void doitEnregistrerLAncienneEtLaNouvelleValeur() {
        when(tacheRepository.findById(TACHE_ID)).thenReturn(Optional.of(tache));
        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
        when(tacheRepository.save(tache)).thenReturn(tache);
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        TacheRequest requete = new TacheRequest("Concevoir le schema", null, null, null,
                null, StatutTache.TERMINEE, null);

        tacheService.mettreAJourTache(TACHE_ID, requete, ALICE_ID);

        ArgumentCaptor<List<HistoriqueTache>> captor = ArgumentCaptor.forClass(List.class);
        verify(historiqueTacheRepository).saveAll(captor.capture());

        HistoriqueTache ligne = captor.getValue().getFirst();
        assertThat(ligne.getNomChamp()).isEqualTo(ChampTache.STATUT);
        assertThat(ligne.getAncienneValeur()).isEqualTo("A_FAIRE");
        assertThat(ligne.getNouvelleValeur()).isEqualTo("TERMINEE");
        assertThat(ligne.getAuteur()).isEqualTo(alice);
    }

    @Test
    void neDoitRienTracerSiAucuneValeurNeChange() {
        when(tacheRepository.findById(TACHE_ID)).thenReturn(Optional.of(tache));
        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
        when(tacheRepository.save(tache)).thenReturn(tache);
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        TacheRequest requete = new TacheRequest("Concevoir le schema", null, null, null,
                PrioriteTache.MOYENNE, StatutTache.A_FAIRE, null);

        tacheService.mettreAJourTache(TACHE_ID, requete, ALICE_ID);

        ArgumentCaptor<List<HistoriqueTache>> captor = ArgumentCaptor.forClass(List.class);
        verify(historiqueTacheRepository).saveAll(captor.capture());

        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    void doitTracerLAssignationEtNotifier() {
        when(tacheRepository.findById(TACHE_ID)).thenReturn(Optional.of(tache));
        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
        when(utilisateurRepository.findByEmail("bruno@codesolutions.fr"))
                .thenReturn(Optional.of(bruno));
        when(tacheRepository.save(tache)).thenReturn(tache);
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        TacheRequest requete = new TacheRequest("Concevoir le schema", null, null, null,
                null, null, "bruno@codesolutions.fr");

        tacheService.mettreAJourTache(TACHE_ID, requete, ALICE_ID);

        assertThat(tache.getAssigne()).isEqualTo(bruno);
        verify(notificationService).notifierAssignation(tache, bruno);
    }

    @Test
    void neDoitPasRenotifierSiLAssigneEstInchange() {
        tache.setAssigne(bruno);

        when(tacheRepository.findById(TACHE_ID)).thenReturn(Optional.of(tache));
        when(utilisateurRepository.findById(ALICE_ID)).thenReturn(Optional.of(alice));
        when(utilisateurRepository.findByEmail("bruno@codesolutions.fr"))
                .thenReturn(Optional.of(bruno));
        when(tacheRepository.save(tache)).thenReturn(tache);
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        TacheRequest requete = new TacheRequest("Concevoir le schema", null, null, null,
                null, null, "bruno@codesolutions.fr");

        tacheService.mettreAJourTache(TACHE_ID, requete, ALICE_ID);

        verify(notificationService, never()).notifierAssignation(any(), any());
    }

    @Test
    void doitRefuserLaMiseAJourSurUneTacheInexistante() {
        when(tacheRepository.findById(TACHE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                tacheService.mettreAJourTache(TACHE_ID, requete(null), ALICE_ID))
                .isInstanceOf(RessourceIntrouvableException.class);

        verify(historiqueTacheRepository, never()).saveAll(anyList());
    }

    // ---------------------------------------------------------------- lectures

    @Test
    void doitConsulterUneTache() {
        when(tacheRepository.findById(TACHE_ID)).thenReturn(Optional.of(tache));
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        assertThat(tacheService.consulterTache(TACHE_ID, BRUNO_ID).id()).isEqualTo(TACHE_ID);
    }

    @Test
    void doitListerToutesLesTachesSansFiltre() {
        when(tacheRepository.findByProjetId(PROJET_ID)).thenReturn(List.of(tache));
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        assertThat(tacheService.listerTaches(PROJET_ID, null, ALICE_ID)).hasSize(1);
        verify(tacheRepository, never()).findByProjetIdAndStatut(any(), any());
    }

    @Test
    void doitFiltrerLesTachesParStatut() {
        when(tacheRepository.findByProjetIdAndStatut(PROJET_ID, StatutTache.EN_COURS))
                .thenReturn(List.of(tache));
        when(tacheMapper.versDto(tache)).thenReturn(tacheDto());

        assertThat(tacheService.listerTaches(PROJET_ID, StatutTache.EN_COURS, ALICE_ID))
                .hasSize(1);
        verify(tacheRepository, never()).findByProjetId(any());
    }

    @Test
    void doitListerLHistoriqueDUneTache() {
        HistoriqueTache ligne =
                new HistoriqueTache(tache, alice, ChampTache.STATUT, "A_FAIRE", "EN_COURS");

        when(tacheRepository.findById(TACHE_ID)).thenReturn(Optional.of(tache));
        when(historiqueTacheRepository.findByTacheIdOrderByDateModifDesc(TACHE_ID))
                .thenReturn(List.of(ligne));
        when(historiqueMapper.versDto(ligne)).thenReturn(null);

        assertThat(tacheService.listerHistorique(TACHE_ID, ALICE_ID)).hasSize(1);
    }

    // ---------------------------------------------------------------- utilitaires

    private Utilisateur utilisateur(Long id, String email) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(id);
        utilisateur.setEmail(email);
        utilisateur.setNom(email.substring(0, email.indexOf('@')));
        return utilisateur;
    }

    private TacheRequest requete(String assigneEmail) {
        return new TacheRequest("Concevoir le schema", "Modeliser les entites",
                LocalDate.now().plusDays(7), null,
                PrioriteTache.HAUTE, StatutTache.A_FAIRE, assigneEmail);
    }

    private TacheDto tacheDto() {
        return new TacheDto(TACHE_ID, "Concevoir le schema", "Modeliser les entites",
                LocalDate.now().plusDays(7), null, PrioriteTache.HAUTE, StatutTache.A_FAIRE,
                PROJET_ID, null, null, null);
    }
}
