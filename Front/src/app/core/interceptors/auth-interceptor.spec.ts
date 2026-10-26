import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { authInterceptor } from './auth-interceptor';
import { AuthService } from '../services/auth';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controleur: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    controleur = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => controleur.verify());

  it("ajoute pas en-tete quand personne est connecté", () => {
    http.get('http://localhost:8080/api/projets').subscribe();

    const appel = controleur.expectOne('http://localhost:8080/api/projets');
    expect(appel.request.headers.has('Authorization')).toBe(false);
    appel.flush([]);
  });

  it("ajoute identifiant quand un utilisateur est connecté", () => {
    connecter();

    http.get('http://localhost:8080/api/projets').subscribe();

    const appel = controleur.expectOne('http://localhost:8080/api/projets');
    expect(appel.request.headers.get('Authorization')).toBe('7');
    appel.flush([]);
  });

  it("laisse passer les routes auth sans en-tête", () => {
    connecter();

    http.post('http://localhost:8080/api/auth/connexion', {}).subscribe();

    const appel = controleur.expectOne('http://localhost:8080/api/auth/connexion');
    expect(appel.request.headers.has('Authorization')).toBe(false);
    appel.flush({});
  });

  function connecter(): void {
    auth.connecter({ email: 'alice@codesolutions.fr', motDePasse: 'password' }).subscribe();
    controleur.expectOne('http://localhost:8080/api/auth/connexion').flush({
      id: 7,
      nom: 'Alice Martin',
      email: 'alice@codesolutions.fr',
      dateCreation: '2026-01-15T09:00:00',
    });
  }
});
