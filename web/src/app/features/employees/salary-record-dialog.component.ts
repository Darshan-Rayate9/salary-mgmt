import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { SalaryRecordCreateRequest } from '../../core/models/employee.model';

export interface SalaryRecordDialogData {
  currencyCode: string;
}

const CURRENCIES = ['USD', 'GBP', 'EUR', 'INR', 'CAD', 'AUD', 'SGD', 'JPY', 'BRL'];
const REASONS = ['Merit increase', 'Promotion', 'Market adjustment', 'Annual increase', 'Initial salary'];

@Component({
  selector: 'app-salary-record-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
  ],
  template: `
    <h2 mat-dialog-title>Add salary record</h2>
    <form [formGroup]="form" (ngSubmit)="submit()">
      <mat-dialog-content class="content">
        <mat-form-field appearance="outline">
          <mat-label>Amount</mat-label>
          <input matInput type="number" formControlName="amount" min="0.01" step="0.01" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Currency</mat-label>
          <mat-select formControlName="currencyCode">
            @for (c of currencies; track c) {
              <mat-option [value]="c">{{ c }}</mat-option>
            }
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Effective date</mat-label>
          <input matInput type="date" formControlName="effectiveDate" />
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Reason</mat-label>
          <mat-select formControlName="reason">
            @for (r of reasons; track r) {
              <mat-option [value]="r">{{ r }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
      </mat-dialog-content>

      <mat-dialog-actions align="end">
        <button mat-button type="button" (click)="cancel()">Cancel</button>
        <button mat-raised-button color="primary" type="submit" [disabled]="form.invalid">Save</button>
      </mat-dialog-actions>
    </form>
  `,
  styles: [
    `
      .content {
        display: flex;
        flex-direction: column;
        min-width: 320px;
        padding-top: 8px;
      }
    `,
  ],
})
export class SalaryRecordDialogComponent {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<SalaryRecordDialogComponent>);
  private data = inject<SalaryRecordDialogData>(MAT_DIALOG_DATA);

  currencies = CURRENCIES;
  reasons = REASONS;

  form = this.fb.group({
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    currencyCode: [this.data.currencyCode, Validators.required],
    effectiveDate: [new Date().toISOString().substring(0, 10), Validators.required],
    reason: ['Merit increase', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const request: SalaryRecordCreateRequest = {
      amount: Number(value.amount),
      currencyCode: value.currencyCode!,
      effectiveDate: value.effectiveDate!,
      reason: value.reason!,
    };
    this.dialogRef.close(request);
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
