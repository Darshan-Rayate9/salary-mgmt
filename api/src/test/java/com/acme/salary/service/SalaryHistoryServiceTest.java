package com.acme.salary.service;

import com.acme.salary.dto.SalaryRecordCreateRequest;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.SalaryRecord;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalaryHistoryServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRecordRepository salaryRecordRepository;

    private SalaryHistoryService newService() {
        // Real CurrencyConversionService (not mocked) - it's a pure fixed-table
        // lookup, cheap and deterministic, so exercising the real thing here
        // is more meaningful than mocking it.
        return new SalaryHistoryService(employeeRepository, salaryRecordRepository, new CurrencyConversionService());
    }

    @Test
    void listHistory_unknownEmployee_throwsResourceNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().listHistory(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listHistory_returnsRecordsNewestFirst() {
        Employee employee = sampleEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        SalaryRecord record = sampleRecord(employee);
        when(salaryRecordRepository.findByEmployeeIdOrderByEffectiveDateDescIdDesc(1L))
                .thenReturn(List.of(record));

        var history = newService().listHistory(1L);

        assertThat(history).hasSize(1);
        assertThat(history.get(0).amount()).isEqualByComparingTo("102000.00");
    }

    @Test
    void addRecord_computesUsdEquivalentViaFixedRateTable() {
        Employee employee = sampleEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.save(any(SalaryRecord.class))).thenAnswer(inv -> {
            SalaryRecord r = inv.getArgument(0);
            r.setId(10L);
            return r;
        });

        var request = new SalaryRecordCreateRequest(
                new BigDecimal("100000.00"), "GBP", LocalDate.of(2024, 6, 1), "Merit increase");
        var response = newService().addRecord(1L, request);

        // 100000 GBP * 1.27 = 127000.00 USD, per CurrencyConversionService's fixed rate.
        assertThat(response.usdEquivalent()).isEqualByComparingTo("127000.00");
        assertThat(response.currencyCode()).isEqualTo("GBP");

        ArgumentCaptor<SalaryRecord> captor = ArgumentCaptor.forClass(SalaryRecord.class);
        verify(salaryRecordRepository).save(captor.capture());
        assertThat(captor.getValue().getEmployee()).isSameAs(employee);
    }

    @Test
    void addRecord_unsupportedCurrency_throwsIllegalArgument() {
        Employee employee = sampleEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var request = new SalaryRecordCreateRequest(
                new BigDecimal("100000.00"), "XXX", LocalDate.of(2024, 6, 1), "Merit increase");

        assertThatThrownBy(() -> newService().addRecord(1L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addRecord_futureEffectiveDate_throwsIllegalArgument() {
        Employee employee = sampleEmployee();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        // A salary can't take effect before it's even been decided.
        var request = new SalaryRecordCreateRequest(
                new BigDecimal("100000.00"), "GBP", LocalDate.now().plusDays(1), "Merit increase");

        assertThatThrownBy(() -> newService().addRecord(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("future");
    }

    @Test
    void addRecord_effectiveDateBeforeHireDate_throwsIllegalArgument() {
        Employee employee = sampleEmployee(); // hired 2019-03-01
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var request = new SalaryRecordCreateRequest(
                new BigDecimal("100000.00"), "GBP", LocalDate.of(2018, 1, 1), "Back-dated record");

        assertThatThrownBy(() -> newService().addRecord(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("hire date");
    }

    private Employee sampleEmployee() {
        Employee employee = new Employee();
        employee.setId(1L);
        employee.setEmployeeCode("E-1001");
        employee.setFirstName("Ada");
        employee.setLastName("Lovelace");
        employee.setHireDate(LocalDate.of(2019, 3, 1));
        return employee;
    }

    private SalaryRecord sampleRecord(Employee employee) {
        SalaryRecord record = new SalaryRecord();
        record.setId(1L);
        record.setEmployee(employee);
        record.setAmount(new BigDecimal("102000.00"));
        record.setCurrencyCode("GBP");
        record.setUsdEquivalent(new BigDecimal("129540.00"));
        record.setEffectiveDate(LocalDate.of(2024, 6, 1));
        record.setReason("Merit increase");
        return record;
    }
}
