import { Component } from '@angular/core';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, MatToolbarModule, MatButtonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  constructor(
    protected auth: AuthService,
    private router: Router,
  ) {}

  logout(): void {
    this.auth.logout();
    // The authGuard only runs on navigation, so clearing credentials doesn't
    // move us off the current page - navigate to /login explicitly.
    this.router.navigate(['/login']);
  }
}
