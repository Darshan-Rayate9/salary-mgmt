import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { EmployeeService } from '../../core/services/employee.service';
import { EmployeeSummary } from '../../core/models/employee.model';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
  ],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.scss',
})
export class EmployeeListComponent implements OnInit {
  private employeeService = inject(EmployeeService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  displayedColumns = ['employeeCode', 'name', 'department', 'level', 'country', 'currentSalary', 'status'];
  employees: EmployeeSummary[] = [];
  total = 0;
  pageSize = 20;
  pageIndex = 0;
  loading = false;

  // Filter dropdown options. The organisation's departments, countries and
  // levels are a fixed, known set (see the seed script), so these are static
  // rather than fetched from a facets endpoint - one less request and one less
  // moving part for a single-org internal tool.
  departments = [
    'Customer Support', 'Design', 'Engineering', 'Finance', 'Human Resources',
    'Legal', 'Marketing', 'Operations', 'Product', 'Sales',
  ];
  countries = [
    'Australia', 'Brazil', 'Canada', 'France', 'Germany', 'India', 'Japan',
    'Singapore', 'United Kingdom', 'United States',
  ];
  levels = ['L1', 'L2', 'L3', 'L4', 'L5', 'L6', 'L7'];
  statuses = ['ACTIVE', 'TERMINATED'];

  filterForm = this.fb.group({
    search: [''],
    department: [''],
    country: [''],
    level: [''],
    status: [''],
  });

  ngOnInit(): void {
    this.filterForm.valueChanges.pipe(debounceTime(300), distinctUntilChanged()).subscribe(() => {
      this.pageIndex = 0;
      this.load();
    });
    this.load();
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  openEmployee(id: number): void {
    this.router.navigate(['/employees', id]);
  }

  addEmployee(): void {
    this.router.navigate(['/employees/new']);
  }

  private load(): void {
    this.loading = true;
    const filters = this.filterForm.getRawValue();
    // Backend pagination is 1-indexed (see ARCHITECTURE.md); Material's paginator is 0-indexed.
    this.employeeService
      .list(this.pageIndex + 1, this.pageSize, {
        search: filters.search ?? '',
        department: filters.department ?? '',
        country: filters.country ?? '',
        level: filters.level ?? '',
        status: (filters.status ?? '') as '' | 'ACTIVE' | 'TERMINATED',
      })
      .subscribe({
        next: (response) => {
          this.employees = response.items;
          this.total = response.total;
          this.loading = false;
        },
        error: () => (this.loading = false),
      });
  }
}
