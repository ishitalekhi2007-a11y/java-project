package com.restaurant.service;

import com.restaurant.RestaurantException;
import com.restaurant.dao.MenuItemDao;
import com.restaurant.db.Database;
import com.restaurant.model.Category;
import com.restaurant.model.MenuItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class MenuService {
    private final Database db;
    private final MenuItemDao menuItemDao = new MenuItemDao();

    public MenuService(Database db) {
        this.db = db;
    }

    public MenuItem addItem(String name, Category category, BigDecimal price) {
        validate(name, price);
        return db.withConnection(c -> menuItemDao.create(c, new MenuItem(name.trim(), category, price)));
    }

    public List<MenuItem> listItems() {
        return db.withConnection(menuItemDao::findAll);
    }

    /** The menu grouped by category, in category order. */
    public Map<Category, List<MenuItem>> menuByCategory() {
        return listItems().stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory, TreeMap::new, Collectors.toList()));
    }

    public MenuItem getItem(int id) {
        return db.withConnection(c -> find(c, id));
    }

    public MenuItem updateItem(int id, String name, BigDecimal price, boolean available) {
        validate(name, price);
        return db.inTransaction(c -> {
            MenuItem item = find(c, id);
            item.setName(name.trim());
            item.setPrice(price);
            item.setAvailable(available);
            menuItemDao.update(c, item);
            return item;
        });
    }

    /** Items that were ever ordered are kept for bill history; mark them unavailable instead. */
    public void deleteItem(int id) {
        db.inTransaction(c -> {
            find(c, id);
            try (var ps = c.prepareStatement("SELECT 1 FROM order_items WHERE menu_item_id = ? LIMIT 1")) {
                ps.setInt(1, id);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        throw new RestaurantException("Item #" + id + " appears on orders; mark it unavailable instead");
                    }
                }
            }
            return menuItemDao.delete(c, id);
        });
    }

    private MenuItem find(Connection c, int id) throws SQLException {
        return menuItemDao.findById(c, id).orElseThrow(() -> new RestaurantException("Menu item #" + id + " not found"));
    }

    private static void validate(String name, BigDecimal price) {
        if (name == null || name.isBlank()) {
            throw new RestaurantException("Item name is required");
        }
        if (price == null || price.signum() < 0) {
            throw new RestaurantException("Price must not be negative");
        }
    }
}
