package com.bfs.qualifier.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class SqlSolverService {

    public String resolveSqlByRegNo(String regNo) {
        // Get last two digits from regNo (ignore non-digits)
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.length() < 2) {
            throw new IllegalArgumentException("regNo must contain at least two digits: " + regNo);
        }
        int lastTwo = Integer.parseInt(digits.substring(digits.length() - 2));
        boolean isOdd = (lastTwo % 2) == 1;

        String path = isOdd ? "sql/q1.sql" : "sql/q2.sql";
        try {
            var res = new ClassPathResource(path);
            byte[] data = res.getContentAsByteArray();
            String sql = new String(data, StandardCharsets.UTF_8).trim();
            if (sql.isBlank()) {
                throw new IllegalStateException("SQL file " + path + " is empty.");
            }
            return sql;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + path + ": " + e.getMessage(), e);
        }
    }
}
