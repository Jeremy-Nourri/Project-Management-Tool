/** Roles possibles sur un projet. Doit correspondre a l'enum Java. */
export type Role = 'ADMIN' | 'MEMBRE' | 'OBSERVATEUR';

export const ROLES: Role[] = ['ADMIN', 'MEMBRE', 'OBSERVATEUR'];

export const LIBELLES_ROLE: Record<Role, string> = {
  ADMIN: 'Administrateur',
  MEMBRE: 'Membre',
  OBSERVATEUR: 'Observateur',
};
