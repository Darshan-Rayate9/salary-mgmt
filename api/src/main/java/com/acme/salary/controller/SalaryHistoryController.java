package com.acme.salary.controller;

import com.acme.salary.dto.SalaryRecordCreateRequest;
import com.acme.salary.dto.SalaryRecordResponse;
import com.acme.salary.service.SalaryHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/salary-history")
@RequiredArgsConstructor
public class SalaryHistoryController {

    private final SalaryHistoryService salaryHistoryService;

    @GetMapping
    public List<SalaryRecordResponse> list(@PathVariable Long employeeId) {
        return salaryHistoryService.listHistory(employeeId);
    }

    @PostMapping
    public ResponseEntity<SalaryRecordResponse> add(
            @PathVariable Long employeeId, @Valid @RequestBody SalaryRecordCreateRequest request) {
        SalaryRecordResponse created = salaryHistoryService.addRecord(employeeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
