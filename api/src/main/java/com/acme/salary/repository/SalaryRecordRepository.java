package com.acme.salary.repository;

import com.acme.salary.entity.SalaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SalaryRecordRepository extends JpaRepository<SalaryRecord, Long> {

    List<SalaryRecord> findByEmployeeIdOrderByEffectiveDateDescIdDesc(Long employeeId);

    // id DESC as an explicit tiebreaker for two records sharing an effectiveDate -
    // deterministic ("most recently added wins"), unlike a bare MAX(effectiveDate).
    Optional<SalaryRecord> findFirstByEmployeeIdOrderByEffectiveDateDescIdDesc(Long employeeId);
}
