package com.acme.salary.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;

/**
 * Same rationale as InstantStringConverter, found the same way: EmployeeBulkWriter
 * (raw JDBC, used by CSV import and the seed script) writes LocalDate columns as
 * plain ISO-8601 text via setString(). Hibernate's default LocalDate mapping goes
 * through java.sql.Date (setDate()/getDate()), and the SQLite driver's getDate()
 * expects a different string format than plain "yyyy-MM-dd" - self-consistent
 * for pure-Hibernate round trips, but incompatible with data written by raw JDBC.
 * Forcing both paths through the same plain-string format resolves it.
 */
@Converter(autoApply = false)
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LocalDate.parse(dbData);
    }
}
