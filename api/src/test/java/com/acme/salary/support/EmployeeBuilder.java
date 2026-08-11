package com.acme.salary.support;

import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;

import java.time.LocalDate;

/**
 * Test-data builder (Object Mother) for {@link Employee}. Centralises the
 * "valid employee" defaults that were previously duplicated across every test
 * class, so each test only has to state the fields it actually cares about:
 *
 * <pre>{@code
 * Employee e = anEmployee().withCode("E-1001").inDepartment("Sales").build();
 * }</pre>
 *
 * The id defaults to null (unsaved) so the same builder serves both mocked unit
 * tests, which set an explicit id, and repository tests, which let the database
 * generate one on save.
 */
public final class EmployeeBuilder {

    private Long id;
    private String employeeCode = "E-1001";
    private String firstName = "Ada";
    private String lastName = "Lovelace";
    private String email;
    private String department = "Engineering";
    private String jobTitle = "Engineer";
    private String level = "L4";
    private String country = "United Kingdom";
    private String currencyCode = "GBP";
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;
    private LocalDate hireDate = LocalDate.of(2019, 3, 1);

    private EmployeeBuilder() {
    }

    public static EmployeeBuilder anEmployee() {
        return new EmployeeBuilder();
    }

    public EmployeeBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public EmployeeBuilder withCode(String employeeCode) {
        this.employeeCode = employeeCode;
        return this;
    }

    public EmployeeBuilder withName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        return this;
    }

    public EmployeeBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public EmployeeBuilder inDepartment(String department) {
        this.department = department;
        return this;
    }

    public EmployeeBuilder withJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public EmployeeBuilder atLevel(String level) {
        this.level = level;
        return this;
    }

    public EmployeeBuilder inCountry(String country) {
        this.country = country;
        return this;
    }

    public EmployeeBuilder withCurrency(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public EmployeeBuilder withStatus(EmploymentStatus employmentStatus) {
        this.employmentStatus = employmentStatus;
        return this;
    }

    public EmployeeBuilder hiredOn(LocalDate hireDate) {
        this.hireDate = hireDate;
        return this;
    }

    public Employee build() {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setEmployeeCode(employeeCode);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        // Default the email off the (unique) employee code so repository tests can
        // persist several employees without colliding on the unique-email constraint.
        employee.setEmail(email != null ? email : employeeCode.toLowerCase() + "@acme.test");
        employee.setDepartment(department);
        employee.setJobTitle(jobTitle);
        employee.setLevel(level);
        employee.setCountry(country);
        employee.setCurrencyCode(currencyCode);
        employee.setEmploymentStatus(employmentStatus);
        employee.setHireDate(hireDate);
        return employee;
    }
}
