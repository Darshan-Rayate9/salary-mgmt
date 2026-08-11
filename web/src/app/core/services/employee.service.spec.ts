import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { EmployeeService } from './employee.service';
import { environment } from '../../../environments/environment';
import { EmployeeSummary } from '../models/employee.model';
import { pageOf } from '../testing/fixtures';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EmployeeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('requests a page of employees with page/limit query params', () => {
    const mockResponse = pageOf<EmployeeSummary>();

    service.list(1, 20).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(
      (r) =>
        r.url === `${environment.apiBaseUrl}/employees` &&
        r.params.get('page') === '1' &&
        r.params.get('limit') === '20',
    );
    expect(req.request.method).toBe('GET');
    req.flush(mockResponse);
  });

  it('passes non-empty filters as query params and omits blank ones', () => {
    service.list(1, 20, { search: 'ada', department: 'Engineering', status: '' }).subscribe();

    const req = httpMock.expectOne((r) => r.url === `${environment.apiBaseUrl}/employees`);
    expect(req.request.params.get('search')).toBe('ada');
    expect(req.request.params.get('department')).toBe('Engineering');
    expect(req.request.params.has('status')).toBeFalse(); // blank filter omitted
    req.flush(pageOf());
  });

  it('creates an employee via POST', () => {
    const request = {
      employeeCode: 'E-1',
      firstName: 'Ada',
      lastName: 'Lovelace',
      email: 'ada@acme.test',
      department: 'Engineering',
      jobTitle: 'Engineer',
      level: 'L3',
      country: 'United States',
      currencyCode: 'USD',
      hireDate: '2022-01-01',
    };
    service.create(request).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/employees`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({});
  });

  it('deletes an employee via DELETE', () => {
    service.delete(7).subscribe();
    const req = httpMock.expectOne(`${environment.apiBaseUrl}/employees/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
