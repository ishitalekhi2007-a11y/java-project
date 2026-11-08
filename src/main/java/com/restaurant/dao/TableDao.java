package com.restaurant.dao;

import com.restaurant.model.Table;
import com.restaurant.model.TableStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TableDao {

    public Table create(Connection c, Table table) throws SQLException {
        String sql = "INSERT INTO restaurant_tables (table_number, capacity, status) VALUES (?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, table.getNumber());
            ps.setInt(2, table.getCapacity());
            ps.setString(3, table.getStatus().name());
            ps.executeUpdate();
            table.setId(Dao.generatedId(ps));
            return table;
        }
    }

    public List<Table> findAll(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM restaurant_tables ORDER BY table_number");
             ResultSet rs = ps.executeQuery()) {
            List<Table> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(map(rs));
            }
            return tables;
        }
    }

    public Optional<Table> findById(Connection c, int id, boolean forUpdate) throws SQLException {
        return findOne(c, "SELECT * FROM restaurant_tables WHERE id = ?" + Dao.lock(forUpdate), id);
    }

    public Optional<Table> findByNumber(Connection c, int number, boolean forUpdate) throws SQLException {
        return findOne(c, "SELECT * FROM restaurant_tables WHERE table_number = ?" + Dao.lock(forUpdate), number);
    }

    public void update(Connection c, Table table) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE restaurant_tables SET capacity = ?, status = ? WHERE id = ?")) {
            ps.setInt(1, table.getCapacity());
            ps.setString(2, table.getStatus().name());
            ps.setInt(3, table.getId());
            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection c, int id, TableStatus status) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE restaurant_tables SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean delete(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM restaurant_tables WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }

    private Optional<Table> findOne(Connection c, String sql, int param) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    private Table map(ResultSet rs) throws SQLException {
        return new Table(rs.getInt("id"), rs.getInt("table_number"), rs.getInt("capacity"),
                TableStatus.valueOf(rs.getString("status")));
    }
}
