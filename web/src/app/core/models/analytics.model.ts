export interface AnalyticsSummary {
  headcount: number;
  avgSalaryUsd: number;
  medianSalaryUsd: number;
  minSalaryUsd: number;
  maxSalaryUsd: number;
  totalPayrollCostUsd: number;
}

export interface GroupAggregate {
  groupName: string;
  headcount: number;
  avgSalaryUsd: number;
  medianSalaryUsd: number;
  minSalaryUsd: number;
  maxSalaryUsd: number;
  totalPayrollCostUsd: number;
}

export interface DistributionBucket {
  bucketLabel: string;
  count: number;
}
