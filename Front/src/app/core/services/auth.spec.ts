import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { AuthService } from './auth';
import { Utilisateur } from '../models/utilisateur';

const ALICE: Utilisateur = {
  id: 1,
  nom: 'Alice Martin',
  email: 'alice@codesolutions.fr',
  dateCreation: '2026-01-15T09:00:00',
};

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('démarre sans utilisateur connecté', () => {
    expect(service.estConnecte()).toBe(false);
    expect(service.identifiant()).toBeNull();
  });

  it('enregistre utilisateur après connexion', () => {
    service.connecter({ email: ALICE.email, motDePasse: 'password' }).subscribe();

    http.expectOne('http://localhost:8080/api/auth/connexion').flush(ALICE);

    expect(service.estConnecte()).toBe(true);
    expect(service.utilisateur()?.nom).toBe('Alice Martin');
    expect(service.identifiant()).toBe(1);
  });

  it('persiste utilisateur dans le localStorage', () => {
    service.connecter({ email: ALICE.email, motDePasse: 'password' }).subscribe();
    http.expectOne('http://localhost:8080/api/auth/connexion').flush(ALICE);

    expect(localStorage.getItem('pmt.utilisateur')).toContain('alice@codesolutions.fr');
  });

  it('vide état à la déconnexion', () => {
    service.connecter({ email: ALICE.email, motDePasse: 'password' }).subscribe();
    http.expectOne('http://localhost:8080/api/auth/connexion').flush(ALICE);

    service.deconnecter();

    expect(service.estConnecte()).toBe(false);
    expect(localStorage.getItem('pmt.utilisateur')).toBeNull();
  });

  it('envoie les données inscription', () => {
    const requete = { nom: 'Alice Martin', email: ALICE.email, motDePasse: 'password' };
    service.inscrire(requete).subscribe();

    const appel = http.expectOne('http://localhost:8080/api/auth/inscription');
    expect(appel.request.method).toBe('POST');
    expect(appel.request.body).toEqual(requete);
    appel.flush(ALICE);
  });

  it('ignore un localStorage corrompu', () => {
    localStorage.setItem('pmt.utilisateur', 'ceci-n-est-pas-du-json');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    expect(TestBed.inject(AuthService).estConnecte()).toBe(false);
  });
});
