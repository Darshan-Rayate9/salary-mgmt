package com.acme.salary.config;

import com.acme.salary.entity.Employee;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRecordRepository;
import com.acme.salary.service.CurrencyConversionService;
import com.acme.salary.service.EmployeeBulkWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EmployeeSeeder is @Profile("!test") so it never runs as a bean in the test
 * context (see src/test/resources/application.yml) - constructed directly
 * here instead, with a small count, against the real schema.
 */
@SpringBootTest
@Transactional
class EmployeeSeederTest {

    // Deliberately not a round number or a multiple of the 500-row chunk size,
    // so the seeder's trailing partial-chunk flush is exercised.
    private static final int SEED_COUNT = 47;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    @Autowired
    private EmployeeBulkWriter bulkWriter;

    @Autowired
    private CurrencyConversionService currencyConversionService;

    private void seed(int count) {
        new EmployeeSeeder(employeeRepository, bulkWriter, currencyConversionService, count).run();
    }

    @Test
    void run_seedsTheRequestedNumberOfEmployees() {
        seed(SEED_COUNT);

        assertThat(employeeRepository.count()).isEqualTo(SEED_COUNT);
    }

    @Test
    void run_givesEveryEmployeeAUniqueCode() {
        seed(SEED_COUNT);

        assertThat(employeeRepository.findAll())
                .extracting(Employee::getEmployeeCode)
                .doesNotHaveDuplicates();
    }

    @Test
    void run_givesEveryEmployeeAUniqueEmail() {
        seed(SEED_COUNT);

        assertThat(employeeRepository.findAll())
                .extracting(Employee::getEmail)
                .doesNotHaveDuplicates();
    }

    @Test
    void run_givesEveryEmployeeSalaryHistoryWithAPositiveUsdSnapshot() {
        seed(SEED_COUNT);

        // The USD conversion maths itself is proven in CurrencyConversionServiceTest;
        // here we only need that the seeder populated a salary history and a USD
        // snapshot for each employee - hence a positive value, not a re-derived one.
        assertThat(employeeRepository.findAll()).allSatisfy(employee ->
                assertThat(salaryRecordRepository.findByEmployeeIdOrderByEffectiveDateDescIdDesc(employee.getId()))
                        .isNotEmpty()
                        .allSatisfy(record -> assertThat(record.getUsdEquivalent()).isPositive()));
    }

    @Test
    void run_isIdempotent_doesNotReseedIfEmployeesAlreadyExist() {
        seed(10);
        assertThat(employeeRepository.count()).isEqualTo(10);

        seed(10);
        assertThat(employeeRepository.count()).isEqualTo(10); // unchanged, not 20
    }
}
