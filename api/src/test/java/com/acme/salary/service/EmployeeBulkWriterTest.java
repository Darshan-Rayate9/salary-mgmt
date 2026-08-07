package com.acme.salary.service;

import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @SpringBootTest against the real schema: EmployeeBulkWriter is raw JDBC
 * (generated-key retrieval, JDBC batching) specifically because JPA's
 * per-entity overhead isn't acceptable at seed-script scale - that's exactly
 * the code that most needs a real database behind its tests, not a mock.
 */
@SpringBootTest
@Transactional
class EmployeeBulkWriterTest {

    @Autowired
    private EmployeeBulkWriter bulkWriter;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SalaryRecordRepository salaryRecordRepository;

    @Test
    void insertEmployees_returnsGeneratedIdsInInputOrder() {
        List<NewEmployeeRow> rows = List.of(
                newRow("E-3001", "Ada", "Lovelace"),
                newRow("E-3002", "Grace", "Hopper"));

        List<Long> ids = bulkWriter.insertEmployees(rows);

        assertThat(ids).hasSize(2);
        assertThat(ids.get(0)).isNotEqualTo(ids.get(1));

        var first = employeeRepository.findById(ids.get(0)).orElseThrow();
        assertThat(first.getEmployeeCode()).isEqualTo("E-3001");
        var second = employeeRepository.findById(ids.get(1)).orElseThrow();
        assertThat(second.getEmployeeCode()).isEqualTo("E-3002");
    }

    @Test
    void insertSalaryRecords_batchInsertsAgainstRealForeignKeys() {
        List<Long> ids = bulkWriter.insertEmployees(List.of(newRow("E-3003", "Alan", "Turing")));
        Long employeeId = ids.get(0);

        bulkWriter.insertSalaryRecords(List.of(
                new NewSalaryRow(employeeId, new BigDecimal("90000.00"), "USD", new BigDecimal("90000.00"),
                        LocalDate.of(2020, 1, 1), "Initial salary"),
                new NewSalaryRow(employeeId, new BigDecimal("105000.00"), "USD", new BigDecimal("105000.00"),
                        LocalDate.of(2023, 1, 1), "Merit increase")));

        var history = salaryRecordRepository.findByEmployeeIdOrderByEffectiveDateDescIdDesc(employeeId);
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getReason()).isEqualTo("Merit increase"); // newest first
    }

    @Test
    void insertSalaryRecords_emptyList_isANoOp() {
        bulkWriter.insertSalaryRecords(List.of());
        // No exception, nothing to assert beyond "didn't blow up" - guards against a batchUpdate
        // call with zero rows, which some JDBC drivers reject.
    }

    private NewEmployeeRow newRow(String code, String firstName, String lastName) {
        return new NewEmployeeRow(
                code, firstName, lastName, code.toLowerCase() + "@acme.test", "Engineering", "Engineer",
                "L4", "United States", "USD", "ACTIVE", LocalDate.of(2022, 1, 1));
    }
}
