package com.acme.salary.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;

/**
 * Forces Instant columns through String (VARCHAR) rather than Hibernate's
 * default TIMESTAMP JDBC type. Discovered by actually running the app: the
 * SQLite JDBC driver's ResultSet.getTimestamp() can't parse the ISO-8601
 * string Hibernate writes for an Instant ("Error parsing time stamp"), even
 * though the column is declared TEXT. Storing as a plain ISO-8601 string and
 * reading it back via getString() sidesteps the driver's timestamp parser
 * entirely - and ISO-8601 sorts correctly as text, so ordering by these
 * columns still works.
 */
@Converter(autoApply = false)
public class InstantStringConverter implements AttributeConverter<Instant, String> {

    @Override
    public String convertToDatabaseColumn(Instant attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public Instant convertToEntityAttribute(String dbData) {
        return dbData == null ? null : Instant.parse(dbData);
    }
}
