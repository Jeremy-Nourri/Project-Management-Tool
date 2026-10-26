import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ProjetService } from './projet';
import { Projet } from '../models/projet';

const BASE = 'http://localhost:8080/api/projets';

const PROJET: Projet = {
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

describe('ProjetService', () => {
  let service: ProjetService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ProjetService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('liste les projets de l utilisateur', () => {
    let recus: Projet[] = [];
    service.listerMesProjets().subscribe((p) => (recus = p));

    http.expectOne(BASE).flush([PROJET]);

    expect(recus.length).toBe(1);
    expect(recus[0].nom).toBe('PMT');
  });

  it('consulte un projet par son identifiant', () => {
    service.consulter(1).subscribe();
    const appel = http.expectOne(BASE + '/1');
    expect(appel.request.method).toBe('GET');
    appel.flush(PROJET);
  });

  it('crée un projet', () => {
    const requete = { nom: 'PMT', description: null, dateDebut: '2026-01-15' };
    service.creer(requete).subscribe();

    const appel = http.expectOne(BASE);
    expect(appel.request.method).toBe('POST');
    expect(appel.request.body).toEqual(requete);
    appel.flush(PROJET);
  });

  it('liste les membres', () => {
    service.listerMembres(1).subscribe();
    http.expectOne(BASE + '/1/membres').flush([]);
  });

  it('invite un membre', () => {
    service.inviterMembre(1, { email: 'bruno@codesolutions.fr', role: 'MEMBRE' }).subscribe();

    const appel = http.expectOne(BASE + '/1/membres');
    expect(appel.request.method).toBe('POST');
    appel.flush({});
  });

  it('attribue un rôle', () => {
    service.attribuerRole(1, { email: 'bruno@codesolutions.fr', role: 'ADMIN' }).subscribe();

    const appel = http.expectOne(BASE + '/1/membres');
    expect(appel.request.method).toBe('PUT');
    appel.flush({});
  });
});
