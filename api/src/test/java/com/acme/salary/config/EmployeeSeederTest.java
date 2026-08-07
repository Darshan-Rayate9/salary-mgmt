package com.acme.salary.config;

import com.acme.salary.entity.Employee;
import com.acme.salary.entity.SalaryRecord;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRecordRepository;
import com.acme.salary.service.CurrencyConversionService;
import com.acme.salary.service.EmployeeBulkWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EmployeeSeeder is @Profile("!test") so it never runs as a bean in the test
 * context (see src/test/resources/application.yml) - constructed directly
 * here instead, with a small count, against the real schema.
 */
@SpringBootTest
@Transactional
class EmployeeSeederTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    @Autowired
    private EmployeeBulkWriter bulkWriter;

    @Autowired
    private CurrencyConversionService currencyConversionService;

    @Test
    void run_seedsRequestedCountWithUniqueCodesAndValidSalaries() {
        int count = 47; // deliberately not a round number or a multiple of the 500-row chunk size
        new EmployeeSeeder(employeeRepository, bulkWriter, currencyConversionService, count).run();

        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).hasSize(count);

        Set<String> codes = employees.stream().map(Employee::getEmployeeCode).collect(java.util.stream.Collectors.toSet());
        assertThat(codes).hasSize(count); // all unique

        Set<String> emails = employees.stream().map(Employee::getEmail).collect(java.util.stream.Collectors.toSet());
        assertThat(emails).hasSize(count); // all unique

        // Every employee has at least one salary record, and its usdEquivalent is
        // consistent with what CurrencyConversionService would compute directly.
        for (Employee employee : employees) {
            List<SalaryRecord> history = salaryRecordRepository
                    .findByEmployeeIdOrderByEffectiveDateDescIdDesc(employee.getId());
            assertThat(history).isNotEmpty();

            SalaryRecord latest = history.get(0);
            var expectedUsd = currencyConversionService.toUsd(latest.getAmount(), latest.getCurrencyCode());
            assertThat(latest.getUsdEquivalent()).isEqualByComparingTo(expectedUsd);
        }
    }

    @Test
    void run_isIdempotent_doesNotReseedIfEmployeesAlreadyExist() {
        new EmployeeSeeder(employeeRepository, bulkWriter, currencyConversionService, 10).run();
        assertThat(employeeRepository.count()).isEqualTo(10);

        new EmployeeSeeder(employeeRepository, bulkWriter, currencyConversionService, 10).run();
        assertThat(employeeRepository.count()).isEqualTo(10); // unchanged, not 20
    }
}
