import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { DetailTache } from './detail-tache';

const API = 'http://localhost:8080/api';

const ALICE = {
  id: 1,
  nom: 'Alice Martin',
  email: 'alice@codesolutions.fr',
  dateCreation: '2026-01-15T09:00:00',
};

const TACHE = {
  id: 10,
  nom: 'Concevoir le schema',
  description: 'Modéliser les entités',
  dateEcheance: '2026-02-01',
  dateFin: null,
  priorite: 'HAUTE',
  statut: 'EN_COURS',
  projetId: 1,
  createur: ALICE,
  assigne: null,
  dateCreation: '2026-01-15T09:00:00',
};

const HISTORIQUE = [
  {
    id: 1,
    auteur: ALICE,
    nomChamp: 'statut',
    ancienneValeur: 'A_FAIRE',
    nouvelleValeur: 'EN_COURS',
    dateModif: '2026-01-18T09:30:00',
  },
];

describe('DetailTache', () => {
  let fixture: ComponentFixture<DetailTache>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.setItem('pmt.utilisateur', JSON.stringify(ALICE));

    await TestBed.configureTestingModule({
      imports: [DetailTache],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(DetailTache);
    fixture.componentRef.setInput('id', '10');
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  function composant(): any {
    return fixture.componentInstance as any;
  }

  function charger(): void {
    fixture.detectChanges();
    http.expectOne(API + '/taches/10').flush(TACHE);
    http.expectOne(API + '/taches/10/historique').flush(HISTORIQUE);
    fixture.detectChanges();
  }

  it('remplit le formulaire avec la tâche chargée', () => {
    charger();

    expect(composant().tache()?.nom).toBe('Concevoir le schema');
    expect(composant().formulaire.getRawValue().statut).toBe('EN_COURS');
    expect(composant().historique().length).toBe(1);
  });

  it('signale une tache inaccessible', () => {
    fixture.detectChanges();
    http.expectOne(API + '/taches/10').flush({}, { status: 403, statusText: 'Forbidden' });
    http.expectOne(API + '/taches/10/historique').flush([]);

    expect(composant().erreur()).toContain('inaccessible');
  });

  it('bascule le mode édition', () => {
    charger();

    expect(composant().modeEdition()).toBe(false);
    composant().basculerEdition();
    fixture.detectChanges();
    expect(composant().modeEdition()).toBe(true);
  });

  it('recharge la tâche apres enregistrement', () => {
    charger();
    composant().basculerEdition();

    composant().formulaire.patchValue({ statut: 'TERMINEE' });
    composant().enregistrer();

    const appel = http.expectOne(API + '/taches/10');
    expect(appel.request.method).toBe('PUT');
    expect(appel.request.body.statut).toBe('TERMINEE');
    appel.flush({ ...TACHE, statut: 'TERMINEE' });

    http.expectOne(API + '/taches/10').flush({ ...TACHE, statut: 'TERMINEE' });
    http.expectOne(API + '/taches/10/historique').flush(HISTORIQUE);

    expect(composant().modeEdition()).toBe(false);
  });

  it("signale un role insuffisant a l'enregistrement", () => {
    charger();

    composant().enregistrer();
    http
      .expectOne(API + '/taches/10')
      .flush({}, { status: 403, statusText: 'Forbidden' });

    expect(composant().erreur()).toContain('role');
  });

  it("n enregistre pas un formulaire invalide", () => {
    charger();

    composant().formulaire.patchValue({ nom: '' });
    composant().enregistrer();

    http.expectNone(API + '/taches/10');
  });
});
