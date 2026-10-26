import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';
import { authGuard } from './auth-guard';
import { AuthService } from '../services/auth';

describe('authGuard', () => {
  let controleur: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    controleur = TestBed.inject(HttpTestingController);
  });

  it('redirige vers la connexion si personne est connecté', () => {
    const resultat = TestBed.runInInjectionContext(() =>
      authGuard({} as never, {} as never),
    );

    expect(resultat).toBeInstanceOf(UrlTree);
    expect(TestBed.inject(Router).serializeUrl(resultat as UrlTree)).toBe('/connexion');
  });

  it('autorise un accès quand un utilisateur est connecté', () => {
    const auth = TestBed.inject(AuthService);
    auth.connecter({ email: 'alice@codesolutions.fr', motDePasse: 'password' }).subscribe();
    controleur.expectOne('http://localhost:8080/api/auth/connexion').flush({
      id: 1,
      nom: 'Alice Martin',
      email: 'alice@codesolutions.fr',
      dateCreation: '2026-01-15T09:00:00',
    });

    const resultat = TestBed.runInInjectionContext(() =>
      authGuard({} as never, {} as never),
    );

    expect(resultat).toBe(true);
  });
});
