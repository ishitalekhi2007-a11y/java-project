package com.restaurant.service;

import com.restaurant.RestaurantException;
import com.restaurant.dao.MenuItemDao;
import com.restaurant.dao.OrderDao;
import com.restaurant.dao.TableDao;
import com.restaurant.db.Database;
import com.restaurant.model.MenuItem;
import com.restaurant.model.Order;
import com.restaurant.model.OrderEdit;
import com.restaurant.model.OrderItem;
import com.restaurant.model.OrderStatus;
import com.restaurant.model.Table;
import com.restaurant.model.TableStatus;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OrderService {
    private final Database db;
    private final TableDao tableDao = new TableDao();
    private final MenuItemDao menuItemDao = new MenuItemDao();
    private final OrderDao orderDao = new OrderDao();

    /** Per-order edit history used as a stack (push on edit, pop on undo). */
    private final Map<Integer, Deque<OrderEdit>> editHistory = new HashMap<>();

    public OrderService(Database db) {
        this.db = db;
    }

    /** Seats a table: the table must be free, and is marked occupied in the same transaction. */
    public Order openOrder(int tableNumber) {
        return db.inTransaction(c -> {
            Table table = tableDao.findByNumber(c, tableNumber, true)
                    .orElseThrow(() -> TableService.notFound(tableNumber));
            if (!table.isAvailable()) {
                throw new RestaurantException("Table " + tableNumber + " is already occupied");
            }
            Order order = orderDao.create(c, table.getId());
            tableDao.updateStatus(c, table.getId(), TableStatus.OCCUPIED);
            return order;
        });
    }

    public Order addItem(int orderId, int menuItemId, int quantity) {
        OrderItem[] line = new OrderItem[1];
        Order order = db.inTransaction(c -> {
            Order o = loadOpenOrder(c, orderId);
            MenuItem item = menuItemDao.findById(c, menuItemId)
                    .orElseThrow(() -> new RestaurantException("Menu item #" + menuItemId + " not found"));
            if (!item.isAvailable()) {
                throw new RestaurantException(item.getName() + " is currently unavailable");
            }
            line[0] = o.addItem(item.getId(), item.getName(), item.getPrice(), quantity);
            orderDao.saveLine(c, orderId, line[0]);
            return o;
        });
        history(orderId).push(new OrderEdit(OrderEdit.Type.ADD, menuItemId, line[0].getName(),
                line[0].getUnitPrice(), quantity));
        return order;
    }

    public Order removeItem(int orderId, int menuItemId, int quantity) {
        OrderItem[] line = new OrderItem[1];
        Order order = db.inTransaction(c -> {
            Order o = loadOpenOrder(c, orderId);
            line[0] = o.removeItem(menuItemId, quantity);
            orderDao.saveLine(c, orderId, line[0]);
            return o;
        });
        history(orderId).push(new OrderEdit(OrderEdit.Type.REMOVE, menuItemId, line[0].getName(),
                line[0].getUnitPrice(), quantity));
        return order;
    }

    /** Reverts the most recent edit on this order. Empty when there is nothing to undo. */
    public Optional<OrderEdit> undoLastEdit(int orderId) {
        Deque<OrderEdit> stack = history(orderId);
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        OrderEdit last = stack.peek();
        db.inTransaction(c -> {
            Order o = loadOpenOrder(c, orderId);
            OrderItem line = last.type() == OrderEdit.Type.ADD
                    ? o.removeItem(last.menuItemId(), last.quantity())
                    : o.addItem(last.menuItemId(), last.itemName(), last.unitPrice(), last.quantity());
            orderDao.saveLine(c, orderId, line);
            return null;
        });
        stack.pop();
        return Optional.of(last);
    }

    public List<OrderEdit> editHistory(int orderId) {
        return List.copyOf(history(orderId));
    }

    /** Cancels an open order and frees its table. */
    public void cancelOrder(int orderId) {
        db.inTransaction(c -> {
            Order o = loadOpenOrder(c, orderId);
            orderDao.updateStatus(c, orderId, OrderStatus.CANCELLED);
            tableDao.updateStatus(c, o.getTableId(), TableStatus.AVAILABLE);
            return null;
        });
        clearHistory(orderId);
    }

    public Order getOrder(int orderId) {
        return db.withConnection(c -> orderDao.findById(c, orderId, false))
                .orElseThrow(() -> new RestaurantException("Order #" + orderId + " not found"));
    }

    public List<Order> openOrders() {
        return db.withConnection(c -> orderDao.findByStatus(c, OrderStatus.OPEN));
    }

    public int tableNumberOf(Order order) {
        return db.withConnection(c -> tableDao.findById(c, order.getTableId(), false))
                .map(Table::getNumber).orElse(-1);
    }

    void clearHistory(int orderId) {
        editHistory.remove(orderId);
    }

    private Deque<OrderEdit> history(int orderId) {
        return editHistory.computeIfAbsent(orderId, id -> new ArrayDeque<>());
    }

    private Order loadOpenOrder(Connection c, int orderId) throws SQLException {
        Order order = orderDao.findById(c, orderId, true)
                .orElseThrow(() -> new RestaurantException("Order #" + orderId + " not found"));
        if (!order.isOpen()) {
            throw new RestaurantException("Order #" + orderId + " is " + order.getStatus() + " and can no longer be edited");
        }
        return order;
    }
}
