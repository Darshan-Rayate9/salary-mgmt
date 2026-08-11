package com.acme.salary.service;

import com.acme.salary.dto.EmployeeCreateRequest;
import com.acme.salary.dto.EmployeeSummary;
import com.acme.salary.dto.EmployeeUpdateRequest;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.entity.SalaryRecord;
import com.acme.salary.exception.ConflictException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.acme.salary.support.EmployeeBuilder.anEmployee;
import static com.acme.salary.support.SalaryRecordBuilder.aSalaryRecord;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SalaryRecordRepository salaryRecordRepository;

    private EmployeeService newService() {
        return new EmployeeService(employeeRepository, salaryRecordRepository);
    }

    @Test
    void getEmployee_unknownId_throwsResourceNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newService().getEmployee(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getEmployee_found_includesLatestSalary() {
        Employee employee = anEmployee().withId(1L).withCode("E-1001").build();
        SalaryRecord latest = aSalaryRecord().forEmployee(employee).withAmount("102000.00").withCurrency("GBP").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.findFirstByEmployeeIdOrderByEffectiveDateDescIdDesc(1L))
                .thenReturn(Optional.of(latest));

        var response = newService().getEmployee(1L);

        assertThat(response.employeeCode()).isEqualTo("E-1001");
        assertThat(response.currentSalaryAmount()).isEqualByComparingTo("102000.00");
    }

    @Test
    void getEmployee_noSalaryRecordYet_currentSalaryIsNull() {
        Employee employee = anEmployee().withId(1L).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(salaryRecordRepository.findFirstByEmployeeIdOrderByEffectiveDateDescIdDesc(1L))
                .thenReturn(Optional.empty());

        var response = newService().getEmployee(1L);

        assertThat(response.currentSalaryAmount()).isNull();
    }

    @Test
    void listEmployees_mapsPageOfSummariesToPageResponse() {
        EmployeeSummary summary = new EmployeeSummary(
                2L, "E-1002", "Grace", "Hopper", "Engineering", "Engineer",
                "L3", "United States", EmploymentStatus.ACTIVE, null, null);
        when(employeeRepository.findAllSummaries(null, null, null, null, null, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1));

        var response = newService().listEmployees(null, null, null, null, null, PageRequest.of(0, 20));

        assertThat(response.items()).hasSize(1);
        assertThat(response.total()).isEqualTo(1);
        assertThat(response.page()).isEqualTo(1);
    }

    @Test
    void listEmployees_blankFiltersBecomeNull_andStatusIsParsedToEnum() {
        when(employeeRepository.findAllSummaries("ada", "Engineering", null, null, EmploymentStatus.ACTIVE, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        // blank search/country/level -> null; "active" (any case) -> EmploymentStatus.ACTIVE.
        var response = newService().listEmployees("ada", "Engineering", "  ", "", "active", PageRequest.of(0, 20));

        assertThat(response.total()).isZero();
    }

    @Test
    void listEmployees_unknownStatus_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                newService().listEmployees(null, null, null, null, "NOT_A_STATUS", PageRequest.of(0, 20)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createEmployee_duplicateEmployeeCode_throwsConflict() {
        when(employeeRepository.existsByEmployeeCode("E-1001")).thenReturn(true);

        assertThatThrownBy(() -> newService().createEmployee(sampleCreateRequest("E-1001")))
                .isInstanceOf(ConflictException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_duplicateEmail_throwsConflict() {
        when(employeeRepository.existsByEmployeeCode("E-1001")).thenReturn(false);
        when(employeeRepository.existsByEmail("ada@acme.test")).thenReturn(true);

        assertThatThrownBy(() -> newService().createEmployee(sampleCreateRequest("E-1001")))
                .isInstanceOf(ConflictException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void createEmployee_valid_savesWithActiveStatusAndNoSalaryYet() {
        when(employeeRepository.existsByEmployeeCode("E-1001")).thenReturn(false);
        when(employeeRepository.existsByEmail("ada@acme.test")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        var response = newService().createEmployee(sampleCreateRequest("E-1001"));

        assertThat(response.employeeCode()).isEqualTo("E-1001");
        assertThat(response.employmentStatus()).isEqualTo(EmploymentStatus.ACTIVE);
        assertThat(response.currentSalaryAmount()).isNull();
    }

    @Test
    void updateEmployee_emailTakenByAnotherEmployee_throwsConflict() {
        Employee employee = anEmployee().withId(1L).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmailAndIdNot("taken@acme.test", 1L)).thenReturn(true);

        EmployeeUpdateRequest request = new EmployeeUpdateRequest(
                "Ada", "Lovelace", "taken@acme.test", "Engineering", "Principal Engineer",
                "L6", "United Kingdom", "GBP", LocalDate.of(2019, 3, 1));

        assertThatThrownBy(() -> newService().updateEmployee(1L, request))
                .isInstanceOf(ConflictException.class);
        verify(employeeRepository, never()).save(any());
    }

    @Test
    void deleteEmployee_marksTerminatedAndNeverHardDeletes() {
        Employee employee = anEmployee().withId(1L).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        newService().deleteEmployee(1L);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertThat(captor.getValue().getEmploymentStatus()).isEqualTo(EmploymentStatus.TERMINATED);
        verify(employeeRepository, never()).delete(any());
    }

    private EmployeeCreateRequest sampleCreateRequest(String code) {
        return new EmployeeCreateRequest(
                code, "Ada", "Lovelace", "ada@acme.test", "Engineering", "Principal Engineer",
                "L6", "United Kingdom", "GBP", LocalDate.of(2019, 3, 1));
    }
}
