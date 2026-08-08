import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  EmployeeCreateRequest,
  EmployeeDetail,
  EmployeeFilters,
  EmployeeSummary,
  EmployeeUpdateRequest,
  PageResponse,
} from '../models/employee.model';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/employees`;

  list(page: number, limit: number, filters: EmployeeFilters = {}): Observable<PageResponse<EmployeeSummary>> {
    let params = new HttpParams().set('page', page).set('limit', limit);
    for (const [key, value] of Object.entries(filters)) {
      if (value) {
        params = params.set(key, value);
      }
    }
    return this.http.get<PageResponse<EmployeeSummary>>(this.base, { params });
  }

  get(id: number): Observable<EmployeeDetail> {
    return this.http.get<EmployeeDetail>(`${this.base}/${id}`);
  }

  create(request: EmployeeCreateRequest): Observable<EmployeeDetail> {
    return this.http.post<EmployeeDetail>(this.base, request);
  }

  update(id: number, request: EmployeeUpdateRequest): Observable<EmployeeDetail> {
    return this.http.put<EmployeeDetail>(`${this.base}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
