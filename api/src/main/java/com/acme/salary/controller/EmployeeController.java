package com.acme.salary.controller;

import com.acme.salary.dto.EmployeeCreateRequest;
import com.acme.salary.dto.EmployeeDetailResponse;
import com.acme.salary.dto.EmployeeResponse;
import com.acme.salary.dto.EmployeeUpdateRequest;
import com.acme.salary.dto.PageResponse;
import com.acme.salary.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public PageResponse<EmployeeResponse> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        return employeeService.listEmployees(search, department, country, level, status, pageable);
    }

    @GetMapping("/{id}")
    public EmployeeDetailResponse getOne(@PathVariable Long id) {
        return employeeService.getEmployee(id);
    }

    @PostMapping
    public ResponseEntity<EmployeeDetailResponse> create(@Valid @RequestBody EmployeeCreateRequest request) {
        EmployeeDetailResponse created = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public EmployeeDetailResponse update(@PathVariable Long id, @Valid @RequestBody EmployeeUpdateRequest request) {
        return employeeService.updateEmployee(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
}
