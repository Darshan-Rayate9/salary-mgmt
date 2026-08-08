import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  // The HTTP Basic "Authorization" header value ("Basic base64(user:pass)"),
  // held in memory only - never localStorage, so an XSS payload can't read a
  // credential off disk. Trade-off: a full page reload requires logging in
  // again, acceptable for a single-user internal admin tool.
  private authHeader = signal<string | null>(null);

  // "Log in" by attaching the credentials and probing a protected endpoint.
  // A 2xx confirms the credentials are valid and keeps them; any error clears
  // them (the interceptor attaches the header we just set to the probe).
  login(username: string, password: string): Observable<unknown> {
    this.authHeader.set('Basic ' + btoa(`${username}:${password}`));
    return this.http
      .get(`${environment.apiBaseUrl}/employees`, { params: { page: 1, limit: 1 } })
      .pipe(tap({ error: () => this.authHeader.set(null) }));
  }

  logout(): void {
    this.authHeader.set(null);
  }

  getAuthHeader(): string | null {
    return this.authHeader();
  }

  isAuthenticated(): boolean {
    return this.authHeader() !== null;
  }
}
