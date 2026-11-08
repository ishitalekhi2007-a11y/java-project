package com.restaurant.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

final class Dao {
    private Dao() {}

    static int generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("No generated key returned");
            }
            return keys.getInt(1);
        }
    }

    /** Row lock for read-modify-write inside a transaction. */
    static String lock(boolean forUpdate) {
        return forUpdate ? " FOR UPDATE" : "";
    }

    static LocalDateTime toDateTime(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
