package com.acme.salary.repository;

import com.acme.salary.dto.AnalyticsSummaryResponse;
import com.acme.salary.dto.DistributionBucketResponse;
import com.acme.salary.dto.GroupAggregateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only aggregate queries against the current_salary view (see V2
 * migration). Deliberately plain JdbcTemplate rather than JPA/JPQL here:
 * these are pure reporting queries with grouping, window functions for
 * median, and a CTE for outliers - forcing that through an ORM buys nothing
 * and JdbcTemplate keeps the actual SQL visible and easy to reason about.
 */
@Repository
@RequiredArgsConstructor
public class AnalyticsRepository {

    private static final String ACTIVE_FILTER =
            "employment_status = 'ACTIVE' AND usd_equivalent IS NOT NULL";

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsSummaryResponse summary() {
        Map<String, Object> stats = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS headcount, AVG(usd_equivalent) AS avg_salary,
                       MIN(usd_equivalent) AS min_salary, MAX(usd_equivalent) AS max_salary,
                       SUM(usd_equivalent) AS total_payroll
                FROM current_salary WHERE %s
                """.formatted(ACTIVE_FILTER));

        BigDecimal median = jdbcTemplate.queryForObject("""
                SELECT AVG(usd_equivalent) FROM (
                    SELECT usd_equivalent,
                           ROW_NUMBER() OVER (ORDER BY usd_equivalent) AS rn,
                           COUNT(*) OVER () AS cnt
                    FROM current_salary WHERE %s
                ) WHERE rn IN ((cnt + 1) / 2, (cnt + 2) / 2)
                """.formatted(ACTIVE_FILTER), BigDecimal.class);

        return new AnalyticsSummaryResponse(
                ((Number) stats.get("headcount")).longValue(),
                asDecimal(stats.get("avg_salary")),
                median,
                asDecimal(stats.get("min_salary")),
                asDecimal(stats.get("max_salary")),
                asDecimal(stats.get("total_payroll")));
    }

    public List<GroupAggregateResponse> byDepartment() {
        return byGroupColumn("department");
    }

    public List<GroupAggregateResponse> byCountry() {
        return byGroupColumn("country");
    }

    public List<GroupAggregateResponse> byLevel() {
        return byGroupColumn("level");
    }

    private List<GroupAggregateResponse> byGroupColumn(String column) {
        List<Map<String, Object>> stats = jdbcTemplate.queryForList("""
                SELECT %1$s AS group_name, COUNT(*) AS headcount, AVG(usd_equivalent) AS avg_salary,
                       MIN(usd_equivalent) AS min_salary, MAX(usd_equivalent) AS max_salary,
                       SUM(usd_equivalent) AS total_payroll
                FROM current_salary WHERE %2$s
                GROUP BY %1$s
                ORDER BY headcount DESC
                """.formatted(column, ACTIVE_FILTER));

        List<Map<String, Object>> medians = jdbcTemplate.queryForList("""
                SELECT %1$s AS group_name, AVG(usd_equivalent) AS median FROM (
                    SELECT %1$s, usd_equivalent,
                           ROW_NUMBER() OVER (PARTITION BY %1$s ORDER BY usd_equivalent) AS rn,
                           COUNT(*) OVER (PARTITION BY %1$s) AS cnt
                    FROM current_salary WHERE %2$s
                ) WHERE rn IN ((cnt + 1) / 2, (cnt + 2) / 2)
                GROUP BY %1$s
                """.formatted(column, ACTIVE_FILTER));

        Map<String, BigDecimal> medianByGroup = new LinkedHashMap<>();
        for (Map<String, Object> row : medians) {
            medianByGroup.put((String) row.get("group_name"), asDecimal(row.get("median")));
        }

        return stats.stream()
                .map(row -> {
                    String groupName = (String) row.get("group_name");
                    return new GroupAggregateResponse(
                            groupName,
                            ((Number) row.get("headcount")).longValue(),
                            asDecimal(row.get("avg_salary")),
                            medianByGroup.get(groupName),
                            asDecimal(row.get("min_salary")),
                            asDecimal(row.get("max_salary")),
                            asDecimal(row.get("total_payroll")));
                })
                .toList();
    }

    public List<DistributionBucketResponse> distribution() {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT
                    CASE
                        WHEN usd_equivalent < 50000 THEN '< $50k'
                        WHEN usd_equivalent < 100000 THEN '$50k - $100k'
                        WHEN usd_equivalent < 150000 THEN '$100k - $150k'
                        WHEN usd_equivalent < 200000 THEN '$150k - $200k'
                        WHEN usd_equivalent < 250000 THEN '$200k - $250k'
                        ELSE '$250k+'
                    END AS bucket,
                    COUNT(*) AS count,
                    MIN(usd_equivalent) AS bucket_min
                FROM current_salary WHERE %s
                GROUP BY bucket
                ORDER BY bucket_min
                """.formatted(ACTIVE_FILTER));

        return rows.stream()
                .map(row -> new DistributionBucketResponse(
                        (String) row.get("bucket"), ((Number) row.get("count")).longValue()))
                .toList();
    }

    private static BigDecimal asDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        return new BigDecimal(value.toString());
    }
}
