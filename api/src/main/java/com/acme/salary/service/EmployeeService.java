package com.acme.salary.service;

import com.acme.salary.dto.EmployeeCreateRequest;
import com.acme.salary.dto.EmployeeDetailResponse;
import com.acme.salary.dto.EmployeeResponse;
import com.acme.salary.dto.EmployeeUpdateRequest;
import com.acme.salary.dto.PageResponse;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import com.acme.salary.entity.SalaryRecord;
import com.acme.salary.exception.ConflictException;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SalaryRecordRepository salaryRecordRepository;

    public PageResponse<EmployeeResponse> listEmployees(
            String search, String department, String country, String level, String status, Pageable pageable) {
        Page<EmployeeResponse> page = employeeRepository
                .findAllSummaries(
                        blankToNull(search), blankToNull(department), blankToNull(country),
                        blankToNull(level), parseStatus(status), pageable)
                .map(EmployeeResponse::from);
        return PageResponse.from(page);
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private static EmploymentStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return EmploymentStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown employment status: " + status);
        }
    }

    public EmployeeDetailResponse getEmployee(Long id) {
        Employee employee = requireEmployee(id);
        SalaryRecord latest = salaryRecordRepository
                .findFirstByEmployeeIdOrderByEffectiveDateDescIdDesc(id)
                .orElse(null);
        return EmployeeDetailResponse.from(employee, latest);
    }

    @Transactional
    public EmployeeDetailResponse createEmployee(EmployeeCreateRequest request) {
        if (employeeRepository.existsByEmployeeCode(request.employeeCode())) {
            throw new ConflictException(
                    "DUPLICATE_ENTRY", "An employee with code " + request.employeeCode() + " already exists");
        }
        if (employeeRepository.existsByEmail(request.email())) {
            throw new ConflictException("DUPLICATE_ENTRY", "An employee with email " + request.email() + " already exists");
        }

        Employee employee = new Employee();
        employee.setEmployeeCode(request.employeeCode());
        employee.setEmploymentStatus(EmploymentStatus.ACTIVE);
        applyCoreFields(employee, request.firstName(), request.lastName(), request.email(),
                request.department(), request.jobTitle(), request.level(), request.country(),
                request.currencyCode(), request.hireDate());

        employee = employeeRepository.save(employee);
        return EmployeeDetailResponse.from(employee, null);
    }

    @Transactional
    public EmployeeDetailResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = requireEmployee(id);

        if (employeeRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("DUPLICATE_ENTRY", "An employee with email " + request.email() + " already exists");
        }

        applyCoreFields(employee, request.firstName(), request.lastName(), request.email(),
                request.department(), request.jobTitle(), request.level(), request.country(),
                request.currencyCode(), request.hireDate());

        employee = employeeRepository.save(employee);
        SalaryRecord latest = salaryRecordRepository
                .findFirstByEmployeeIdOrderByEffectiveDateDescIdDesc(id)
                .orElse(null);
        return EmployeeDetailResponse.from(employee, latest);
    }

    /**
     * Soft-delete: mark the employee TERMINATED and keep the record (and its
     * salary history) for audit. Termination is a real business event that
     * headcount and attrition analytics still need, so we never hard-delete.
     */
    @Transactional
    public void deleteEmployee(Long id) {
        Employee employee = requireEmployee(id);
        employee.setEmploymentStatus(EmploymentStatus.TERMINATED);
        employeeRepository.save(employee);
    }

    private Employee requireEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EMPLOYEE_NOT_FOUND", "No employee with id " + id));
    }

    private void applyCoreFields(
            Employee employee, String firstName, String lastName, String email, String department,
            String jobTitle, String level, String country, String currencyCode, java.time.LocalDate hireDate) {
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        employee.setEmail(email);
        employee.setDepartment(department);
        employee.setJobTitle(jobTitle);
        employee.setLevel(level);
        employee.setCountry(country);
        employee.setCurrencyCode(currencyCode);
        employee.setHireDate(hireDate);
    }
}
