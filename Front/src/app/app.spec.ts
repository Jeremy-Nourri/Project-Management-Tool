import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { App } from './app';
import { AuthService } from './core/services/auth';

const ALICE = {
  id: 1,
  nom: 'Alice Martin',
  email: 'alice@codesolutions.fr',
  dateCreation: '2026-01-15T09:00:00',
};

describe('App', () => {
  let fixture: ComponentFixture<App>;

  async function preparer(): Promise<void> {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(App);
    fixture.detectChanges();
  }

  afterEach(() => {
    localStorage.clear();
    TestBed.resetTestingModule();
  });

  it('masque la navigation quand personne n est connecte', async () => {
    localStorage.clear();
    await preparer();

    expect(fixture.nativeElement.querySelector('nav')).toBeNull();
  });

  it('affiche le nom de l utilisateur connecte', async () => {
    localStorage.setItem('pmt.utilisateur', JSON.stringify(ALICE));
    await preparer();

    expect(fixture.nativeElement.textContent).toContain('Alice Martin');
  });

  it('deconnecte et redirige vers la page de connexion', async () => {
    localStorage.setItem('pmt.utilisateur', JSON.stringify(ALICE));
    await preparer();

    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    (fixture.componentInstance as any).deconnecter();

    expect(TestBed.inject(AuthService).estConnecte()).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/connexion']);
  });
});
