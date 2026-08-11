import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AuthService } from './auth.service';
import { environment } from '../../../environments/environment';
import { pageOf } from '../testing/fixtures';

describe('authInterceptor', () => {
  let httpClient: HttpClient;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    // AuthService persists to sessionStorage, which Karma keeps between tests.
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]), // the interceptor injects Router to redirect on 401
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => httpMock.verify());

  it('adds a Basic Authorization header once credentials exist', () => {
    authService.login('hr.manager', 'secret').subscribe();
    // The login probe itself flows through the interceptor and already carries the header.
    httpMock
      .expectOne((r) => r.url === `${environment.apiBaseUrl}/employees`)
      .flush(pageOf());

    httpClient.get(`${environment.apiBaseUrl}/employees/1`).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/employees/1`);
    expect(req.request.headers.get('Authorization')).toBe('Basic ' + btoa('hr.manager:secret'));
    req.flush({});
  });

  it('does not add a header before login', () => {
    httpClient.get(`${environment.apiBaseUrl}/employees`).subscribe();

    const req = httpMock.expectOne(`${environment.apiBaseUrl}/employees`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });
});
