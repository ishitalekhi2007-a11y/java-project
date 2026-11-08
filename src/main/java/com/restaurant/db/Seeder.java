package com.restaurant.db;

import com.restaurant.model.Category;
import com.restaurant.service.MenuService;
import com.restaurant.service.TableService;

import java.math.BigDecimal;

/** Loads sample tables and menu items on first run. */
public final class Seeder {
    private Seeder() {}

    public static void seedIfEmpty(TableService tables, MenuService menu) {
        if (tables.listTables().isEmpty()) {
            int[][] layout = {{1, 2}, {2, 2}, {3, 4}, {4, 4}, {5, 6}, {6, 8}};
            for (int[] t : layout) {
                tables.addTable(t[0], t[1]);
            }
        }
        if (menu.listItems().isEmpty()) {
            menu.addItem("Paneer Tikka", Category.STARTER, new BigDecimal("240"));
            menu.addItem("Veg Spring Rolls", Category.STARTER, new BigDecimal("180"));
            menu.addItem("Butter Chicken", Category.MAIN_COURSE, new BigDecimal("360"));
            menu.addItem("Dal Makhani", Category.MAIN_COURSE, new BigDecimal("260"));
            menu.addItem("Veg Biryani", Category.MAIN_COURSE, new BigDecimal("280"));
            menu.addItem("Butter Naan", Category.MAIN_COURSE, new BigDecimal("50"));
            menu.addItem("Gulab Jamun", Category.DESSERT, new BigDecimal("120"));
            menu.addItem("Brownie Sundae", Category.DESSERT, new BigDecimal("190"));
            menu.addItem("Masala Chai", Category.BEVERAGE, new BigDecimal("60"));
            menu.addItem("Fresh Lime Soda", Category.BEVERAGE, new BigDecimal("90"));
        }
    }
}
