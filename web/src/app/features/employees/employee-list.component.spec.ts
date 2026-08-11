import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { EmployeeListComponent } from './employee-list.component';
import { environment } from '../../../environments/environment';
import { pageOf } from '../../core/testing/fixtures';

describe('EmployeeListComponent', () => {
  let fixture: ComponentFixture<EmployeeListComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeListComponent, NoopAnimationsModule],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();

    fixture = TestBed.createComponent(EmployeeListComponent);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads the first page of employees on init', () => {
    fixture.detectChanges();

    const listReq = httpMock.expectOne((r) => r.url === `${environment.apiBaseUrl}/employees`);
    expect(listReq.request.params.get('page')).toBe('1');
    listReq.flush(
      pageOf([
        {
          id: 1,
          employeeCode: 'E-1001',
          firstName: 'Ada',
          lastName: 'Lovelace',
          department: 'Engineering',
          jobTitle: 'Principal Engineer',
          level: 'L6',
          country: 'United Kingdom',
          employmentStatus: 'ACTIVE',
          currentSalaryAmount: 102000,
          currentSalaryCurrency: 'GBP',
        },
      ]),
    );

    expect(fixture.componentInstance.employees.length).toBe(1);
    expect(fixture.componentInstance.total).toBe(1);
  });
});
