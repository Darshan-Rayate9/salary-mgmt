import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { forkJoin } from 'rxjs';
import { ChartConfiguration, ChartData } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { AnalyticsService } from '../../core/services/analytics.service';
import { AnalyticsSummary, DistributionBucket, GroupAggregate } from '../../core/models/analytics.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, BaseChartDirective, MatCardModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private analytics = inject(AnalyticsService);

  loading = true;
  summary: AnalyticsSummary | null = null;

  byDepartmentChart: ChartData<'bar'> = { labels: [], datasets: [] };
  byCountryChart: ChartData<'bar'> = { labels: [], datasets: [] };
  byLevelChart: ChartData<'bar'> = { labels: [], datasets: [] };
  distributionChart: ChartData<'bar'> = { labels: [], datasets: [] };

  readonly salaryChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: true } },
    scales: { y: { beginAtZero: true, title: { display: true, text: 'USD' } } },
  };

  readonly countChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: { y: { beginAtZero: true, title: { display: true, text: 'Employees' } } },
  };

  ngOnInit(): void {
    forkJoin({
      summary: this.analytics.summary(),
      byDepartment: this.analytics.byDepartment(),
      byCountry: this.analytics.byCountry(),
      byLevel: this.analytics.byLevel(),
      distribution: this.analytics.distribution(),
    }).subscribe({
      next: (data) => {
        this.summary = data.summary;
        this.byDepartmentChart = this.salaryChart(data.byDepartment);
        this.byCountryChart = this.salaryChart(data.byCountry);
        this.byLevelChart = this.salaryChart(sortByLevel(data.byLevel));
        this.distributionChart = this.distribution(data.distribution);
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
  }

  private salaryChart(groups: GroupAggregate[]): ChartData<'bar'> {
    return {
      labels: groups.map((g) => g.groupName),
      datasets: [
        { label: 'Average (USD)', data: groups.map((g) => round(g.avgSalaryUsd)), backgroundColor: '#3f51b5' },
        { label: 'Median (USD)', data: groups.map((g) => round(g.medianSalaryUsd)), backgroundColor: '#7986cb' },
      ],
    };
  }

  private distribution(buckets: DistributionBucket[]): ChartData<'bar'> {
    return {
      labels: buckets.map((b) => b.bucketLabel),
      datasets: [{ label: 'Employees', data: buckets.map((b) => b.count), backgroundColor: '#3f51b5' }],
    };
  }
}

function round(value: number): number {
  return Math.round(value);
}

function sortByLevel(groups: GroupAggregate[]): GroupAggregate[] {
  return [...groups].sort((a, b) => a.groupName.localeCompare(b.groupName));
}
