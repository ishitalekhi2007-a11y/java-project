package com.restaurant.service;

import com.restaurant.RestaurantException;
import com.restaurant.dao.TableDao;
import com.restaurant.db.Database;
import com.restaurant.model.Table;

import java.util.List;

public class TableService {
    private final Database db;
    private final TableDao tableDao = new TableDao();

    public TableService(Database db) {
        this.db = db;
    }

    public Table addTable(int number, int capacity) {
        requirePositive(number, "Table number");
        requirePositive(capacity, "Capacity");
        return db.inTransaction(c -> {
            if (tableDao.findByNumber(c, number, false).isPresent()) {
                throw new RestaurantException("Table " + number + " already exists");
            }
            return tableDao.create(c, new Table(number, capacity));
        });
    }

    public List<Table> listTables() {
        return db.withConnection(tableDao::findAll);
    }

    public Table updateCapacity(int number, int capacity) {
        requirePositive(capacity, "Capacity");
        return db.inTransaction(c -> {
            Table table = tableDao.findByNumber(c, number, true).orElseThrow(() -> notFound(number));
            table.setCapacity(capacity);
            tableDao.update(c, table);
            return table;
        });
    }

    public void deleteTable(int number) {
        db.inTransaction(c -> {
            Table table = tableDao.findByNumber(c, number, true).orElseThrow(() -> notFound(number));
            if (!table.isAvailable()) {
                throw new RestaurantException("Table " + number + " is occupied and cannot be deleted");
            }
            if (hasOrderHistory(c, table.getId())) {
                throw new RestaurantException("Table " + number + " has order history and cannot be deleted");
            }
            return tableDao.delete(c, table.getId());
        });
    }

    private boolean hasOrderHistory(java.sql.Connection c, int tableId) throws java.sql.SQLException {
        try (var ps = c.prepareStatement("SELECT 1 FROM orders WHERE table_id = ? LIMIT 1")) {
            ps.setInt(1, tableId);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static RestaurantException notFound(int number) {
        return new RestaurantException("Table " + number + " not found");
    }

    private static void requirePositive(int value, String field) {
        if (value <= 0) {
            throw new RestaurantException(field + " must be greater than zero");
        }
    }
}
