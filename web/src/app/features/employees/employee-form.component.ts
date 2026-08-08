import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { EmployeeService } from '../../core/services/employee.service';

const CURRENCIES = ['USD', 'GBP', 'EUR', 'INR', 'CAD', 'AUD', 'SGD', 'JPY', 'BRL'];

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.scss',
})
export class EmployeeFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private employeeService = inject(EmployeeService);
  private snackBar = inject(MatSnackBar);

  currencies = CURRENCIES;
  editId: number | null = null;
  saving = false;
  errorMessage: string | null = null;

  form = this.fb.group({
    employeeCode: ['', Validators.required],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    department: ['', Validators.required],
    jobTitle: ['', Validators.required],
    level: ['', Validators.required],
    country: ['', Validators.required],
    currencyCode: ['USD', Validators.required],
    hireDate: ['', Validators.required],
  });

  get isEdit(): boolean {
    return this.editId !== null;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.editId = Number(idParam);
      this.form.controls.employeeCode.disable(); // employeeCode is immutable once assigned
      this.employeeService.get(this.editId).subscribe((employee) => {
        this.form.patchValue({
          employeeCode: employee.employeeCode,
          firstName: employee.firstName,
          lastName: employee.lastName,
          email: employee.email,
          department: employee.department,
          jobTitle: employee.jobTitle,
          level: employee.level,
          country: employee.country,
          currencyCode: employee.currencyCode,
          hireDate: employee.hireDate,
        });
      });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    this.saving = true;
    this.errorMessage = null;
    const value = this.form.getRawValue();

    const done = {
      next: (employee: { id: number }) => {
        this.saving = false;
        this.snackBar.open(this.isEdit ? 'Employee updated.' : 'Employee created.', 'Dismiss', {
          duration: 4000,
        });
        this.router.navigate(['/employees', this.isEdit ? this.editId : employee.id]);
      },
      error: (err: { error?: { error?: { message?: string } } }) => {
        this.saving = false;
        this.errorMessage = err.error?.error?.message ?? 'Could not save employee.';
      },
    };

    if (this.isEdit) {
      const { employeeCode, ...update } = value;
      this.employeeService.update(this.editId!, update as any).subscribe(done);
    } else {
      this.employeeService.create(value as any).subscribe(done);
    }
  }
}
