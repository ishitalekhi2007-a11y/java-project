package com.restaurant.service;

import com.restaurant.RestaurantException;
import com.restaurant.billing.BillBreakdown;
import com.restaurant.billing.BillCalculator;
import com.restaurant.billing.Discount;
import com.restaurant.dao.BillDao;
import com.restaurant.dao.OrderDao;
import com.restaurant.dao.TableDao;
import com.restaurant.db.Database;
import com.restaurant.model.Bill;
import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import com.restaurant.model.PaymentMethod;
import com.restaurant.model.PaymentStatus;
import com.restaurant.model.TableStatus;

import java.time.LocalDateTime;
import java.util.List;

public class BillingService {
    private final Database db;
    private final BillCalculator calculator;
    private final OrderService orderService;
    private final OrderDao orderDao = new OrderDao();
    private final BillDao billDao = new BillDao();
    private final TableDao tableDao = new TableDao();

    public BillingService(Database db, BillCalculator calculator, OrderService orderService) {
        this.db = db;
        this.calculator = calculator;
        this.orderService = orderService;
    }

    public BillBreakdown preview(int orderId, Discount discount) {
        Order order = orderService.getOrder(orderId);
        return calculator.calculate(order, discount);
    }

    /**
     * Locks the order, computes the bill, stores it and closes the order in one transaction.
     * Any failure rolls everything back, so an order is never half-billed or billed twice.
     */
    public Bill generateBill(int orderId, Discount discount) {
        Bill bill = db.inTransaction(c -> {
            Order order = orderDao.findById(c, orderId, true)
                    .orElseThrow(() -> new RestaurantException("Order #" + orderId + " not found"));
            if (!order.isOpen()) {
                throw new RestaurantException("Order #" + orderId + " is already " + order.getStatus());
            }
            if (order.getItems().isEmpty()) {
                throw new RestaurantException("Order #" + orderId + " has no items to bill");
            }
            BillBreakdown b = calculator.calculate(order, discount);
            Bill created = billDao.create(c, new Bill(0, orderId, b.subtotal(), b.discount(), b.discountDescription(),
                    b.taxTotal(), b.taxDescription(), b.total(), PaymentStatus.UNPAID, null, LocalDateTime.now(), null));
            orderDao.updateStatus(c, orderId, OrderStatus.BILLED);
            return created;
        });
        orderService.clearHistory(orderId);
        return bill;
    }

    /** Records payment and frees the table in one transaction. */
    public Bill payBill(int billId, PaymentMethod method) {
        return db.inTransaction(c -> {
            Bill bill = billDao.findById(c, billId, true)
                    .orElseThrow(() -> new RestaurantException("Bill #" + billId + " not found"));
            if (bill.isPaid()) {
                throw new RestaurantException("Bill #" + billId + " is already paid");
            }
            Order order = orderDao.findById(c, bill.getOrderId(), true)
                    .orElseThrow(() -> new RestaurantException("Order #" + bill.getOrderId() + " not found"));
            billDao.markPaid(c, billId, method, LocalDateTime.now());
            tableDao.updateStatus(c, order.getTableId(), TableStatus.AVAILABLE);
            return billDao.findById(c, billId, false).orElseThrow();
        });
    }

    public Bill getBill(int billId) {
        return db.withConnection(c -> billDao.findById(c, billId, false))
                .orElseThrow(() -> new RestaurantException("Bill #" + billId + " not found"));
    }

    public List<Bill> listBills() {
        return db.withConnection(billDao::findAll);
    }
}
