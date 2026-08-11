package com.acme.salary.config;

import com.acme.salary.repository.EmployeeRepository;
import com.acme.salary.service.CurrencyConversionService;
import com.acme.salary.service.EmployeeBulkWriter;
import com.acme.salary.service.NewEmployeeRow;
import com.acme.salary.service.NewSalaryRow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The "Seed script with 10,000 employees" deliverable. Idempotent (checks
 * employee count first) and runs on every boot - see ARCHITECTURE.md's
 * deployment section on why a possibly-ephemeral filesystem shouldn't matter
 * for demo readiness. Excluded from the "test" profile: 10k rows of generated
 * data has no business slowing down a context-loads test (see
 * src/test/resources/application.yml).
 */
@Component
@Profile("!test")
@Slf4j
public class EmployeeSeeder implements CommandLineRunner {

    private static final int CHUNK_SIZE = 500;
    // Fixed seed: reproducible-looking demo data across runs, not cryptographic randomness.
    private static final long RANDOM_SEED = 42L;

    private record Country(String name, String currency, double costOfLivingMultiplier) {
    }

    private static final List<String> DEPARTMENTS = List.of(
            "Engineering", "Sales", "Marketing", "Product", "Design",
            "Finance", "Human Resources", "Legal", "Operations", "Customer Support");

    private static final List<Country> COUNTRIES = List.of(
            new Country("United States", "USD", 1.00),
            new Country("United Kingdom", "GBP", 0.85),
            new Country("Germany", "EUR", 0.80),
            new Country("India", "INR", 0.35),
            new Country("Canada", "CAD", 0.78),
            new Country("Australia", "AUD", 0.85),
            new Country("Singapore", "SGD", 0.90),
            new Country("Japan", "JPY", 0.75),
            new Country("Brazil", "BRL", 0.42),
            new Country("France", "EUR", 0.80));

    // level -> [minUsd, maxUsd] base salary band, and a generic title.
    private static final String[] LEVELS = {"L1", "L2", "L3", "L4", "L5", "L6", "L7"};
    private static final int[][] LEVEL_USD_BAND = {
            {45_000, 65_000}, {60_000, 85_000}, {80_000, 115_000}, {105_000, 150_000},
            {140_000, 200_000}, {185_000, 270_000}, {240_000, 360_000}
    };
    private static final String[] LEVEL_TITLE = {
            "Associate", "Analyst", "Senior Analyst", "Manager", "Senior Manager", "Director", "Vice President"
    };

    private static final String[] RAISE_REASONS = {"Merit increase", "Promotion", "Market adjustment", "Annual increase"};

    private static final String[] FIRST_NAMES = {
            "Ada", "Grace", "Alan", "Linus", "Margaret", "John", "Barbara", "Dennis", "Katherine", "Tim",
            "Radia", "Vint", "Frances", "Edsger", "Donald", "Karen", "Ken", "Anita", "Guido", "Yukihiro",
            "Priya", "Wei", "Fatima", "Carlos", "Sofia", "Hiroshi", "Elena", "Mohammed", "Aisha", "Lucas",
            "Emma", "Noah", "Olivia", "Liam", "Ava", "Ethan", "Mia", "James", "Isabella", "Benjamin",
            "Charlotte", "Lucas", "Amelia", "Henry", "Harper", "Alexander", "Evelyn", "Daniel", "Abigail", "Matthew"
    };

    private static final String[] LAST_NAMES = {
            "Lovelace", "Hopper", "Turing", "Torvalds", "Hamilton", "Backus", "Liskov", "Ritchie", "Johnson", "Berners-Lee",
            "Perlman", "Cerf", "Allen", "Dijkstra", "Knuth", "Sparck", "Thompson", "Borg", "van Rossum", "Matsumoto",
            "Sharma", "Zhang", "Khan", "Silva", "Garcia", "Tanaka", "Petrova", "Ahmed", "Diallo", "Costa",
            "Smith", "Brown", "Wilson", "Taylor", "Davies", "Evans", "Roberts", "Walker", "Wright", "Clarke",
            "Patel", "Kumar", "Nguyen", "Kim", "Rossi", "Mueller", "Dubois", "Andersson", "Kowalski", "Ivanov"
    };

    private final EmployeeRepository employeeRepository;
    private final EmployeeBulkWriter bulkWriter;
    private final CurrencyConversionService currencyConversionService;
    private final int employeeCount;

    public EmployeeSeeder(
            EmployeeRepository employeeRepository,
            EmployeeBulkWriter bulkWriter,
            CurrencyConversionService currencyConversionService,
            @Value("${app.seed.employee-count:10000}") int employeeCount) {
        this.employeeRepository = employeeRepository;
        this.bulkWriter = bulkWriter;
        this.currencyConversionService = currencyConversionService;
        this.employeeCount = employeeCount;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            return;
        }

