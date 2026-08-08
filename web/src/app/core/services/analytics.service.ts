import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AnalyticsSummary, DistributionBucket, GroupAggregate } from '../models/analytics.model';

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private http = inject(HttpClient);
  private base = `${environment.apiBaseUrl}/analytics`;

  summary(): Observable<AnalyticsSummary> {
    return this.http.get<AnalyticsSummary>(`${this.base}/summary`);
  }

  byDepartment(): Observable<GroupAggregate[]> {
    return this.http.get<GroupAggregate[]>(`${this.base}/by-department`);
  }

  byCountry(): Observable<GroupAggregate[]> {
    return this.http.get<GroupAggregate[]>(`${this.base}/by-country`);
  }

  byLevel(): Observable<GroupAggregate[]> {
    return this.http.get<GroupAggregate[]>(`${this.base}/by-level`);
  }

  distribution(): Observable<DistributionBucket[]> {
    return this.http.get<DistributionBucket[]>(`${this.base}/distribution`);
  }
}
