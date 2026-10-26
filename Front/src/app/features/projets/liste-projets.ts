import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Projet } from '../../core/models/projet';
import { ProjetService } from '../../core/services/projet';

@Component({
  selector: 'app-liste-projets',
  imports: [ReactiveFormsModule, RouterLink, DatePipe],
  templateUrl: './liste-projets.html',
})
export class ListeProjets {
  private readonly fb = inject(FormBuilder);
  private readonly projetService = inject(ProjetService);

  protected readonly projets = signal<Projet[]>([]);
  protected readonly erreur = signal<string | null>(null);
  protected readonly formulaireVisible = signal(false);

  protected readonly formulaire = this.fb.nonNullable.group({
    nom: ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    dateDebut: [new Date().toISOString().slice(0, 10), [Validators.required]],
  });

  constructor() {
    this.charger();
  }

  protected charger(): void {
    this.projetService.listerMesProjets().subscribe({
      next: (projets) => this.projets.set(projets),
      error: () => this.erreur.set('Impossible de charger les projets.'),
    });
  }

  protected basculerFormulaire(): void {
    this.formulaireVisible.update((visible) => !visible);
  }

  protected creer(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    this.projetService.creer(this.formulaire.getRawValue()).subscribe({
      next: (projet) => {
        this.projets.update((liste) => [projet, ...liste]);
        this.formulaire.reset({ dateDebut: new Date().toISOString().slice(0, 10) });
        this.formulaireVisible.set(false);
      },
      error: () => this.erreur.set('La création du projet a échoué.'),
    });
  }
}
