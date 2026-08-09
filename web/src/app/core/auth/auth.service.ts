import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

// Key under which the Basic auth header is persisted (see AuthService).
const AUTH_STORAGE_KEY = 'salary.authHeader';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);

  // The HTTP Basic "Authorization" header value ("Basic base64(user:pass)"),
  // persisted in sessionStorage - NOT localStorage. sessionStorage survives a
  // page reload within the same tab (so refresh no longer forces re-login), but
  // is cleared when the tab closes and is never shared across tabs or written to
  // disk long-term: a middle ground between memory-only (lost on refresh) and
  // localStorage (persists on disk, readable by any XSS across sessions).
  private authHeader = signal<string | null>(sessionStorage.getItem(AUTH_STORAGE_KEY));

  // "Log in" by attaching the credentials and probing a protected endpoint.
  // A 2xx confirms the credentials are valid and keeps them; any error clears
  // them (the interceptor attaches the header we just set to the probe).
  login(username: string, password: string): Observable<unknown> {
    this.setAuthHeader('Basic ' + btoa(`${username}:${password}`));
    return this.http
      .get(`${environment.apiBaseUrl}/employees`, { params: { page: 1, limit: 1 } })
      .pipe(tap({ error: () => this.setAuthHeader(null) }));
  }

  logout(): void {
    this.setAuthHeader(null);
  }

  getAuthHeader(): string | null {
    return this.authHeader();
  }

  isAuthenticated(): boolean {
    return this.authHeader() !== null;
  }

  // Keep the in-memory signal and sessionStorage in lock-step.
  private setAuthHeader(value: string | null): void {
    this.authHeader.set(value);
    if (value) {
      sessionStorage.setItem(AUTH_STORAGE_KEY, value);
    } else {
      sessionStorage.removeItem(AUTH_STORAGE_KEY);
    }
  }
}
