package com.pmt.service;

import com.pmt.dto.HistoriqueDto;
import com.pmt.dto.TacheDto;
import com.pmt.dto.TacheRequest;
import com.pmt.entity.ChampTache;
import com.pmt.entity.HistoriqueTache;
import com.pmt.entity.Projet;
import com.pmt.entity.Role;
import com.pmt.entity.StatutTache;
import com.pmt.entity.Tache;
import com.pmt.entity.Utilisateur;
import com.pmt.exception.RessourceIntrouvableException;
import com.pmt.mapper.HistoriqueMapper;
import com.pmt.mapper.TacheMapper;
import com.pmt.repository.HistoriqueTacheRepository;
import com.pmt.repository.ProjetRepository;
import com.pmt.repository.TacheRepository;
import com.pmt.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Gestion des tâches d'un projet et de leur historique de modifications.
 */
@Service
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ProjetRepository projetRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueTacheRepository historiqueTacheRepository;
    private final ProjetService projetService;
    private final NotificationService notificationService;
    private final TacheMapper tacheMapper;
    private final HistoriqueMapper historiqueMapper;

    public TacheService(TacheRepository tacheRepository,
                        ProjetRepository projetRepository,
                        UtilisateurRepository utilisateurRepository,
                        HistoriqueTacheRepository historiqueTacheRepository,
                        ProjetService projetService,
                        NotificationService notificationService,
                        TacheMapper tacheMapper,
                        HistoriqueMapper historiqueMapper) {
        this.tacheRepository = tacheRepository;
        this.projetRepository = projetRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.historiqueTacheRepository = historiqueTacheRepository;
        this.projetService = projetService;
        this.notificationService = notificationService;
        this.tacheMapper = tacheMapper;
        this.historiqueMapper = historiqueMapper;
    }

    /**
     * Cree une tache dans un projet. Reserve aux administrateurs et aux membres
     *
     * @param projetId projet qui accueille la tache
     * @param requete  donnees de la tache
     * @param auteurId utilisateur qui cree la tache
     * @return la tache creee
     */
    @Transactional
    public TacheDto creerTache(Long projetId, TacheRequest requete, Long auteurId) {
        projetService.verifierRole(projetId, auteurId, Role.ADMIN, Role.MEMBRE);

        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Projet introuvable : " + projetId));
        Utilisateur auteur = recupererUtilisateur(auteurId);

        Tache tache = new Tache();
        tache.setProjet(projet);
        tache.setCreateur(auteur);
        tache.setNom(requete.nom());
        tache.setDescription(requete.description());
        tache.setDateEcheance(requete.dateEcheance());
        tache.setDateFin(requete.dateFin());
        if (requete.priorite() != null) {
            tache.setPriorite(requete.priorite());
        }
        if (requete.statut() != null) {
            tache.setStatut(requete.statut());
        }

        Utilisateur assigne = null;
        if (requete.assigneEmail() != null) {
            assigne = recupererMembreDuProjet(projetId, requete.assigneEmail());
            tache.setAssigne(assigne);
        }

        Tache sauvegardee = tacheRepository.save(tache);

        if (assigne != null) {
            notificationService.notifierAssignation(sauvegardee, assigne);
        }

        return tacheMapper.versDto(sauvegardee);
    }

    /**
     * @param tacheId  tache a modifier
     * @param requete  nouvelles valeurs
     * @param auteurId utilisateur qui effectue la modification
     * @return la tache mise à jour
     */
    @Transactional
    public TacheDto mettreAJourTache(Long tacheId, TacheRequest requete, Long auteurId) {
        Tache tache = recupererTache(tacheId);
        projetService.verifierRole(tache.getProjet().getId(), auteurId, Role.ADMIN, Role.MEMBRE);

        Utilisateur auteur = recupererUtilisateur(auteurId);
        List<HistoriqueTache> modifications = new ArrayList<>();

        tracer(modifications, tache, auteur, ChampTache.NOM,
                tache.getNom(), requete.nom(), tache::setNom);

        tracer(modifications, tache, auteur, ChampTache.DESCRIPTION,
                tache.getDescription(), requete.description(), tache::setDescription);

        tracerValeur(modifications, tache, auteur, ChampTache.DATE_ECHEANCE,
                tache.getDateEcheance(), requete.dateEcheance(), tache::setDateEcheance);

        tracerValeur(modifications, tache, auteur, ChampTache.DATE_FIN,
                tache.getDateFin(), requete.dateFin(), tache::setDateFin);

        tracerValeur(modifications, tache, auteur, ChampTache.PRIORITE,
                tache.getPriorite(), requete.priorite(), tache::setPriorite);

        tracerValeur(modifications, tache, auteur, ChampTache.STATUT,
                tache.getStatut(), requete.statut(), tache::setStatut);

        Utilisateur nouvelAssigne = null;
        if (requete.assigneEmail() != null) {
            nouvelAssigne = recupererMembreDuProjet(
                    tache.getProjet().getId(), requete.assigneEmail());

            Utilisateur ancienAssigne = tache.getAssigne();
            if (ancienAssigne == null || !ancienAssigne.getId().equals(nouvelAssigne.getId())) {
                modifications.add(new HistoriqueTache(tache, auteur, ChampTache.ASSIGNE,
                        ancienAssigne == null ? null : ancienAssigne.getEmail(),
                        nouvelAssigne.getEmail()));
                tache.setAssigne(nouvelAssigne);
            } else {
                nouvelAssigne = null;
            }
        }

        Tache sauvegardee = tacheRepository.save(tache);
        historiqueTacheRepository.saveAll(modifications);

        if (nouvelAssigne != null) {
            notificationService.notifierAssignation(sauvegardee, nouvelAssigne);
        }

        return tacheMapper.versDto(sauvegardee);
    }

    /**
     *
     * @param tacheId       tache demandee
     * @param utilisateurId utilisateur qui consulte
     * @return la tache
     */
    @Transactional(readOnly = true)
    public TacheDto consulterTache(Long tacheId, Long utilisateurId) {
        Tache tache = recupererTache(tacheId);
        projetService.verifierRole(tache.getProjet().getId(), utilisateurId,
                Role.ADMIN, Role.MEMBRE, Role.OBSERVATEUR);

        return tacheMapper.versDto(tache);
    }

    /**
     *
     * @param projetId projet concerne
     * @param statut statut à filtrer, null pour toutes les taches
     * @param utilisateurId utilisateur qui consulte
     * @return les taches correspondantes
     */
    @Transactional(readOnly = true)
    public List<TacheDto> listerTaches(Long projetId, StatutTache statut, Long utilisateurId) {
        projetService.verifierRole(projetId, utilisateurId,
                Role.ADMIN, Role.MEMBRE, Role.OBSERVATEUR);

        List<Tache> taches = statut == null
                ? tacheRepository.findByProjetId(projetId)
                : tacheRepository.findByProjetIdAndStatut(projetId, statut);

        return taches.stream().map(tacheMapper::versDto).toList();
    }

    /**
     * @param tacheId       tache concernee
     * @param utilisateurId utilisateur qui consulte
     * @return les modifications tracees
     */
    @Transactional(readOnly = true)
    public List<HistoriqueDto> listerHistorique(Long tacheId, Long utilisateurId) {
        Tache tache = recupererTache(tacheId);
        projetService.verifierRole(tache.getProjet().getId(), utilisateurId,
                Role.ADMIN, Role.MEMBRE, Role.OBSERVATEUR);

        return historiqueTacheRepository.findByTacheIdOrderByDateModifDesc(tacheId)
                .stream()
                .map(historiqueMapper::versDto)
                .toList();
    }

    /**
     * Compare deux valeurs de type texte si elles sont différentes
     * on applique la nouvelle valeur et ajoute une ligne d'historique
     */
    private void tracer(List<HistoriqueTache> modifications,
                        Tache tache, Utilisateur auteur,
                        String nomChamp,
                        String ancienne,
                        String nouvelle,
                        Consumer<String> setter) {
        if (nouvelle == null || Objects.equals(ancienne, nouvelle)) {
            return;
        }
        modifications.add(new HistoriqueTache(tache, auteur, nomChamp, ancienne, nouvelle));
        setter.accept(nouvelle);
    }

    /**
     * Variante générique pour les champs non textuels (dates, enums)
     * Les valeurs sont converties en chaine pour être stockées dans l'historique
     */
    private <T> void tracerValeur(List<HistoriqueTache> modifications, Tache tache,
                                  Utilisateur auteur, String nomChamp, T ancienne, T nouvelle,
                                  Consumer<T> setter) {
        if (nouvelle == null || Objects.equals(ancienne, nouvelle)) {
            return;
        }
        modifications.add(new HistoriqueTache(tache, auteur, nomChamp,
                ancienne == null ? null : String.valueOf(ancienne),
                String.valueOf(nouvelle)));
        setter.accept(nouvelle);
    }

    /** Charge une tache ou leve une exception si elle n'existe pas */
    private Tache recupererTache(Long tacheId) {
        return tacheRepository.findById(tacheId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Tache introuvable : " + tacheId));
    }

    /** Charge un utilisateur ou léve une exception s'il n'existe pas */
    private Utilisateur recupererUtilisateur(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Utilisateur introuvable : " + utilisateurId));
    }

    /**
     * Charge un utilisateur par email et verifie qu'il est bien membre du projet
     * On ne peut assigner une tâche qu'à un membre du projet concerne
     */
    private Utilisateur recupererMembreDuProjet(Long projetId, String email) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun utilisateur inscrit avec l'email " + email));

        projetService.verifierRole(projetId, utilisateur.getId(),
                Role.ADMIN, Role.MEMBRE, Role.OBSERVATEUR);

        return utilisateur;
    }
}
