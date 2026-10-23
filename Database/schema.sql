DROP TABLE IF EXISTS historique_tache;
DROP TABLE IF EXISTS tache;
DROP TABLE IF EXISTS projet_membre;
DROP TABLE IF EXISTS projet;
DROP TABLE IF EXISTS utilisateur;

CREATE TABLE utilisateur (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    nom           VARCHAR(50)  NOT NULL,
    email         VARCHAR(255) NOT NULL,
    mot_de_passe  VARCHAR(100) NOT NULL,
    date_creation DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_utilisateur_email UNIQUE (email)
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE projet (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nom         VARCHAR(100) NOT NULL,
    description TEXT         NULL,
    date_debut  DATE         NOT NULL,
    createur_id BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_projet_createur FOREIGN KEY (createur_id)
        REFERENCES utilisateur (id) ON DELETE RESTRICT ON UPDATE CASCADE
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE projet_membre (
    projet_id      BIGINT      NOT NULL,
    utilisateur_id BIGINT      NOT NULL,
    role           VARCHAR(20) NOT NULL,
    date_ajout     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (projet_id, utilisateur_id),
    CONSTRAINT fk_membre_projet FOREIGN KEY (projet_id)
        REFERENCES projet (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_membre_utilisateur FOREIGN KEY (utilisateur_id)
        REFERENCES utilisateur (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT ck_membre_role CHECK (role IN ('ADMIN', 'MEMBRE', 'OBSERVATEUR'))
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_membre_utilisateur ON projet_membre (utilisateur_id);

CREATE TABLE tache (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nom            VARCHAR(100) NOT NULL,
    description    TEXT         NULL,
    date_echeance  DATE         NULL,
    date_fin       DATE         NULL,
    priorite       VARCHAR(20)  NOT NULL DEFAULT 'MOYENNE',
    statut         VARCHAR(20)  NOT NULL DEFAULT 'A_FAIRE',
    projet_id      BIGINT       NOT NULL,
    createur_id    BIGINT       NOT NULL,
    assigne_id     BIGINT       NULL,
    date_creation  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_tache_projet FOREIGN KEY (projet_id)
        REFERENCES projet (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_tache_createur FOREIGN KEY (createur_id)
        REFERENCES utilisateur (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_tache_assigne FOREIGN KEY (assigne_id)
        REFERENCES utilisateur (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT ck_tache_priorite CHECK (priorite IN ('BASSE', 'MOYENNE', 'HAUTE')),
    CONSTRAINT ck_tache_statut CHECK (statut IN ('A_FAIRE', 'EN_COURS', 'TERMINEE'))
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tache_projet_statut ON tache (projet_id, statut);
CREATE INDEX idx_tache_assigne ON tache (assigne_id);

CREATE TABLE historique_tache (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    tache_id         BIGINT      NOT NULL,
    auteur_id        BIGINT      NOT NULL,
    nom_champ        VARCHAR(50) NOT NULL,
    ancienne_valeur  TEXT        NULL,
    nouvelle_valeur  TEXT        NULL,
    date_modif       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_historique_tache FOREIGN KEY (tache_id)
        REFERENCES tache (id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_historique_auteur FOREIGN KEY (auteur_id)
        REFERENCES utilisateur (id) ON DELETE RESTRICT ON UPDATE CASCADE
) DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_historique_tache_date ON historique_tache (tache_id, date_modif);

INSERT INTO utilisateur (id, nom, email, mot_de_passe) VALUES
(1, 'Alice Martin',  'alice@codesolutions.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(2, 'Bruno Lefevre', 'bruno@codesolutions.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(3, 'Chloe Dubois',  'chloe@codesolutions.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(4, 'David Nguyen',  'david@codesolutions.fr', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');

INSERT INTO projet (id, nom, description, date_debut, createur_id) VALUES
(1, 'Project Management Tool', 'Plateforme de gestion de projet collaboratif', '2026-01-15', 1),
(2, 'Site Vitrine',            'Refonte du site vitrine de Code Solutions',    '2026-03-01', 2);

INSERT INTO projet_membre (projet_id, utilisateur_id, role) VALUES
(1, 1, 'ADMIN'),
(1, 2, 'MEMBRE'),
(1, 3, 'OBSERVATEUR'),
(2, 2, 'ADMIN'),
(2, 1, 'OBSERVATEUR'),
(2, 4, 'MEMBRE');

INSERT INTO tache (id, nom, description, date_echeance, date_fin, priorite, statut, projet_id, createur_id, assigne_id) VALUES
(1, 'Concevoir le schema de base de donnees', 'Modeliser les entites et generer le script SQL', '2026-01-25', '2026-01-24', 'HAUTE',   'TERMINEE', 1, 1, 1),
(2, 'Implementer l''authentification',        'Inscription et connexion des utilisateurs',      '2026-02-10', NULL,         'HAUTE',   'EN_COURS', 1, 1, 2),
(3, 'Developper le tableau de bord',          'Affichage des taches par statut',                '2026-02-28', NULL,         'MOYENNE', 'A_FAIRE',  1, 2, 3),
(4, 'Mettre en place la pipeline CI/CD',      NULL,                                             '2026-03-15', NULL,         'MOYENNE', 'A_FAIRE',  1, 1, NULL),
(5, 'Rediger les maquettes',                  'Maquettes des pages principales',                '2026-03-20', NULL,         'BASSE',   'EN_COURS', 2, 2, 4),
(6, 'Choisir la charte graphique',            NULL,                                             '2026-03-10', '2026-03-08', 'BASSE',   'TERMINEE', 2, 2, 2);

INSERT INTO historique_tache (tache_id, auteur_id, nom_champ, ancienne_valeur, nouvelle_valeur, date_modif) VALUES
(1, 1, 'statut',     'A_FAIRE',  'EN_COURS',   '2026-01-18 09:30:00'),
(1, 1, 'statut',     'EN_COURS', 'TERMINEE',   '2026-01-24 17:05:00'),
(1, 1, 'date_fin',   NULL,       '2026-01-24', '2026-01-24 17:05:00'),
(2, 1, 'assigne_id', NULL,       '2',          '2026-02-01 10:00:00'),
(2, 2, 'statut',     'A_FAIRE',  'EN_COURS',   '2026-02-03 08:45:00'),
(3, 2, 'priorite',   'BASSE',    'MOYENNE',    '2026-02-05 14:20:00'),
(5, 2, 'assigne_id', NULL,       '4',          '2026-03-02 11:10:00'),
(6, 2, 'statut',     'EN_COURS', 'TERMINEE',   '2026-03-08 16:00:00');
