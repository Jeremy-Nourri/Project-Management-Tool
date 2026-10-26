import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth';

@Component({
  selector: 'app-inscription',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './inscription.html',
})
export class Inscription {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly erreur = signal<string | null>(null);
  protected readonly enCours = signal(false);

  protected readonly formulaire = this.fb.nonNullable.group({
    nom: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    email: ['', [Validators.required, Validators.email]],
    motDePasse: ['', [Validators.required, Validators.minLength(8)]],
  });

  protected soumettre(): void {
    if (this.formulaire.invalid) {
      this.formulaire.markAllAsTouched();
      return;
    }

    this.enCours.set(true);
    this.erreur.set(null);

    this.auth.inscrire(this.formulaire.getRawValue()).subscribe({
      next: () => this.router.navigate(['/connexion']),
      error: (reponse) => {
        this.erreur.set(
          reponse.status === 409
            ? 'Un compte existe déja avec cet email.'
            : "L'inscription a échoué."
        );
        this.enCours.set(false);
      },
    });
  }
}
