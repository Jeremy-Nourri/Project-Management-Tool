export interface Utilisateur {
  id: number;
  nom: string;
  email: string;
  dateCreation: string;
}

export interface InscriptionRequest {
  nom: string;
  email: string;
  motDePasse: string;
}

export interface ConnexionRequest {
  email: string;
  motDePasse: string;
}
