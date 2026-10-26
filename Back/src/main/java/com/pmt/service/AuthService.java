package com.pmt.service;

import com.pmt.dto.ConnexionRequest;
import com.pmt.dto.InscriptionRequest;
import com.pmt.dto.UtilisateurDto;
import com.pmt.entity.Utilisateur;
import com.pmt.exception.ConflitException;
import com.pmt.exception.RessourceIntrouvableException;
import com.pmt.mapper.UtilisateurMapper;
import com.pmt.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Inscription et connexion des utilisateurs. */
@Service
public class AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurMapper utilisateurMapper;

    public AuthService(UtilisateurRepository utilisateurRepository,
                       PasswordEncoder passwordEncoder,
                       UtilisateurMapper utilisateurMapper) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.utilisateurMapper = utilisateurMapper;
    }

    /**
     * Cree un compte utilisateur.
     *
     * @param requete nom, email et mot de passe en clair
     * @return l'utilisateur cree, sans son mot de passe
     * @throws ConflitException si l'email est deja utilise
     */
    @Transactional
    public UtilisateurDto inscrire(InscriptionRequest requete) {
        if (utilisateurRepository.existsByEmail(requete.email())) {
            throw new ConflitException("Un compte existe deja avec l'email " + requete.email());
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(requete.nom());
        utilisateur.setEmail(requete.email());
        utilisateur.setMotDePasse(passwordEncoder.encode(requete.motDePasse()));

        return utilisateurMapper.versDto(utilisateurRepository.save(utilisateur));
    }

    /**
     * Verifie les identifiants et retourne l'utilisateur correspondant.
     *
     * @param requete email et mot de passe en clair
     * @return l'utilisateur authentifie, sans son mot de passe
     * @throws RessourceIntrouvableException si l'email est inconnu ou le mot de passe incorrect
     */
    @Transactional(readOnly = true)
    public UtilisateurDto connecter(ConnexionRequest requete) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(requete.email())
                .orElseThrow(() -> new RessourceIntrouvableException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(requete.motDePasse(), utilisateur.getMotDePasse())) {
            throw new RessourceIntrouvableException("Email ou mot de passe incorrect");
        }

        return utilisateurMapper.versDto(utilisateur);
    }
}
