import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    // AuthService persists to sessionStorage, which Karma keeps between tests -
    // clear it so each test starts from a known, unauthenticated state.
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('starts unauthenticated', () => {
    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getAuthHeader()).toBeNull();
  });

  it('keeps the basic credentials after a successful login probe', () => {
    service.login('hr.manager', 'secret').subscribe();

    const req = httpMock.expectOne((r) => r.url === `${environment.apiBaseUrl}/employees`);
    expect(req.request.method).toBe('GET');
    req.flush({ items: [], page: 1, limit: 1, total: 0, totalPages: 0, hasNext: false, hasPrev: false });

    expect(service.isAuthenticated()).toBeTrue();
    expect(service.getAuthHeader()).toBe('Basic ' + btoa('hr.manager:secret'));
  });

  it('clears the credentials when the login probe fails', () => {
    service.login('hr.manager', 'wrong').subscribe({ error: () => undefined });

    httpMock
      .expectOne((r) => r.url === `${environment.apiBaseUrl}/employees`)
      .flush({ error: { code: 'AUTH_REQUIRED' } }, { status: 401, statusText: 'Unauthorized' });

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getAuthHeader()).toBeNull();
  });

  it('clears the credentials on logout', () => {
    service.login('hr.manager', 'secret').subscribe();
    httpMock
      .expectOne((r) => r.url === `${environment.apiBaseUrl}/employees`)
      .flush({ items: [], page: 1, limit: 1, total: 0, totalPages: 0, hasNext: false, hasPrev: false });

    service.logout();

    expect(service.isAuthenticated()).toBeFalse();
    expect(service.getAuthHeader()).toBeNull();
  });
});
