package com.restaurant;

import com.restaurant.billing.BillCalculator;
import com.restaurant.billing.GstTax;
import com.restaurant.billing.NoDiscount;
import com.restaurant.billing.PercentageDiscount;
import com.restaurant.db.DataAccessException;
import com.restaurant.db.Database;
import com.restaurant.model.Bill;
import com.restaurant.model.Category;
import com.restaurant.model.MenuItem;
import com.restaurant.model.Order;
import com.restaurant.model.OrderEdit;
import com.restaurant.model.OrderStatus;
import com.restaurant.model.PaymentMethod;
import com.restaurant.model.PaymentStatus;
import com.restaurant.model.Table;
import com.restaurant.model.TableStatus;
import com.restaurant.service.BillingService;
import com.restaurant.service.MenuService;
import com.restaurant.service.OrderService;
import com.restaurant.service.TableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceIntegrationTest {
    private Database db;
    private TableService tables;
    private MenuService menu;
    private OrderService orders;
    private BillingService billing;
    private MenuItem tikka;
    private MenuItem naan;

    @BeforeEach
    void setUp() {
        db = new Database("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        db.initSchema();
        tables = new TableService(db);
        menu = new MenuService(db);
        orders = new OrderService(db);
        billing = new BillingService(db, new BillCalculator(List.of(new GstTax(new BigDecimal("5")))), orders);
        tables.addTable(1, 4);
        tikka = menu.addItem("Paneer Tikka", Category.STARTER, new BigDecimal("240"));
        naan = menu.addItem("Butter Naan", Category.MAIN_COURSE, new BigDecimal("50"));
    }

    private TableStatus statusOfTable1() {
        return tables.listTables().get(0).getStatus();
    }

    @Test
    void openingAnOrderOccupiesTheTable() {
        orders.openOrder(1);
        assertEquals(TableStatus.OCCUPIED, statusOfTable1());
        assertThrows(RestaurantException.class, () -> orders.openOrder(1));
    }

    @Test
    void undoRevertsEditsInReverseOrder() {
        int id = orders.openOrder(1).getId();
        orders.addItem(id, tikka.getId(), 2);
        orders.addItem(id, naan.getId(), 4);
        orders.removeItem(id, tikka.getId(), 1);
        assertEquals(new BigDecimal("440.00"), orders.getOrder(id).subtotal());

        assertEquals(OrderEdit.Type.REMOVE, orders.undoLastEdit(id).orElseThrow().type());
        assertEquals(new BigDecimal("680.00"), orders.getOrder(id).subtotal());

        assertEquals(OrderEdit.Type.ADD, orders.undoLastEdit(id).orElseThrow().type());
        Order order = orders.getOrder(id);
        assertEquals(1, order.getItems().size());
        assertEquals(2, order.findLine(tikka.getId()).orElseThrow().getQuantity());

        orders.undoLastEdit(id);
        assertTrue(orders.getOrder(id).getItems().isEmpty());
        assertTrue(orders.undoLastEdit(id).isEmpty());
    }

    @Test
    void lineKeepsPriceCapturedAtOrderTime() {
        int id = orders.openOrder(1).getId();
        orders.addItem(id, tikka.getId(), 1);
        menu.updateItem(tikka.getId(), "Paneer Tikka", new BigDecimal("300"), true);
        assertEquals(new BigDecimal("240.00"), orders.getOrder(id).subtotal());
    }

    @Test
    void unavailableItemCannotBeOrdered() {
        menu.updateItem(naan.getId(), "Butter Naan", new BigDecimal("50"), false);
        int id = orders.openOrder(1).getId();
        assertThrows(RestaurantException.class, () -> orders.addItem(id, naan.getId(), 1));
    }

    @Test
    void billingClosesOrderAndPaymentFreesTable() {
        int id = orders.openOrder(1).getId();
        orders.addItem(id, tikka.getId(), 2);

        Bill bill = billing.generateBill(id, new PercentageDiscount(new BigDecimal("10")));
        assertEquals(new BigDecimal("453.60"), bill.getTotal()); // 480 - 48 = 432, + 5% GST 21.60
        assertEquals(OrderStatus.BILLED, orders.getOrder(id).getStatus());
        assertEquals(TableStatus.OCCUPIED, statusOfTable1());
        assertThrows(RestaurantException.class, () -> orders.addItem(id, naan.getId(), 1));
        assertThrows(RestaurantException.class, () -> billing.generateBill(id, new NoDiscount()));

        Bill paid = billing.payBill(bill.getId(), PaymentMethod.UPI);
        assertEquals(PaymentStatus.PAID, paid.getPaymentStatus());
        assertEquals(TableStatus.AVAILABLE, statusOfTable1());
        assertThrows(RestaurantException.class, () -> billing.payBill(bill.getId(), PaymentMethod.CASH));
    }

    @Test
    void failedBillingRollsBackTheWholeTransaction() {
        int id = orders.openOrder(1).getId();
        orders.addItem(id, tikka.getId(), 1);
        // A stray bill row for this order makes the INSERT in generateBill violate the UNIQUE constraint.
        db.withConnection(c -> {
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO bills (order_id, subtotal, discount, tax, "
                    + "total, payment_status, created_at) VALUES (?, 0, 0, 0, 0, 'UNPAID', ?)")) {
                ps.setInt(1, id);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                return ps.executeUpdate();
            }
        });

        assertThrows(DataAccessException.class, () -> billing.generateBill(id, new NoDiscount()));
        assertEquals(OrderStatus.OPEN, orders.getOrder(id).getStatus());
        assertEquals(1, billing.listBills().size());
    }

    @Test
    void cancelFreesTableAndOccupiedTableCannotBeDeleted() {
        int id = orders.openOrder(1).getId();
        assertThrows(RestaurantException.class, () -> tables.deleteTable(1));
        orders.cancelOrder(id);
        assertEquals(TableStatus.AVAILABLE, statusOfTable1());
        assertEquals(OrderStatus.CANCELLED, orders.getOrder(id).getStatus());
    }

    @Test
    void tableCrud() {
        Table t = tables.addTable(9, 2);
        assertEquals(6, tables.updateCapacity(9, 6).getCapacity());
        assertThrows(RestaurantException.class, () -> tables.addTable(9, 2));
        tables.deleteTable(9);
        assertEquals(1, tables.listTables().size());
        assertTrue(t.getId() > 0);
    }
}
