import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AnalyticsService } from './analytics.service';
import { environment } from '../../../environments/environment';

describe('AnalyticsService', () => {
  let service: AnalyticsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AnalyticsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('requests the summary endpoint', () => {
    service.summary().subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/analytics/summary`);
    expect(req.request.method).toBe('GET');
    req.flush({ headcount: 0, avgSalaryUsd: 0, medianSalaryUsd: 0, minSalaryUsd: 0, maxSalaryUsd: 0, totalPayrollCostUsd: 0 });
  });

  it('requests the by-department endpoint', () => {
    service.byDepartment().subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/analytics/by-department`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('requests the distribution endpoint', () => {
    service.distribution().subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/analytics/distribution`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });
});
