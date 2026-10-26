import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth';

@Component({
  imports: [RouterOutlet, RouterLink],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  private readonly router = inject(Router);
  protected readonly auth = inject(AuthService);

  protected deconnecter(): void {
    this.auth.deconnecter();
    this.router.navigate(['/connexion']);
  }
}
