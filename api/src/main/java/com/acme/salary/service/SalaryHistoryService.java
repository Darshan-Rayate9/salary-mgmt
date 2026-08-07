package com.acme.salary.service;

import com.acme.salary.dto.SalaryRecordCreateRequest;
import com.acme.salary.dto.SalaryRecordResponse;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.SalaryRecord;
import com.acme.salary.exception.ResourceNotFoundException;
import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.repository.SalaryRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SalaryHistoryService {

    private final EmployeeRepository employeeRepository;
    private final SalaryRecordRepository salaryRecordRepository;
    private final CurrencyConversionService currencyConversionService;

    public SalaryHistoryService(
            EmployeeRepository employeeRepository,
            SalaryRecordRepository salaryRecordRepository,
            CurrencyConversionService currencyConversionService) {
        this.employeeRepository = employeeRepository;
        this.salaryRecordRepository = salaryRecordRepository;
        this.currencyConversionService = currencyConversionService;
    }

    public List<SalaryRecordResponse> listHistory(Long employeeId) {
        requireEmployee(employeeId);
        return salaryRecordRepository.findByEmployeeIdOrderByEffectiveDateDescIdDesc(employeeId).stream()
                .map(SalaryRecordResponse::from)
                .toList();
    }

    /**
     * Salary changes are appended, never edited or replacing a prior record -
     * this is the entire mechanism behind "current salary" and the audit
     * trail described in ARCHITECTURE.md.
     */
    @Transactional
    public SalaryRecordResponse addRecord(Long employeeId, SalaryRecordCreateRequest request) {
        Employee employee = requireEmployee(employeeId);

        if (!currencyConversionService.isSupported(request.currencyCode())) {
            throw new IllegalArgumentException("Unsupported currency code: " + request.currencyCode());
        }

        SalaryRecord record = new SalaryRecord();
        record.setEmployee(employee);
        record.setAmount(request.amount());
        record.setCurrencyCode(request.currencyCode());
        record.setUsdEquivalent(currencyConversionService.toUsd(request.amount(), request.currencyCode()));
        record.setEffectiveDate(request.effectiveDate());
        record.setReason(request.reason());

        record = salaryRecordRepository.save(record);
        return SalaryRecordResponse.from(record);
    }

    private Employee requireEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EMPLOYEE_NOT_FOUND", "No employee with id " + employeeId));
    }
}
