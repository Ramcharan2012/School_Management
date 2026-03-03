package com.school.management.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Utility for generating human-readable, unique IDs.
 * Format examples:
 * APP-2024-00001 (Admission Application)
 * STU-2024-00001 (Student roll number)
 * TCH-2024-00001 (Teacher employee ID)
 * RCPT-2024-00001 (Fee receipt)
 *
 * Note: In production use a DB sequence or UUID for true uniqueness across
 * restarts.
 * This is sufficient for single-instance deployment.
 */
public final class IdGeneratorUtil {

    private static final AtomicLong counter = new AtomicLong(System.currentTimeMillis() % 100000);
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");

    private IdGeneratorUtil() {
    }

    private static String generate(String prefix) {
        String year = LocalDate.now().format(YEAR_FORMAT);
        long seq = counter.incrementAndGet();
        return String.format("%s-%s-%05d", prefix, year, seq % 100000);
    }

    public static String generateApplicationNumber() {
        return generate("APP");
    }

    public static String generateStudentRollNumber() {
        return generate("STU");
    }

    public static String generateTeacherEmployeeId() {
        return generate("TCH");
    }

    public static String generateReceiptNumber() {
        return generate("RCPT");
    }
}
