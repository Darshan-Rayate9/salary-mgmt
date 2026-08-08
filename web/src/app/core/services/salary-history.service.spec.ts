import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { SalaryHistoryService } from './salary-history.service';
import { environment } from '../../../environments/environment';

describe('SalaryHistoryService', () => {
  let service: SalaryHistoryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(SalaryHistoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('lists salary history for an employee', () => {
    service.list(42).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/employees/42/salary-history`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('posts a new salary record', () => {
    const request = { amount: 100000, currencyCode: 'USD', effectiveDate: '2024-01-01', reason: 'Merit increase' };
    service.add(42, request).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/employees/42/salary-history`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ id: 1, ...request, usdEquivalent: 100000, createdAt: '2024-01-01T00:00:00Z' });
  });
});
