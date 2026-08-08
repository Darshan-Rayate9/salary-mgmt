export type EmploymentStatus = 'ACTIVE' | 'TERMINATED';

export interface EmployeeSummary {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  department: string;
  jobTitle: string;
  level: string;
  country: string;
  employmentStatus: EmploymentStatus;
  currentSalaryAmount: number | null;
  currentSalaryCurrency: string | null;
}

export interface EmployeeDetail extends EmployeeSummary {
  email: string;
  currencyCode: string;
  hireDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface EmployeeCreateRequest {
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  department: string;
  jobTitle: string;
  level: string;
  country: string;
  currencyCode: string;
  hireDate: string;
}

export type EmployeeUpdateRequest = Omit<EmployeeCreateRequest, 'employeeCode'>;

export interface SalaryRecord {
  id: number;
  amount: number;
  currencyCode: string;
  usdEquivalent: number;
  effectiveDate: string;
  reason: string;
  createdAt: string;
}

export interface SalaryRecordCreateRequest {
  amount: number;
  currencyCode: string;
  effectiveDate: string;
  reason: string;
}

export interface EmployeeFilters {
  search?: string;
  department?: string;
  country?: string;
  level?: string;
  status?: EmploymentStatus | '';
}

export interface PageResponse<T> {
  items: T[];
  page: number;
  limit: number;
  total: number;
  totalPages: number;
  hasNext: boolean;
  hasPrev: boolean;
}
