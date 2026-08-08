package com.acme.salary.entity;

/**
 * Employment state. TERMINATED is a real business event preserved permanently:
 * deleting an employee is always a soft-delete (mark TERMINATED, keep the row
 * and its salary history for audit) - there is no hard-delete path. See
 * EmployeeService#deleteEmployee and ARCHITECTURE.md.
 */
public enum EmploymentStatus {
    ACTIVE,
    TERMINATED
}
