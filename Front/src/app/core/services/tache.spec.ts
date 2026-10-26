import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TacheService } from './tache';

const BASE = 'http://localhost:8080/api';

describe('TacheService', () => {
  let service: TacheService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TacheService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('liste toutes les tâches sans filtre', () => {
    service.lister(1).subscribe();
    const appel = http.expectOne(BASE + '/projets/1/taches');
    expect(appel.request.params.has('statut')).toBe(false);
    appel.flush([]);
  });

  it('ajoute le paramètre statut quand il est fourni', () => {
    service.lister(1, 'EN_COURS').subscribe();
    const appel = http.expectOne(
      (r) => r.url === BASE + '/projets/1/taches' && r.params.get('statut') === 'EN_COURS',
    );
    appel.flush([]);
  });

  it('consulte une tâche', () => {
    service.consulter(5).subscribe();
    http.expectOne(BASE + '/taches/5').flush({});
  });

  it('crée une tâche', () => {
    service.creer(1, { nom: 'Nouvelle tache' }).subscribe();
    const appel = http.expectOne(BASE + '/projets/1/taches');
    expect(appel.request.method).toBe('POST');
    appel.flush({});
  });

  it('met à jour une tâche', () => {
    service.mettreAJour(5, { nom: 'Tache modifiee' }).subscribe();
    const appel = http.expectOne(BASE + '/taches/5');
    expect(appel.request.method).toBe('PUT');
    appel.flush({});
  });

  it('liste historique', () => {
    service.listerHistorique(5).subscribe();
    http.expectOne(BASE + '/taches/5/historique').flush([]);
  });
});
