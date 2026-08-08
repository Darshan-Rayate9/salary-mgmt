import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SalaryRecord, SalaryRecordCreateRequest } from '../models/employee.model';

@Injectable({ providedIn: 'root' })
export class SalaryHistoryService {
  private http = inject(HttpClient);

  list(employeeId: number): Observable<SalaryRecord[]> {
    return this.http.get<SalaryRecord[]>(
      `${environment.apiBaseUrl}/employees/${employeeId}/salary-history`,
    );
  }

  add(employeeId: number, request: SalaryRecordCreateRequest): Observable<SalaryRecord> {
    return this.http.post<SalaryRecord>(
      `${environment.apiBaseUrl}/employees/${employeeId}/salary-history`,
      request,
    );
  }
}
