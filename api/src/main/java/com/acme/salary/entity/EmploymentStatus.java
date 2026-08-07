package com.acme.salary.entity;

/**
 * ACTIVE / TERMINATED is a real business event and is preserved permanently
 * (see ARCHITECTURE.md "soft-delete vs. data-entry correction"). It is not the
 * mechanism for deleting an employee record created by mistake - a record with
 * no attached SalaryRecord yet can be hard-deleted instead.
 */
public enum EmploymentStatus {
    ACTIVE,
    TERMINATED
}
