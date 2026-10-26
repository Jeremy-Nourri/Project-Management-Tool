import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  { path: '', redirectTo: 'projets', pathMatch: 'full' },
  {
    path: 'connexion',
    loadComponent: () =>
      import('./features/auth/connexion').then((m) => m.Connexion),
  },
  {
    path: 'inscription',
    loadComponent: () =>
      import('./features/auth/inscription').then((m) => m.Inscription),
  },
  {
    path: 'projets',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/projets/liste-projets').then((m) => m.ListeProjets),
  },
  {
    path: 'projets/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/projets/detail-projet').then((m) => m.DetailProjet),
  },
  {
    path: 'taches/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/taches/detail-tache').then((m) => m.DetailTache),
  },
  { path: '**', redirectTo: 'projets' },
];
