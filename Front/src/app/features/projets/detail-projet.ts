import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Membre, Projet } from '../../core/models/projet';
import { LIBELLES_ROLE, Role, ROLES } from '../../core/models/role';
import {
  LIBELLES_PRIORITE,
  LIBELLES_STATUT,
  PRIORITES,
  STATUTS,
  StatutTache,
  Tache,
} from '../../core/models/tache';
import { AuthService } from '../../core/services/auth';
import { ProjetService } from '../../core/services/projet';
import { TacheService } from '../../core/services/tache';

@Component({
  selector: 'app-detail-projet',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './detail-projet.html',
})
export class DetailProjet implements OnInit {
  readonly id = input.required<string>();

  private readonly fb = inject(FormBuilder);
  private readonly projetService = inject(ProjetService);
  private readonly tacheService = inject(TacheService);
  private readonly auth = inject(AuthService);

  protected readonly roles = ROLES;
  protected readonly statuts = STATUTS;
  protected readonly priorites = PRIORITES;
  protected readonly libellesRole = LIBELLES_ROLE;
  protected readonly libellesStatut = LIBELLES_STATUT;
  protected readonly libellesPriorite = LIBELLES_PRIORITE;

  protected readonly projet = signal<Projet | null>(null);
  protected readonly membres = signal<Membre[]>([]);
  protected readonly taches = signal<Tache[]>([]);
  protected readonly erreur = signal<string | null>(null);

  protected readonly monRole = computed<Role | null>(() => {
    const moi = this.auth.utilisateur();
    return this.membres().find((m) => m.utilisateur.id === moi?.id)?.role ?? null;
  });

  protected readonly estAdmin = computed(() => this.monRole() === 'ADMIN');
  protected readonly peutModifier = computed(
    () => this.monRole() === 'ADMIN' || this.monRole() === 'MEMBRE'
  );

  protected readonly formulaireMembre = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    role: ['MEMBRE' as Role, [Validators.required]],
  });

  protected readonly formulaireTache = this.fb.nonNullable.group({
    nom: ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    dateEcheance: [''],
    priorite: ['MOYENNE'],
    assigneEmail: [''],
  });

  ngOnInit(): void {
    this.charger();
  }

  protected tachesParStatut(statut: StatutTache): Tache[] {
    return this.taches().filter((tache) => tache.statut === statut);
  }

  protected charger(): void {
    const projetId = Number(this.id());

    this.projetService.consulter(projetId).subscribe({
      next: (projet) => this.projet.set(projet),
      error: () => this.erreur.set('Projet inaccessible.'),
    });
    this.projetService.listerMembres(projetId).subscribe({
      next: (membres) => this.membres.set(membres),
    });
    this.tacheService.lister(projetId).subscribe({
      next: (taches) => this.taches.set(taches),
    });
  }

  protected inviter(): void {
    if (this.formulaireMembre.invalid) {
      this.formulaireMembre.markAllAsTouched();
      return;
    }

    this.projetService
      .inviterMembre(Number(this.id()), this.formulaireMembre.getRawValue())
      .subscribe({
        next: (membre) => {
          this.membres.update((liste) => [...liste, membre]);
          this.formulaireMembre.reset({ role: 'MEMBRE' });
          this.erreur.set(null);
        },
        error: (reponse) =>
          this.erreur.set(
            reponse.status === 404
              ? 'Aucun compte avec cet email.'
              : reponse.status === 409
                ? 'Cet utilisateur est deja membre.'
                : "L'invitation a echoue."
          ),
      });
  }

  protected changerRole(membre: Membre, role: Role): void {
    this.projetService
      .attribuerRole(Number(this.id()), { email: membre.utilisateur.email, role })
      .subscribe({
        next: (misAJour) =>
          this.membres.update((liste) =>
            liste.map((m) =>
              m.utilisateur.id === misAJour.utilisateur.id ? misAJour : m
            )
          ),
        error: (reponse) =>
          this.erreur.set(
            reponse.status === 409
              ? 'Impossible de retirer le dernier administrateur.'
              : 'Le changement de role a echoue.'
          ),
      });
  }

  protected creerTache(): void {
    if (this.formulaireTache.invalid) {
      this.formulaireTache.markAllAsTouched();
      return;
    }

    const valeurs = this.formulaireTache.getRawValue();

    this.tacheService
      .creer(Number(this.id()), {
        nom: valeurs.nom,
        description: valeurs.description || null,
        dateEcheance: valeurs.dateEcheance || null,
        priorite: valeurs.priorite as 'BASSE' | 'MOYENNE' | 'HAUTE',
        assigneEmail: valeurs.assigneEmail || null,
      })
      .subscribe({
        next: (tache) => {
          this.taches.update((liste) => [...liste, tache]);
          this.formulaireTache.reset({ priorite: 'MOYENNE' });
          this.erreur.set(null);
        },
        error: () => this.erreur.set('La creation de la tache a echoue.'),
      });
  }
}
