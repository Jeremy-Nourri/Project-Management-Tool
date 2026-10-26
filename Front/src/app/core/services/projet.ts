import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Membre, MembreRequest, Projet, ProjetRequest } from '../models/projet';

@Injectable({ providedIn: 'root' })
export class ProjetService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiUrl}/projets`;

  listerMesProjets(): Observable<Projet[]> {
    return this.http.get<Projet[]>(this.url);
  }

  consulter(projetId: number): Observable<Projet> {
    return this.http.get<Projet>(`${this.url}/${projetId}`);
  }

  creer(requete: ProjetRequest): Observable<Projet> {
    return this.http.post<Projet>(this.url, requete);
  }

  listerMembres(projetId: number): Observable<Membre[]> {
    return this.http.get<Membre[]>(`${this.url}/${projetId}/membres`);
  }

  inviterMembre(projetId: number, requete: MembreRequest): Observable<Membre> {
    return this.http.post<Membre>(`${this.url}/${projetId}/membres`, requete);
  }

  attribuerRole(projetId: number, requete: MembreRequest): Observable<Membre> {
    return this.http.put<Membre>(`${this.url}/${projetId}/membres`, requete);
  }
}
