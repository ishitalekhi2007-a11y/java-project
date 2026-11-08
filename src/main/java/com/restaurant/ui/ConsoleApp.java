package com.restaurant.ui;

import com.restaurant.RestaurantException;
import com.restaurant.billing.BillBreakdown;
import com.restaurant.billing.Discount;
import com.restaurant.billing.FlatDiscount;
import com.restaurant.billing.NoDiscount;
import com.restaurant.billing.PercentageDiscount;
import com.restaurant.db.DataAccessException;
import com.restaurant.model.Bill;
import com.restaurant.model.Category;
import com.restaurant.model.MenuItem;
import com.restaurant.model.Order;
import com.restaurant.model.OrderEdit;
import com.restaurant.model.OrderItem;
import com.restaurant.model.PaymentMethod;
import com.restaurant.model.Table;
import com.restaurant.service.BillingService;
import com.restaurant.service.MenuService;
import com.restaurant.service.OrderService;
import com.restaurant.service.TableService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {
    private final TableService tables;
    private final MenuService menu;
    private final OrderService orders;
    private final BillingService billing;
    private final Input in;

    public ConsoleApp(TableService tables, MenuService menu, OrderService orders, BillingService billing,
                      Scanner scanner) {
        this.tables = tables;
        this.menu = menu;
        this.orders = orders;
        this.billing = billing;
        this.in = new Input(scanner);
    }

    public void run() {
        System.out.println("=== Restaurant Billing & Table Management ===");
        try {
            while (true) {
                System.out.println();
                System.out.println("1. Tables   2. Menu   3. Orders   4. Billing   0. Exit");
                int choice = in.integer("> ");
                if (choice == 0) {
                    break;
                }
                switch (choice) {
                    case 1 -> tablesMenu();
                    case 2 -> menuMenu();
                    case 3 -> ordersMenu();
                    case 4 -> billingMenu();
                    default -> System.out.println("Unknown option.");
                }
            }
        } catch (Input.EndOfInput e) {
            // stdin closed
        }
        System.out.println("Goodbye.");
    }

    // ---------------------------------------------------------------- tables

    private void tablesMenu() {
        System.out.println("\n-- Tables --  1. List  2. Add  3. Change capacity  4. Delete  0. Back");
        switch (in.integer("> ")) {
            case 1 -> listTables();
            case 2 -> attempt(() -> {
                Table t = tables.addTable(in.integer("Table number: "), in.integer("Seats: "));
                System.out.println("Added " + t);
            });
            case 3 -> attempt(() -> {
                Table t = tables.updateCapacity(in.integer("Table number: "), in.integer("New seats: "));
                System.out.println("Updated " + t);
            });
            case 4 -> attempt(() -> {
                int number = in.integer("Table number: ");
                tables.deleteTable(number);
                System.out.println("Deleted table " + number);
            });
            default -> { }
        }
    }

    private void listTables() {
        List<Table> all = tables.listTables();
        long free = all.stream().filter(Table::isAvailable).count();
        all.forEach(t -> System.out.println("  " + t));
        System.out.printf("  %d of %d tables available%n", free, all.size());
    }

    // ------------------------------------------------------------------ menu

    private void menuMenu() {
        System.out.println("\n-- Menu --  1. Show  2. Add item  3. Edit item  4. Delete item  0. Back");
        switch (in.integer("> ")) {
            case 1 -> showMenu();
            case 2 -> attempt(() -> {
                String name = in.text("Name: ");
                Category category = in.choice("Category: ", Category.values());
                MenuItem item = menu.addItem(name, category, in.amount("Price: "));
                System.out.println("Added " + item);
            });
            case 3 -> attempt(() -> {
                MenuItem item = menu.getItem(in.integer("Item id: "));
                System.out.println("Editing " + item);
                String name = in.textOr("Name [" + item.getName() + "]: ", item.getName());
                BigDecimal price = in.amountOr("Price [" + item.getPrice() + "]: ", item.getPrice());
                boolean available = in.yesNo("Available?");
                System.out.println("Updated " + menu.updateItem(item.getId(), name, price, available));
            });
            case 4 -> attempt(() -> {
                int id = in.integer("Item id: ");
                menu.deleteItem(id);
                System.out.println("Deleted item #" + id);
            });
            default -> { }
        }
    }

    private void showMenu() {
        for (Map.Entry<Category, List<MenuItem>> group : menu.menuByCategory().entrySet()) {
            System.out.println("  [" + group.getKey() + "]");
            group.getValue().forEach(i -> System.out.println("    " + i));
        }
    }

    // ---------------------------------------------------------------- orders

    private void ordersMenu() {
        System.out.println("\n-- Orders --  1. Open order for table  2. List open orders  3. View order");
        System.out.println("              4. Add item  5. Remove item  6. Undo last edit  7. Cancel order  0. Back");
        switch (in.integer("> ")) {
            case 1 -> attempt(() -> {
                Order o = orders.openOrder(in.integer("Table number: "));
                System.out.println("Opened order #" + o.getId() + " for table " + orders.tableNumberOf(o));
            });
            case 2 -> {
                List<Order> open = orders.openOrders();
                if (open.isEmpty()) {
                    System.out.println("  No open orders.");
                }
                open.forEach(o -> System.out.printf("  Order #%-3d | table %-3d | %d line(s) | subtotal %s%n",
                        o.getId(), orders.tableNumberOf(o), o.getItems().size(), o.subtotal()));
            }
            case 3 -> attempt(() -> printOrder(orders.getOrder(in.integer("Order id: "))));
            case 4 -> attempt(() -> {
                int orderId = in.integer("Order id: ");
                showMenu();
                printOrder(orders.addItem(orderId, in.integer("Item id: "), in.integer("Quantity: ")));
            });
            case 5 -> attempt(() -> {
                Order o = orders.getOrder(in.integer("Order id: "));
                printOrder(o);
                printOrder(orders.removeItem(o.getId(), in.integer("Item id to remove: "), in.integer("Quantity: ")));
            });
            case 6 -> attempt(() -> {
                int orderId = in.integer("Order id: ");
                Optional<OrderEdit> undone = orders.undoLastEdit(orderId);
                System.out.println(undone.map(e -> "Undid: " + e).orElse("Nothing to undo for this order."));
                undone.ifPresent(e -> printOrder(orders.getOrder(orderId)));
            });
            case 7 -> attempt(() -> {
                int orderId = in.integer("Order id: ");
                if (in.yesNo("Cancel order #" + orderId + " and free its table?")) {
                    orders.cancelOrder(orderId);
                    System.out.println("Order #" + orderId + " cancelled.");
                }
            });
            default -> { }
        }
    }

    private void printOrder(Order o) {
        System.out.printf("  Order #%d | table %d | %s%n", o.getId(), orders.tableNumberOf(o), o.getStatus());
        if (o.getItems().isEmpty()) {
            System.out.println("    (no items)");
        }
        for (OrderItem i : o.getItems()) {
            System.out.printf("    #%-3d %-22s %3d x %8s = %9s%n",
                    i.getMenuItemId(), i.getName(), i.getQuantity(), i.getUnitPrice(), i.lineTotal());
        }
        System.out.printf("    %47s %9s%n", "Subtotal", o.subtotal());
        List<OrderEdit> history = orders.editHistory(o.getId());
        if (!history.isEmpty()) {
            System.out.println("    Last edit (undoable): " + history.get(0));
        }
    }

    // --------------------------------------------------------------- billing

    private void billingMenu() {
        System.out.println("\n-- Billing --  1. Preview bill  2. Generate bill  3. Record payment  4. List bills  0. Back");
        switch (in.integer("> ")) {
            case 1 -> attempt(() -> {
                int orderId = in.integer("Order id: ");
                printBreakdown(billing.preview(orderId, askDiscount()));
            });
            case 2 -> attempt(() -> {
                int orderId = in.integer("Order id: ");
                Discount discount = askDiscount();
                printBreakdown(billing.preview(orderId, discount));
                if (in.yesNo("Generate this bill? The order will be closed for edits")) {
                    Bill bill = billing.generateBill(orderId, discount);
                    System.out.println("Generated " + bill);
                }
            });
            case 3 -> attempt(() -> {
                Bill bill = billing.getBill(in.integer("Bill id: "));
                System.out.println("  " + bill);
                PaymentMethod method = in.choice("Payment method: ", PaymentMethod.values());
                System.out.println("Paid. " + billing.payBill(bill.getId(), method) + " - table is now free.");
            });
            case 4 -> {
                List<Bill> bills = billing.listBills();
                if (bills.isEmpty()) {
                    System.out.println("  No bills yet.");
                }
                bills.forEach(b -> System.out.println("  " + b));
            }
            default -> { }
        }
    }

    private Discount askDiscount() {
        System.out.println("Discount: 1. None  2. Percentage  3. Flat amount");
        return switch (in.integer("> ")) {
            case 2 -> new PercentageDiscount(in.amount("Percent off: "));
            case 3 -> new FlatDiscount(in.amount("Amount off: "), in.amount("Minimum subtotal: "));
            default -> new NoDiscount();
        };
    }

    private void printBreakdown(BillBreakdown b) {
        System.out.println("  ----------------------------------------");
        System.out.printf("  %-28s %10s%n", "Subtotal", b.subtotal());
        System.out.printf("  %-28s %10s%n", "Discount (" + b.discountDescription() + ")", "-" + b.discount());
        b.taxes().forEach((label, amount) -> System.out.printf("  %-28s %10s%n", label, amount));
        System.out.println("  ----------------------------------------");
        System.out.printf("  %-28s %10s%n", "TOTAL", b.total());
    }

    /** Runs one action, reporting rule violations and database errors without leaving the menu. */
    private void attempt(Runnable action) {
        try {
            action.run();
        } catch (RestaurantException e) {
            System.out.println("  ! " + e.getMessage());
        } catch (DataAccessException e) {
            System.out.println("  ! " + e.getMessage());
        }
    }
}
