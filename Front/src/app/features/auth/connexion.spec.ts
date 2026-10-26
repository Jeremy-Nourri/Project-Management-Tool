import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { Connexion } from './connexion';

describe('Connexion', () => {
  let fixture: ComponentFixture<Connexion>;
  let http: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Connexion],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(Connexion);
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  function composant(): any {
    return fixture.componentInstance as any;
  }

  it('demarre avec un formulaire invalide', () => {
    expect(composant().formulaire.invalid).toBe(true);
  });

  it("n appelle pas l API si le formulaire est invalide", () => {
    composant().soumettre();

    http.expectNone('http://localhost:8080/api/auth/connexion');
    expect(composant().formulaire.touched).toBe(true);
  });

  it('redirige vers les projets apres une connexion reussie', () => {
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    composant().formulaire.setValue({
      email: 'alice@codesolutions.fr',
      motDePasse: 'password',
    });
    composant().soumettre();

    http.expectOne('http://localhost:8080/api/auth/connexion').flush({
      id: 1,
      nom: 'Alice Martin',
      email: 'alice@codesolutions.fr',
      dateCreation: '2026-01-15T09:00:00',
    });

    expect(navigate).toHaveBeenCalledWith(['/projets']);
  });

  it('affiche un message quand les identifiants sont refuses', () => {
    composant().formulaire.setValue({
      email: 'alice@codesolutions.fr',
      motDePasse: 'mauvais',
    });
    composant().soumettre();

    http
      .expectOne('http://localhost:8080/api/auth/connexion')
      .flush({}, { status: 404, statusText: 'Not Found' });

    fixture.detectChanges();
    expect(composant().erreur()).toContain('incorrect');
    expect(composant().enCours()).toBe(false);
  });
});
