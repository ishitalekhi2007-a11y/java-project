package com.restaurant;

import com.restaurant.billing.BillCalculator;
import com.restaurant.billing.GstTax;
import com.restaurant.billing.ServiceCharge;
import com.restaurant.db.Database;
import com.restaurant.db.Seeder;
import com.restaurant.service.BillingService;
import com.restaurant.service.MenuService;
import com.restaurant.service.OrderService;
import com.restaurant.service.TableService;
import com.restaurant.ui.ConsoleApp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : "jdbc:h2:./data/restaurant";
        Database db = new Database(url);
        db.initSchema();

        TableService tables = new TableService(db);
        MenuService menu = new MenuService(db);
        OrderService orders = new OrderService(db);
        BillCalculator calculator = new BillCalculator(List.of(
                new GstTax(new BigDecimal("5")),
                new ServiceCharge(new BigDecimal("10"), new BigDecimal("500"))));
        BillingService billing = new BillingService(db, calculator, orders);

        Seeder.seedIfEmpty(tables, menu);
        new ConsoleApp(tables, menu, orders, billing, new Scanner(System.in)).run();
    }
}
