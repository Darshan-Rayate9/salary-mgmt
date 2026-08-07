package com.acme.salary.repository;

import com.acme.salary.dto.EmployeeSummary;
import com.acme.salary.entity.Employee;
import com.acme.salary.entity.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    /**
     * Resolves "current salary" (latest SalaryRecord per employee) for a whole
     * page in one query - never per-row lazy-loading, which would turn a
     * 50-row page into 51 queries (see ARCHITECTURE.md).
     *
     * Explicit countQuery on purpose: Spring Data's automatic count-query
     * derivation strips the SELECT clause of the main query to build a COUNT,
     * which is unreliable for a "SELECT new ...(...)" constructor expression
     * with nested subqueries like this one - found by an integration test
     * that created employees then immediately listed them and got an empty
     * page back despite the rows genuinely being there. A plain
     * "SELECT COUNT(e) ..." sidesteps the derivation entirely - and must carry
     * the same optional-filter WHERE clause as the value query so the total
     * matches the filtered result set.
     *
     * Filters use the "(:param IS NULL OR ...)" idiom so a single query serves
     * every combination of present/absent filters - null means "don't filter
     * on this". Search matches employeeCode/first/last name, case-insensitively.
     *
     * Known limitation: if two records for the same employee share the exact
     * same max effectiveDate, this scalar subquery can return more than one
     * row and the query will fail at runtime. Acceptable for the scaffold;
     * harden with an explicit tiebreaker (e.g. highest id) before relying on
     * this for real data entry.
     */
    @Query(
            value = """
                    SELECT new com.acme.salary.dto.EmployeeSummary(
                        e.id, e.employeeCode, e.firstName, e.lastName, e.department, e.jobTitle,
                        e.level, e.country, e.employmentStatus,
                        (SELECT sr.amount FROM SalaryRecord sr
                         WHERE sr.employee.id = e.id
                         AND sr.effectiveDate = (SELECT MAX(sr2.effectiveDate) FROM SalaryRecord sr2 WHERE sr2.employee.id = e.id)),
                        (SELECT sr.currencyCode FROM SalaryRecord sr
                         WHERE sr.employee.id = e.id
                         AND sr.effectiveDate = (SELECT MAX(sr2.effectiveDate) FROM SalaryRecord sr2 WHERE sr2.employee.id = e.id))
                    )
                    FROM Employee e
                    WHERE (:search IS NULL
                           OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))
                      AND (:department IS NULL OR e.department = :department)
                      AND (:country IS NULL OR e.country = :country)
                      AND (:level IS NULL OR e.level = :level)
                      AND (:status IS NULL OR e.employmentStatus = :status)
                    """,
            countQuery = """
                    SELECT COUNT(e) FROM Employee e
                    WHERE (:search IS NULL
                           OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                           OR LOWER(e.employeeCode) LIKE LOWER(CONCAT('%', :search, '%')))
                      AND (:department IS NULL OR e.department = :department)
                      AND (:country IS NULL OR e.country = :country)
                      AND (:level IS NULL OR e.level = :level)
                      AND (:status IS NULL OR e.employmentStatus = :status)
                    """)
    Page<EmployeeSummary> findAllSummaries(
            @Param("search") String search,
            @Param("department") String department,
            @Param("country") String country,
            @Param("level") String level,
            @Param("status") EmploymentStatus status,
            Pageable pageable);

    @Query("""
            SELECT new com.acme.salary.dto.EmployeeSummary(
                e.id, e.employeeCode, e.firstName, e.lastName, e.department, e.jobTitle,
                e.level, e.country, e.employmentStatus,
                (SELECT sr.amount FROM SalaryRecord sr
                 WHERE sr.employee.id = e.id
                 AND sr.effectiveDate = (SELECT MAX(sr2.effectiveDate) FROM SalaryRecord sr2 WHERE sr2.employee.id = e.id)),
                (SELECT sr.currencyCode FROM SalaryRecord sr
                 WHERE sr.employee.id = e.id
                 AND sr.effectiveDate = (SELECT MAX(sr2.effectiveDate) FROM SalaryRecord sr2 WHERE sr2.employee.id = e.id))
            )
            FROM Employee e
            WHERE e.id = :id
            """)
    Optional<EmployeeSummary> findSummaryById(Long id);
}
