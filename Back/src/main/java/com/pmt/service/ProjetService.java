package com.pmt.service;

import com.pmt.dto.MembreDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion des projets et de leurs membres
 * Porte-les règles d'autorisation liées au role sur un projet
 */
@Service
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final ProjetMembreRepository projetMembreRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ProjetMapper projetMapper;

    public ProjetService(ProjetRepository projetRepository,
                         ProjetMembreRepository projetMembreRepository,
                         UtilisateurRepository utilisateurRepository,
                         ProjetMapper projetMapper) {
        this.projetRepository = projetRepository;
        this.projetMembreRepository = projetMembreRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.projetMapper = projetMapper;
    }

    /**
     * Crée un projet et inscrit son createur comme administrateur
     *
     * @param requete nom, description et date de debut
     * @param createurId identifiant de l'utilisateur qui créé le projet
     * @return le projet créé
     * @throws RessourceIntrouvableException si le créateur n'existe pas
     */
    @Transactional
    public ProjetDto creerProjet(ProjetRequest requete, Long createurId) {
        Utilisateur createur = recupererUtilisateur(createurId);

        Projet projet = new Projet();
        projet.setNom(requete.nom());
        projet.setDescription(requete.description());
        projet.setDateDebut(requete.dateDebut());
        projet.setCreateur(createur);

        Projet sauvegarde = projetRepository.save(projet);

        // Le createur devient automatiquement administrateur du projet.
        projetMembreRepository.save(new ProjetMembre(sauvegarde, createur, Role.ADMIN));

        return projetMapper.versDto(sauvegarde);
    }


    /**
     * Vérifie que l'utilisateur détient l'un des roles attendus sur le projet
     * appelé au debut de chaque methode qui modifie un projet ou ses taches
     *
     * @param projetId projet concerne
     * @param utilisateurId  utilisateur dont on vérifie le role
     * @param rolesAutorises roles accepte pour l'action
     * @return le role effectivement détenu par l'utilisateur
     * @throws AccesRefuseException si l'utilisateur n'est pas membre du projet,
     *                              ou si son role n'est pas dans la liste
     */
    @Transactional(readOnly = true)
    public Role verifierRole(Long projetId, Long utilisateurId, Role... rolesAutorises) {
        ProjetMembre membre = projetMembreRepository
                .findByIdProjetIdAndIdUtilisateurId(projetId, utilisateurId)
                .orElseThrow(() -> new AccesRefuseException(
                        "Vous n'etes pas membre de ce projet"));

        if (!List.of(rolesAutorises).contains(membre.getRole())) {
            throw new AccesRefuseException(
                    "Votre role (" + membre.getRole() + ") ne permet pas cette action");
        }

        return membre.getRole();
    }

    /**
     * Invite un utilisateur sur un projet en lui attribuant un role.
     * Reserve a l'administrateur du projet.
     *
     * @param projetId projet concerne
     * @param requete email de l'invite et role à lui donner
     * @param demandeurId utilisateur qui realise l'invitation
     * @return le membre ajoute
     * @throws AccesRefuseException si le demandeur n'est pas administrateur
     * @throws RessourceIntrouvableException si le projet ou l'invite n'existe pas
     * @throws ConflitException si l'utilisateur est deja membre
     */
    @Transactional
    public MembreDto inviterMembre(Long projetId, MembreRequest requete, Long demandeurId) {
        verifierRole(projetId, demandeurId, Role.ADMIN);

        Projet projet = recupererProjet(projetId);
        Utilisateur invite = recupererUtilisateurParEmail(requete.email());

        if (projetMembreRepository.existsByIdProjetIdAndIdUtilisateurId(projetId, invite.getId())) {
            throw new ConflitException(
                    requete.email() + " est deja membre de ce projet");
        }

        ProjetMembre membre = projetMembreRepository
                .save(new ProjetMembre(projet, invite, requete.role()));

        return projetMapper.versMembreDto(membre);
    }

    /**
     * Modifie le role d'un membre existant. Reserve a l'administrateur du projet.
     *
     * @param projetId projet concerne
     * @param requete email du membre et nouveau role
     * @param demandeurId utilisateur qui realise la modification
     * @return le membre mis a jour
     * @throws AccesRefuseException si le demandeur n'est pas administrateur
     * @throws RessourceIntrouvableException si le membre n'existe pas sur ce projet
     */
    @Transactional
    public MembreDto attribuerRole(Long projetId, MembreRequest requete, Long demandeurId) {
        verifierRole(projetId, demandeurId, Role.ADMIN);

        Utilisateur cible = recupererUtilisateurParEmail(requete.email());

        ProjetMembre membre = projetMembreRepository
                .findByIdProjetIdAndIdUtilisateurId(projetId, cible.getId())
                .orElseThrow(() -> new RessourceIntrouvableException(
                        requete.email() + " n'est pas membre de ce projet"));

        // Un projet doit toujours conserver au moins un administrateur,
        if (membre.getRole() == Role.ADMIN && requete.role() != Role.ADMIN
                && compterAdministrateurs(projetId) == 1) {
            throw new ConflitException(
                    "Impossible de retirer le dernier administrateur du projet");
        }

        membre.setRole(requete.role());

        return projetMapper.versMembreDto(projetMembreRepository.save(membre));
    }

    /**
     * Liste les projets dont l'utilisateur est membre, quel que soit son role
     *
     * @param utilisateurId utilisateur concerne
     * @return les projets, du plus recent au plus ancien
     */
    @Transactional(readOnly = true)
    public List<ProjetDto> listerProjetsDeLUtilisateur(Long utilisateurId) {
        return projetRepository.findProjetsDeLUtilisateur(utilisateurId)
                .stream()
                .map(projetMapper::versDto)
                .toList();
    }

    /**
     * Retourne le detail d'un projet
     * Accessible à tout membre, quel que soit son role
     *
     * @param projetId projet demande
     * @param utilisateurId utilisateur qui consulte
     * @return le projet
     * @throws AccesRefuseException si l'utilisateur n'est pas membre
     */
    @Transactional(readOnly = true)
    public ProjetDto consulterProjet(Long projetId, Long utilisateurId) {
        verifierRole(projetId, utilisateurId, Role.ADMIN, Role.MEMBRE, Role.OBSERVATEUR);

        return projetMapper.versDto(recupererProjet(projetId));
    }

    /**
     * Liste les membres d'un projet et leurs roles.
     *
     * @param projetId      projet concerne
     * @param utilisateurId utilisateur qui consulte
     * @return les membres du projet
     * @throws AccesRefuseException si l'utilisateur n'est pas membre
     */
    @Transactional(readOnly = true)
    public List<MembreDto> listerMembres(Long projetId, Long utilisateurId) {
        verifierRole(projetId, utilisateurId, Role.ADMIN, Role.MEMBRE, Role.OBSERVATEUR);

        return projetMembreRepository.findByIdProjetId(projetId)
                .stream()
                .map(projetMapper::versMembreDto)
                .toList();
    }


    private Projet recupererProjet(Long projetId) {
        return projetRepository.findById(projetId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Projet introuvable : " + projetId));
    }

    private Utilisateur recupererUtilisateurParEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Aucun utilisateur inscrit avec l'email " + email));
    }

    private long compterAdministrateurs(Long projetId) {
        return projetMembreRepository.findByIdProjetId(projetId)
                .stream()
                .filter(membre -> membre.getRole() == Role.ADMIN)
                .count();
    }

    private Utilisateur recupererUtilisateur(Long utilisateurId) {
        return utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new RessourceIntrouvableException(
                        "Utilisateur introuvable : " + utilisateurId));
    }
}
