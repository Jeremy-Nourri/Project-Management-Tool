import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ConnexionRequest,
  InscriptionRequest,
  Utilisateur,
} from '../models/utilisateur';

const CLE_STOCKAGE = 'pmt.utilisateur';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/auth`;

  private readonly utilisateurCourant = signal<Utilisateur | null>(this.lireStockage());

  readonly utilisateur = this.utilisateurCourant.asReadonly();

  readonly estConnecte = computed(() => this.utilisateurCourant() !== null);

  inscrire(requete: InscriptionRequest): Observable<Utilisateur> {
    return this.http.post<Utilisateur>(`${this.url}/inscription`, requete);
  }

  connecter(requete: ConnexionRequest): Observable<Utilisateur> {
    return this.http
      .post<Utilisateur>(`${this.url}/connexion`, requete)
      .pipe(tap((utilisateur) => this.enregistrer(utilisateur)));
  }

  deconnecter(): void {
    this.utilisateurCourant.set(null);
    localStorage.removeItem(CLE_STOCKAGE);
  }

  identifiant(): number | null {
    return this.utilisateurCourant()?.id ?? null;
  }

  private enregistrer(utilisateur: Utilisateur): void {
    this.utilisateurCourant.set(utilisateur);
    localStorage.setItem(CLE_STOCKAGE, JSON.stringify(utilisateur));
  }

  private lireStockage(): Utilisateur | null {
    const brut = localStorage.getItem(CLE_STOCKAGE);
    if (!brut) {
      return null;
    }
    try {
      return JSON.parse(brut) as Utilisateur;
    } catch {
      localStorage.removeItem(CLE_STOCKAGE);
      return null;
    }
  }
}
