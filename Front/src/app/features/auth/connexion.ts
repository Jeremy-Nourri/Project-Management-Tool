import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-connexion',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './connexion.html',
})
export class Connexion {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly erreur = signal<string | null>(null);
  protected readonly enCours = signal(false);

  protected readonly formulaire = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required]],
  });

  protected soumettre(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    this.enCours.set(true);
    this.erreur.set(null);

    this.auth.connecter(this.formulaire.getRawValue()).subscribe({
      next: () => this.router.navigate(['/projets']),
      error: () => {
        this.erreur.set('Email ou mot de passe incorrect.');
        this.enCours.set(false);
      },
    });
  }
}
