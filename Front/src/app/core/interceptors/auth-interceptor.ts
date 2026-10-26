import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';

export const authInterceptor: HttpInterceptorFn = (requete, suivant) => {
  if (requete.url.includes('/auth/')) {
    return suivant(requete);
  }

  const identifiant = inject(AuthService).identifiant();
  if (identifiant === null) {
    return suivant(requete);
  }

  return suivant(
    requete.clone({ setHeaders: { Authorization: String(identifiant) } })
  );
};
