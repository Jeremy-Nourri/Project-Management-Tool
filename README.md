# Project Management Tool (PMT)

Plateforme de gestion de projet collaboratif pour équipes de développement :
création de projets, gestion des membres et de leurs rôles, suivi des tâches
et historique des modifications.

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA |
| Base de données | MySQL 8 |
| Frontend | Angular 22, TypeScript |
| Tests | JUnit 5, Mockito, Testcontainers, JaCoCo, Vitest |
| CI/CD | GitHub Actions, Docker Hub |

## Structure du dépôt

```
.
├── Back/               backend Spring Boot (+ Dockerfile)
├── Front/              frontend Angular (+ Dockerfile)
├── Database/           schema.sql : structure et données de test
├── .github/workflows/  pipeline CI/CD
└── docker-compose.yml  orchestration complète
```

## Démarrage rapide

Prérequis : Docker et Docker Compose.

```bash
git clone https://github.com/Jeremy-Nourri/Project-Management-Tool.git
cd "Project Management"
cp .env.example .env        # adapter si besoin
docker compose up --build
```

| Service | URL |
|---|---|
| Application | http://localhost:4200 |
| API (Swagger) | http://localhost:8080/swagger-ui.html |
| MailHog (notifications) | http://localhost:8025 |
| MySQL | localhost:3307 |

Le script `Database/schema.sql` est exécuté automatiquement au premier
démarrage : la base contient alors 4 utilisateurs, 2 projets et 6 tâches de test.

Arrêt : `docker compose down` (`-v` pour effacer aussi les données).

## Déploiement depuis Docker Hub

Les images sont publiées à chaque merge sur `main` :

```bash
docker pull <DOCKERHUB_USER>/pmt-backend:latest
docker pull <DOCKERHUB_USER>/pmt-frontend:latest
```

Pour déployer sans recompiler, renseigner `DOCKERHUB_USER` dans `.env`,
retirer la section `build:` du service `backend` dans `docker-compose.yml`,
puis :

```bash
docker compose pull && docker compose up -d
```

## Configuration

Toutes les variables ont une valeur par défaut adaptée au développement local.
Voir `.env.example`.

| Variable | Défaut | Rôle |
|---|---|---|
| `DB_NAME` / `DB_ROOT_PASSWORD` / `DB_PORT` | `pmt` / `root` / `3307` | base de données |
| `BACKEND_PORT` / `FRONTEND_PORT` | `8080` / `4200` | ports publiés |
| `CORS_ORIGINS` | `http://localhost:4200` | origines autorisées |
| `DOCKERHUB_USER` / `TAG` | — / `latest` | image à déployer |

> MailHog est un serveur SMTP de développement : il capture les notifications
> au lieu de les envoyer. En production, `MAIL_HOST` et `MAIL_PORT` pointent
> vers un vrai relais.

## Développement sans Docker

```bash
# base de données
mysql -u root -p -e "CREATE DATABASE pmt CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p pmt -e "source Database/schema.sql"

# backend
cd Back && mvn spring-boot:run

# frontend
cd Front && npm install && npm start
```

## Tests

```bash
cd Back  && mvn verify        # 39 tests  (JUnit, Mockito, Testcontainers)
cd Front && npm run test:ci   # 58 tests  (Vitest)
```

Côté backend, Docker doit être démarré : les tests d'intégration lancent un
conteneur MySQL (Testcontainers) qui exécute `Database/schema.sql`, validant
ainsi le mapping JPA contre le schéma livré.

| Rapport | Emplacement | Couverture |
|---|---|---|
| Backend (JaCoCo) | `Back/target/site/jacoco/index.html` | 75,7 % instructions / 69,0 % branches |
| Frontend (Vitest) | `Front/coverage/index.html` | 93,4 % instructions / 89,9 % branches |

Les deux builds échouent sous **60 %** d'instructions ou de branches.

## Pipeline CI/CD

Déclenchement : push et pull request sur `main`.

| Job | Quand | Rôle |
|---|---|---|
| `backend` | push et PR | compile, teste, vérifie la couverture, publie le rapport |
| `frontend` | push et PR | teste, vérifie la couverture, build de production |
| `deploiement` | push sur `main` uniquement | construit les deux images et les pousse sur Docker Hub |

Le déploiement dépend du succès des **deux** jobs de test : aucune image n'est
publiée si un test échoue ou si la couverture passe sous le seuil. Les deux
images sont construites en parallèle via une matrice.

Secrets requis : `DOCKERHUB_USERNAME`, `DOCKERHUB_TOKEN`.

## API

Documentation interactive : `/swagger-ui.html` — spécification OpenAPI : `/v3/api-docs`.

Hors `/api/auth`, les routes attendent l'en-tête `Authorization` portant
l'identifiant retourné par `/api/auth/connexion`.

> L'énoncé dispense de la sécurisation du backend : cette identification n'est
> pas un mécanisme d'authentification sécurisé. Son extraction étant isolée
> dans les contrôleurs, un passage à JWT n'impacterait pas la couche service.

### Rôles et permissions

| Action | Admin | Membre | Observateur |
|---|:---:|:---:|:---:|
| Ajouter un membre, attribuer un rôle | X | | |
| Créer, assigner, mettre à jour une tâche | X | X | |
| Consulter tâches, tableau de bord, historique | X | X | X |
