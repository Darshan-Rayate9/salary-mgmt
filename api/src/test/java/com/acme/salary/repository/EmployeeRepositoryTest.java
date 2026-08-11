package com.acme.salary.repository;

import com.acme.salary.dto.EmployeeSummary;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;

import static com.acme.salary.support.EmployeeBuilder.anEmployee;
import static com.acme.salary.support.SalaryRecordBuilder.aSalaryRecord;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs against the real Flyway-managed SQLite schema (Replace.NONE), not an
 * auto-configured H2 - see ARCHITECTURE.md: tests should catch real migration
 * issues, not a schema that only exists in test.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    @Test
    void findAllSummaries_resolvesLatestSalaryPerEmployee() {
        Employee employee = persistEmployee("E-1001", "Ada", "Lovelace");

        saveSalaryRecord(employee, "90000.00", "114000.00", LocalDate.of(2023, 1, 1), "Base salary");
        saveSalaryRecord(employee, "102000.00", "129000.00", LocalDate.of(2024, 6, 1), "Merit increase");

        var page = employeeRepository.findAllSummaries(null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        EmployeeSummary summary = page.getContent().get(0);
        assertThat(summary.getEmployeeCode()).isEqualTo("E-1001");
        assertThat(summary.getCurrentSalaryAmount()).isEqualByComparingTo("102000.00");
        assertThat(summary.getCurrentSalaryCurrency()).isEqualTo("GBP");
    }

    @Test
    void findAllSummaries_employeeWithNoSalaryRecord_returnsNullSalary() {
        persistEmployee("E-1002", "Grace", "Hopper");

        var page = employeeRepository.findAllSummaries(null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getCurrentSalaryAmount()).isNull();
    }

    @Test
    void findSummaryById_unknownId_returnsEmpty() {
        assertThat(employeeRepository.findSummaryById(999_999L)).isEmpty();
    }

    @Test
    void findAllSummaries_filtersByDepartmentAndStatus() {
        persistEmployee("E-2001", "Ada", "Lovelace", "Engineering", EmploymentStatus.ACTIVE);
        persistEmployee("E-2002", "Grace", "Hopper", "Sales", EmploymentStatus.ACTIVE);
        persistEmployee("E-2003", "Alan", "Turing", "Engineering", EmploymentStatus.TERMINATED);

        var engineeringActive = employeeRepository.findAllSummaries(
                null, "Engineering", null, null, EmploymentStatus.ACTIVE, PageRequest.of(0, 10));

        assertThat(engineeringActive.getTotalElements()).isEqualTo(1);
        assertThat(engineeringActive.getContent().get(0).getEmployeeCode()).isEqualTo("E-2001");
    }

    @Test
    void findAllSummaries_searchMatchesNameOrCodeCaseInsensitively() {
        persistEmployee("E-3001", "Ada", "Lovelace", "Engineering", EmploymentStatus.ACTIVE);
        persistEmployee("E-3002", "Grace", "Hopper", "Engineering", EmploymentStatus.ACTIVE);

        assertThat(employeeRepository.findAllSummaries("lovel", null, null, null, null, PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(1);
        assertThat(employeeRepository.findAllSummaries("e-3002", null, null, null, null, PageRequest.of(0, 10))
                .getTotalElements()).isEqualTo(1);
        assertThat(employeeRepository.findAllSummaries("nomatch", null, null, null, null, PageRequest.of(0, 10))
                .getTotalElements()).isZero();
    }

    private Employee persistEmployee(String code, String firstName, String lastName) {
        return persistEmployee(code, firstName, lastName, "Engineering", EmploymentStatus.ACTIVE);
    }

    private Employee persistEmployee(
            String code, String firstName, String lastName, String department, EmploymentStatus status) {
        return employeeRepository.save(anEmployee()
                .withCode(code).withName(firstName, lastName).inDepartment(department).withStatus(status)
                .build());
    }

    private void saveSalaryRecord(
            Employee employee, String amount, String usdEquivalent, LocalDate effectiveDate, String reason) {
        salaryRecordRepository.save(aSalaryRecord()
                .forEmployee(employee).withAmount(amount).withUsdEquivalent(usdEquivalent)
                .effectiveOn(effectiveDate).because(reason)
                .build());
    }
}
