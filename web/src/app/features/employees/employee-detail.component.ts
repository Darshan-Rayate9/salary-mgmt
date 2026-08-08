import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EmployeeService } from '../../core/services/employee.service';
import { SalaryHistoryService } from '../../core/services/salary-history.service';
import { EmployeeDetail, SalaryRecord } from '../../core/models/employee.model';
import {
  SalaryRecordDialogComponent,
  SalaryRecordDialogData,
} from './salary-record-dialog.component';

@Component({
  selector: 'app-employee-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    MatSnackBarModule,
  ],
  templateUrl: './employee-detail.component.html',
  styleUrl: './employee-detail.component.scss',
})
export class EmployeeDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private employeeService = inject(EmployeeService);
  private salaryHistoryService = inject(SalaryHistoryService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  employee: EmployeeDetail | null = null;
  history: SalaryRecord[] = [];
  loading = true;
  historyColumns = ['effectiveDate', 'amount', 'usdEquivalent', 'reason'];

  private id!: number;

  ngOnInit(): void {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.load();
  }

  addRaise(): void {
    const data: SalaryRecordDialogData = { currencyCode: this.employee?.currencyCode ?? 'USD' };
    this.dialog
      .open(SalaryRecordDialogComponent, { data })
      .afterClosed()
      .subscribe((request) => {
        if (!request) {
          return;
        }
        this.salaryHistoryService.add(this.id, request).subscribe({
          next: () => {
            this.snackBar.open('Salary record added.', 'Dismiss', { duration: 4000 });
            this.load();
          },
          error: () => this.snackBar.open('Could not add salary record.', 'Dismiss', { duration: 4000 }),
        });
      });
  }

  edit(): void {
    this.router.navigate(['/employees', this.id, 'edit']);
  }

  remove(): void {
    const label = this.history.length > 0 ? 'terminate' : 'delete';
    if (!confirm(`Are you sure you want to ${label} this employee?`)) {
      return;
    }
    this.employeeService.delete(this.id).subscribe({
      next: () => {
        this.snackBar.open(`Employee ${label}d.`, 'Dismiss', { duration: 4000 });
        this.router.navigate(['/employees']);
      },
      error: () => this.snackBar.open('Could not delete employee.', 'Dismiss', { duration: 4000 }),
    });
  }

  private load(): void {
    this.loading = true;
    this.employeeService.get(this.id).subscribe({
      next: (employee) => {
        this.employee = employee;
        this.loading = false;
      },
      error: () => (this.loading = false),
    });
    this.salaryHistoryService.list(this.id).subscribe((history) => (this.history = history));
  }
}
