package com.pmt.repository;

import com.pmt.AbstractIntegrationTest;
import com.pmt.entity.Utilisateur;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UtilisateurRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void doitPersisterUnUtilisateurEtRemplirLaDateDeCreation() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom("Test User");
        utilisateur.setEmail("test.user@codesolutions.fr");
        utilisateur.setMotDePasse("$2a$10$hashfactice");

        Utilisateur sauvegarde = utilisateurRepository.save(utilisateur);

        assertThat(sauvegarde.getId()).isNotNull();
        assertThat(sauvegarde.getDateCreation()).isNotNull();
    }

    @Test
    void doitRetrouverUnUtilisateurParEmail() {
        Optional<Utilisateur> trouve = utilisateurRepository.findByEmail("alice@codesolutions.fr");

        assertThat(trouve).isPresent();
        assertThat(trouve.get().getNom()).isEqualTo("Alice Martin");
    }

    @Test
    void doitIndiquerQuUnEmailExisteDeja() {
        assertThat(utilisateurRepository.existsByEmail("bruno@codesolutions.fr")).isTrue();
        assertThat(utilisateurRepository.existsByEmail("inconnu@codesolutions.fr")).isFalse();
    }
}
