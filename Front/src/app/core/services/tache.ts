import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Historique, StatutTache, Tache, TacheRequest } from '../models/tache';

@Injectable({ providedIn: 'root' })
export class TacheService {
  private readonly http = inject(HttpClient);
  private readonly url = environment.apiUrl;

  lister(projetId: number, statut?: StatutTache): Observable<Tache[]> {
    const params = statut ? new HttpParams().set('statut', statut) : undefined;
    return this.http.get<Tache[]>(`${this.url}/projets/${projetId}/taches`, { params });
  }

  consulter(tacheId: number): Observable<Tache> {
    return this.http.get<Tache>(`${this.url}/taches/${tacheId}`);
  }

  creer(projetId: number, requete: TacheRequest): Observable<Tache> {
    return this.http.post<Tache>(`${this.url}/projets/${projetId}/taches`, requete);
  }

  mettreAJour(tacheId: number, requete: TacheRequest): Observable<Tache> {
    return this.http.put<Tache>(`${this.url}/taches/${tacheId}`, requete);
  }

  listerHistorique(tacheId: number): Observable<Historique[]> {
    return this.http.get<Historique[]>(`${this.url}/taches/${tacheId}/historique`);
  }
}