        long startedAt = System.currentTimeMillis();
        Random random = new Random(RANDOM_SEED);
        LocalDate today = LocalDate.now();

        List<NewEmployeeRow> employeeBuffer = new ArrayList<>(CHUNK_SIZE);
        List<PendingSalaryHistory> salaryBuffer = new ArrayList<>(CHUNK_SIZE);
        int totalInserted = 0;

        for (int i = 1; i <= employeeCount; i++) {
            String employeeCode = "E-%05d".formatted(i);
            String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
            String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
            String email = "%s.%s%d@acme.com".formatted(
                    firstName.toLowerCase().replace(" ", ""), lastName.toLowerCase().replace(" ", "").replace("'", ""), i);
            String department = DEPARTMENTS.get(random.nextInt(DEPARTMENTS.size()));
            int levelIndex = random.nextInt(LEVELS.length);
            String level = LEVELS[levelIndex];
            String jobTitle = "%s, %s".formatted(LEVEL_TITLE[levelIndex], department);
            Country country = COUNTRIES.get(random.nextInt(COUNTRIES.size()));
            // 92% active, 8% terminated - a plausible attrition rate for a stable org.
            boolean terminated = random.nextInt(100) < 8;

            LocalDate hireDate = today.minusDays(random.nextInt(8 * 365) + 90);

            employeeBuffer.add(new NewEmployeeRow(
                    employeeCode, firstName, lastName, email, department, jobTitle, level, country.name(),
                    country.currency(), terminated ? "TERMINATED" : "ACTIVE", hireDate));

            salaryBuffer.add(buildSalaryHistory(random, levelIndex, country, hireDate, today));

            if (employeeBuffer.size() == CHUNK_SIZE) {
                totalInserted += flushChunk(employeeBuffer, salaryBuffer);
            }
        }

        if (!employeeBuffer.isEmpty()) {
            totalInserted += flushChunk(employeeBuffer, salaryBuffer);
        }

        log.info("Seeded {} employees with salary history in {} ms", totalInserted, System.currentTimeMillis() - startedAt);
    }

    private int flushChunk(List<NewEmployeeRow> employeeBuffer, List<PendingSalaryHistory> salaryBuffer) {
        List<Long> ids = bulkWriter.insertEmployees(employeeBuffer);

        List<NewSalaryRow> salaryRows = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Long employeeId = ids.get(i);
            for (PendingSalary record : salaryBuffer.get(i).records()) {
                salaryRows.add(new NewSalaryRow(
                        employeeId, record.amount(), record.currency(),
                        currencyConversionService.toUsd(record.amount(), record.currency()),
                        record.effectiveDate(), record.reason()));
            }
        }
        bulkWriter.insertSalaryRecords(salaryRows);

        int count = employeeBuffer.size();
        employeeBuffer.clear();
        salaryBuffer.clear();
        return count;
    }

    /** 1-3 salary records depending on tenure, trending upward to the employee's current target salary. */
    private PendingSalaryHistory buildSalaryHistory(
            Random random, int levelIndex, Country country, LocalDate hireDate, LocalDate today) {
        int[] band = LEVEL_USD_BAND[levelIndex];
        double currentTargetUsd = (band[0] + random.nextDouble() * (band[1] - band[0])) * country.costOfLivingMultiplier();

        int tenureYears = Period.between(hireDate, today).getYears();
        int recordCount = tenureYears >= 4 ? 3 : (tenureYears >= 2 ? 2 : 1);

        List<PendingSalary> records = new ArrayList<>(recordCount);
        for (int r = 0; r < recordCount; r++) {
            boolean isLatest = r == recordCount - 1;
            // Earlier records are progressively lower, trending up to currentTargetUsd at the latest record.
            double usdForRecord = isLatest ? currentTargetUsd : currentTargetUsd * Math.pow(0.88, recordCount - 1 - r);
            BigDecimal amount = usdToLocalAmount(usdForRecord, country);

            LocalDate effectiveDate = recordCount == 1
                    ? hireDate
                    : hireDate.plusDays((long) ((today.toEpochDay() - hireDate.toEpochDay()) * (r / (double) recordCount)));

            String reason = r == 0 ? "Initial salary" : RAISE_REASONS[random.nextInt(RAISE_REASONS.length)];
            records.add(new PendingSalary(amount, country.currency(), effectiveDate, reason));
        }

        return new PendingSalaryHistory(records);
    }

    private BigDecimal usdToLocalAmount(double usdTarget, Country country) {
        // Same fixed rate table used everywhere else, so a seeded employee's
        // local amount and its USD snapshot are always mutually consistent.
        return currencyConversionService.fromUsd(BigDecimal.valueOf(usdTarget), country.currency());
    }

    private record PendingSalary(BigDecimal amount, String currency, LocalDate effectiveDate, String reason) {
    }

    private record PendingSalaryHistory(List<PendingSalary> records) {
    }
}
