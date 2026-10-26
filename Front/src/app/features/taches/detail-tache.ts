import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  Historique,
  LIBELLES_PRIORITE,
  LIBELLES_STATUT,
  PRIORITES,
  STATUTS,
  Tache,
} from '../../core/models/tache';
import { TacheService } from '../../core/services/tache';

@Component({
  selector: 'app-detail-tache',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './detail-tache.html',
})
export class DetailTache implements OnInit {
  readonly id = input.required<string>();

  private readonly fb = inject(FormBuilder);
  private readonly tacheService = inject(TacheService);

  protected readonly statuts = STATUTS;
  protected readonly priorites = PRIORITES;
  protected readonly libellesStatut = LIBELLES_STATUT;
  protected readonly libellesPriorite = LIBELLES_PRIORITE;

  protected readonly tache = signal<Tache | null>(null);
  protected readonly historique = signal<Historique[]>([]);
  protected readonly erreur = signal<string | null>(null);
  protected readonly modeEdition = signal(false);

  protected readonly formulaire = this.fb.nonNullable.group({
    nom: ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    dateEcheance: [''],
    dateFin: [''],
    priorite: ['MOYENNE'],
    statut: ['A_FAIRE'],
  });

  ngOnInit(): void {
    this.charger();
  }

  protected charger(): void {
    const tacheId = Number(this.id());

    this.tacheService.consulter(tacheId).subscribe({
      next: (tache) => {
        this.tache.set(tache);
        this.formulaire.patchValue({
          nom: tache.nom,
          description: tache.description ?? '',
          dateEcheance: tache.dateEcheance ?? '',
          dateFin: tache.dateFin ?? '',
          priorite: tache.priorite,
          statut: tache.statut,
        });
      },
      error: () => this.erreur.set('Tache inaccessible.'),
    });

    this.tacheService.listerHistorique(tacheId).subscribe({
      next: (lignes) => this.historique.set(lignes),
    });
  }

  protected basculerEdition(): void {
    this.modeEdition.update((edition) => !edition);
  }

  protected enregistrer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    const valeurs = this.formulaire.getRawValue();

    this.tacheService
      .mettreAJour(Number(this.id()), {
        nom: valeurs.nom,
        description: valeurs.description || null,
        dateEcheance: valeurs.dateEcheance || null,
        dateFin: valeurs.dateFin || null,
        priorite: valeurs.priorite as 'BASSE' | 'MOYENNE' | 'HAUTE',
        statut: valeurs.statut as 'A_FAIRE' | 'EN_COURS' | 'TERMINEE',
      })
      .subscribe({
        next: () => {
          this.modeEdition.set(false);
          this.erreur.set(null);
          this.charger();
        },
        error: (reponse) =>
          this.erreur.set(
            reponse.status === 403
              ? "Votre role ne permet pas de modifier cette tâche."
              : 'La mise a jour a échoué.'
          ),
      });
  }
}
