import { Role } from './role';
import { Utilisateur } from './utilisateur';

export interface Projet {
  id: number;
  nom: string;
  description: string | null;
  dateDebut: string;
  createur: Utilisateur;
}

export interface ProjetRequest {
  nom: string;
  description: string | null;
  dateDebut: string;
}

export interface Membre {
  utilisateur: Utilisateur;
  role: Role;
}

export interface MembreRequest {
  email: string;
  role: Role;
}
