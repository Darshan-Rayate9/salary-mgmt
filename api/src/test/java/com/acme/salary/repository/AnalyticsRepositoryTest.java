package com.acme.salary.repository;

import com.acme.salary.dto.GroupAggregateResponse;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.entity.SalaryRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @SpringBootTest rather than a JDBC-only slice: AnalyticsRepository is plain
 * JdbcTemplate, but seeding fixtures through the JPA repositories (already
 * proven in EmployeeRepositoryTest) is simpler than hand-rolling SQL inserts,
 * and this is exactly the hand-written aggregate SQL that most needs a real
 * database behind it rather than a mock.
 *
 * @Transactional here is load-bearing, not decoration: plain @SpringBootTest
 * commits for real, so without it every test method's inserted employees
 * would leak into the next method's headcount assertions. This rolls each
 * test method back, the same mechanism @DataJpaTest uses automatically.
 */
@SpringBootTest
@Transactional
class AnalyticsRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Test
    void summary_computesHeadcountAvgMedianMinMaxTotal() {
        seedEmployee("E-S1", "Engineering", "L4", "USA", "100000.00");
        seedEmployee("E-S2", "Engineering", "L5", "USA", "200000.00");
        seedEmployee("E-S3", "Engineering", "L6", "USA", "300000.00");

        var summary = analyticsRepository.summary();

        assertThat(summary.headcount()).isEqualTo(3);
        assertThat(summary.avgSalaryUsd()).isEqualByComparingTo("200000.00");
        assertThat(summary.medianSalaryUsd()).isEqualByComparingTo("200000.00");
        assertThat(summary.minSalaryUsd()).isEqualByComparingTo("100000.00");
        assertThat(summary.maxSalaryUsd()).isEqualByComparingTo("300000.00");
        assertThat(summary.totalPayrollCostUsd()).isEqualByComparingTo("600000.00");
    }

    @Test
    void summary_excludesTerminatedAndSalarylessEmployees() {
        seedEmployee("E-S4", "Sales", "L3", "USA", "80000.00");
        Employee terminated = seedEmployee("E-S5", "Sales", "L3", "USA", "80000.00");
        terminated.setEmploymentStatus(EmploymentStatus.TERMINATED);
        // saveAndFlush, not save: AnalyticsRepository reads via plain JdbcTemplate
        // on the same connection, bypassing Hibernate's session entirely - it
        // can't see a pending, unflushed change sitting in the persistence
        // context. A plain save() here would leave the UPDATE unsent until
        // Hibernate's own auto-flush triggers (end of transaction, or before a
        // JPQL/Criteria query) - neither of which this test causes.
        employeeRepository.saveAndFlush(terminated);
        seedEmployeeWithNoSalary("E-S6", "Sales", "L3", "USA");

        var summary = analyticsRepository.summary();

        assertThat(summary.headcount()).isEqualTo(1);
    }

    @Test
    void byDepartment_groupsIndependently() {
        seedEmployee("E-D1", "Engineering", "L4", "USA", "150000.00");
        seedEmployee("E-D2", "Engineering", "L4", "USA", "170000.00");
        seedEmployee("E-D3", "Sales", "L4", "USA", "90000.00");

        List<GroupAggregateResponse> byDept = analyticsRepository.byDepartment();

        var engineering = byDept.stream().filter(g -> g.groupName().equals("Engineering")).findFirst().orElseThrow();
        assertThat(engineering.headcount()).isEqualTo(2);
        assertThat(engineering.avgSalaryUsd()).isEqualByComparingTo("160000.00");

        var sales = byDept.stream().filter(g -> g.groupName().equals("Sales")).findFirst().orElseThrow();
        assertThat(sales.headcount()).isEqualTo(1);
        assertThat(sales.avgSalaryUsd()).isEqualByComparingTo("90000.00");
    }

    @Test
    void distribution_bucketsByRange() {
        seedEmployee("E-B1", "Engineering", "L2", "USA", "40000.00"); // < $50k
        seedEmployee("E-B2", "Engineering", "L4", "USA", "120000.00"); // $100k-$150k
        seedEmployee("E-B3", "Engineering", "L7", "USA", "300000.00"); // $250k+

        var buckets = analyticsRepository.distribution();

        assertThat(buckets).extracting("bucketLabel").contains("< $50k", "$100k - $150k", "$250k+");
    }

    private Employee seedEmployee(String code, String department, String level, String country, String usdSalary) {
        Employee employee = seedEmployeeWithNoSalary(code, department, level, country);

        SalaryRecord record = new SalaryRecord();
        record.setEmployee(employee);
        record.setAmount(new BigDecimal(usdSalary));
        record.setCurrencyCode("USD");
        record.setUsdEquivalent(new BigDecimal(usdSalary));
        record.setEffectiveDate(LocalDate.of(2024, 1, 1));
        record.setReason("Base salary");
        salaryRecordRepository.save(record);

        return employee;
    }

    private Employee seedEmployeeWithNoSalary(String code, String department, String level, String country) {
        Employee employee = new Employee();
        employee.setEmployeeCode(code);
        employee.setFirstName("Test");
        employee.setLastName(code);
        employee.setEmail(code.toLowerCase() + "@acme.test");
        employee.setDepartment(department);
        employee.setJobTitle("Engineer");
        employee.setLevel(level);
        employee.setCountry(country);
        employee.setCurrencyCode("USD");
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        return employeeRepository.save(employee);
    }
}
