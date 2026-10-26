import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { Inscription } from './inscription';

const URL_INSCRIPTION = 'http://localhost:8080/api/auth/inscription';

describe('Inscription', () => {
  let fixture: ComponentFixture<Inscription>;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Inscription],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Inscription);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function composant(): any {
    return fixture.componentInstance as any;
  }

  function remplir(): void {
    composant().formulaire.setValue({
      nom: 'Alice Martin',
      email: 'alice@codesolutions.fr',
      motDePasse: 'password123',
    });
  }

  it('refuse un mot de passe trop court', () => {
    composant().formulaire.setValue({
      nom: 'Alice Martin',
      email: 'alice@codesolutions.fr',
      motDePasse: 'court',
    });

    expect(composant().formulaire.invalid).toBe(true);
    composant().soumettre();
    fixture.detectChanges();
    http.expectNone(URL_INSCRIPTION);
  });

  it('redirige vers la connexion après inscription', () => {
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    remplir();
    composant().soumettre();
    http.expectOne(URL_INSCRIPTION).flush({
      id: 1,
      nom: 'Alice Martin',
      email: 'alice@codesolutions.fr',
      dateCreation: '2026-01-15T09:00:00',
    });

    expect(navigate).toHaveBeenCalledWith(['/connexion']);
  });

  it('signale un email déjà utilisé', () => {
    remplir();
    composant().soumettre();
    http.expectOne(URL_INSCRIPTION).flush({}, { status: 409, statusText: 'Conflict' });

    fixture.detectChanges();
    expect(composant().erreur()).toContain('existe');
  });

  it('affiche un message générique sur les autres erreurs', () => {
    remplir();
    composant().soumettre();
    http.expectOne(URL_INSCRIPTION).flush({}, { status: 500, statusText: 'Error' });

    fixture.detectChanges();
    expect(composant().erreur()).toContain('inscription');
  });
});
