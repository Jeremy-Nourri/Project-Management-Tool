import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { DetailProjet } from './detail-projet';
import { AuthService } from '../../core/services/auth';

const API = 'http://localhost:8080/api';

const ALICE = {
  id: 1,
  nom: 'Alice Martin',
  email: 'alice@codesolutions.fr',
  dateCreation: '2026-01-15T09:00:00',
};

const BRUNO = {
  id: 2,
  nom: 'Bruno Lefevre',
  email: 'bruno@codesolutions.fr',
  dateCreation: '2026-01-15T09:00:00',
};

const PROJET = {
  id: 1,
  nom: 'PMT',
  description: 'Outil collaboratif',
  dateDebut: '2026-01-15',
  createur: ALICE,
};

const TACHE = {
  id: 10,
  nom: 'Concevoir le schema',
  description: null,
  dateEcheance: null,
  dateFin: null,
  priorite: 'HAUTE',
  statut: 'EN_COURS',
  projetId: 1,
  createur: ALICE,
  assigne: null,
  dateCreation: '2026-01-15T09:00:00',
};

describe('DetailProjet', () => {
  let fixture: ComponentFixture<DetailProjet>;
  let http: HttpTestingController;

  beforeEach(async () => {
    localStorage.setItem('pmt.utilisateur', JSON.stringify(ALICE));

    await TestBed.configureTestingModule({
      imports: [DetailProjet],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(DetailProjet);
    fixture.componentRef.setInput('id', '1');
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  function composant(): any {
    return fixture.componentInstance as any;
  }

  /** Repond aux trois appels declenches au chargement. */
  function chargerAvecRole(role: string): void {
    fixture.detectChanges();
    http.expectOne(API + '/projets/1').flush(PROJET);
    http
      .expectOne(API + '/projets/1/membres')
      .flush([{ utilisateur: ALICE, role }, { utilisateur: BRUNO, role: 'MEMBRE' }]);
    http.expectOne(API + '/projets/1/taches').flush([TACHE]);
    fixture.detectChanges();
  }

  it('déduit le role utilisateur connecté', () => {
    chargerAvecRole('ADMIN');

    expect(composant().monRole()).toBe('ADMIN');
    expect(composant().estAdmin()).toBe(true);
    expect(composant().peutModifier()).toBe(true);
  });

  it("interdit administration à un observateur", () => {
    chargerAvecRole('OBSERVATEUR');

    expect(composant().estAdmin()).toBe(false);
    expect(composant().peutModifier()).toBe(false);
  });

  it('autorise la modification à un membre', () => {
    chargerAvecRole('MEMBRE');

    expect(composant().estAdmin()).toBe(false);
    expect(composant().peutModifier()).toBe(true);
  });

  it('repartit les taches par statut', () => {
    chargerAvecRole('ADMIN');

    expect(composant().tachesParStatut('EN_COURS').length).toBe(1);
    expect(composant().tachesParStatut('A_FAIRE').length).toBe(0);
  });

  it('ajoute un membre invité à la liste', () => {
    chargerAvecRole('ADMIN');

    composant().formulaireMembre.setValue({
      email: 'chloe@codesolutions.fr',
      role: 'OBSERVATEUR',
    });
    composant().inviter();

    const appel = http.expectOne(API + '/projets/1/membres');
    expect(appel.request.method).toBe('POST');
    appel.flush({ utilisateur: { ...BRUNO, id: 3 }, role: 'OBSERVATEUR' });

    expect(composant().membres().length).toBe(3);
  });

  it('signale un email inconnu invitation', () => {
    chargerAvecRole('ADMIN');

    composant().formulaireMembre.setValue({
      email: 'inconnu@codesolutions.fr',
      role: 'MEMBRE',
    });
    composant().inviter();

    http
      .expectOne(API + '/projets/1/membres')
      .flush({}, { status: 404, statusText: 'Not Found' });

    expect(composant().erreur()).toContain('compte');
  });

  it('signale un membre déja présent', () => {
    chargerAvecRole('ADMIN');

    composant().formulaireMembre.setValue({
      email: BRUNO.email,
      role: 'MEMBRE',
    });
    composant().inviter();

    http
      .expectOne(API + '/projets/1/membres')
      .flush({}, { status: 409, statusText: 'Conflict' });

    expect(composant().erreur()).toContain('membre');
  });

  it("invite pas si le formulaire est invalide", () => {
    chargerAvecRole('ADMIN');

    composant().formulaireMembre.setValue({ email: 'pas-un-email', role: 'MEMBRE' });
    composant().inviter();

    http.expectNone(API + '/projets/1/membres');
  });

  it('met a jour le rôle dans la liste', () => {
    chargerAvecRole('ADMIN');

    composant().changerRole({ utilisateur: BRUNO, role: 'MEMBRE' }, 'ADMIN');

    const appel = http.expectOne(API + '/projets/1/membres');
    expect(appel.request.method).toBe('PUT');
    appel.flush({ utilisateur: BRUNO, role: 'ADMIN' });

    expect(composant().membres()[1].role).toBe('ADMIN');
  });

  it('signale le retrait du dernier administrateur', () => {
    chargerAvecRole('ADMIN');

    composant().changerRole({ utilisateur: ALICE, role: 'ADMIN' }, 'MEMBRE');
    http
      .expectOne(API + '/projets/1/membres')
      .flush({}, { status: 409, statusText: 'Conflict' });

    expect(composant().erreur()).toContain('administrateur');
  });

  it('ajoute la tache créé au tableau', () => {
    chargerAvecRole('ADMIN');

    composant().formulaireTache.patchValue({ nom: 'Nouvelle tache' });
    composant().creerTache();

    const appel = http.expectOne(API + '/projets/1/taches');
    expect(appel.request.method).toBe('POST');
    appel.flush({ ...TACHE, id: 11, statut: 'A_FAIRE' });

    expect(composant().taches().length).toBe(2);
  });

  it("ne crée pas de tâche si le nom manque", () => {
    chargerAvecRole('ADMIN');

    composant().formulaireTache.patchValue({ nom: '' });
    composant().creerTache();

    http.expectNone(API + '/projets/1/taches');
  });
});
