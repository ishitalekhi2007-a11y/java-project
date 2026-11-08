package com.restaurant.model;

public class Table {
    private int id;
    private int number;
    private int capacity;
    private TableStatus status;

    public Table(int id, int number, int capacity, TableStatus status) {
        this.id = id;
        this.number = number;
        this.capacity = capacity;
        this.status = status;
    }

    public Table(int number, int capacity) {
        this(0, number, capacity, TableStatus.AVAILABLE);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getNumber() { return number; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public TableStatus getStatus() { return status; }
    public void setStatus(TableStatus status) { this.status = status; }
    public boolean isAvailable() { return status == TableStatus.AVAILABLE; }

    @Override
    public String toString() {
        return String.format("Table %-3d | seats %-2d | %s", number, capacity, status);
    }
}
