import { Utilisateur } from './utilisateur';

export type StatutTache = 'A_FAIRE' | 'EN_COURS' | 'TERMINEE';
export type PrioriteTache = 'BASSE' | 'MOYENNE' | 'HAUTE';

export const STATUTS: StatutTache[] = ['A_FAIRE', 'EN_COURS', 'TERMINEE'];
export const PRIORITES: PrioriteTache[] = ['BASSE', 'MOYENNE', 'HAUTE'];

export const LIBELLES_STATUT: Record<StatutTache, string> = {
  A_FAIRE: 'A faire',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminee',
};

export const LIBELLES_PRIORITE: Record<PrioriteTache, string> = {
  BASSE: 'Basse',
  MOYENNE: 'Moyenne',
  HAUTE: 'Haute',
};

export interface Tache {
  id: number;
  nom: string;
  description: string | null;
  dateEcheance: string | null;
  dateFin: string | null;
  priorite: PrioriteTache;
  statut: StatutTache;
  projetId: number;
  createur: Utilisateur;
  assigne: Utilisateur | null;
  dateCreation: string;
}

export interface TacheRequest {
  nom: string;
  description?: string | null;
  dateEcheance?: string | null;
  dateFin?: string | null;
  priorite?: PrioriteTache | null;
  statut?: StatutTache | null;
  assigneEmail?: string | null;
}

export interface Historique {
  id: number;
  auteur: Utilisateur;
  nomChamp: string;
  ancienneValeur: string | null;
  nouvelleValeur: string | null;
  dateModif: string;
}
