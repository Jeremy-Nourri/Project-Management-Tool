import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ListeProjets } from './liste-projets';

const URL = 'http://localhost:8080/api/projets';

const PROJET = {
  id: 1,
  nom: 'PMT',
  description: 'Outil collaboratif',
  dateDebut: '2026-01-15',
  createur: {
    id: 1,
    nom: 'Alice Martin',
    email: 'alice@codesolutions.fr',
    dateCreation: '2026-01-15T09:00:00',
  },
};

describe('ListeProjets', () => {
  let fixture: ComponentFixture<ListeProjets>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [ListeProjets],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(ListeProjets);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  function composant(): any {
    return fixture.componentInstance as any;
  }

  it('charge les projets au demarrage', () => {
    http.expectOne(URL).flush([PROJET]);
    fixture.detectChanges();

    expect(composant().projets().length).toBe(1);
  });

  it('affiche une erreur si le chargement echoue', () => {
    http.expectOne(URL).flush({}, { status: 500, statusText: 'Error' });

    expect(composant().erreur()).toContain('Impossible');
  });

  it('bascule la visibilite du formulaire', () => {
    http.expectOne(URL).flush([]);

    expect(composant().formulaireVisible()).toBe(false);
    composant().basculerFormulaire();
    fixture.detectChanges();
    expect(composant().formulaireVisible()).toBe(true);
  });

  it("n envoie rien si le formulaire de creation est invalide", () => {
    http.expectOne(URL).flush([]);

    composant().formulaire.patchValue({ nom: '' });
    composant().creer();

    http.expectNone(URL);
  });

  it('ajoute le projet cree en tete de liste', () => {
    http.expectOne(URL).flush([]);

    composant().formulaire.patchValue({ nom: 'Nouveau projet' });
    composant().creer();

    const appel = http.expectOne(URL);
    expect(appel.request.method).toBe('POST');
    appel.flush(PROJET);

    fixture.detectChanges();
    expect(composant().projets().length).toBe(1);
    expect(composant().formulaireVisible()).toBe(false);
  });

  it('signale un echec de creation', () => {
    http.expectOne(URL).flush([]);

    composant().formulaire.patchValue({ nom: 'Nouveau projet' });
    composant().creer();
    http.expectOne(URL).flush({}, { status: 500, statusText: 'Error' });

    expect(composant().erreur()).toContain('projet');
  });
});
