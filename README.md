# Restaurant Billing and Table Management System

Console application for managing restaurant tables, the menu, customer orders and bills.
Java 17, JDBC, embedded H2 database (no server to install).

## Run

Requires Java 17+. Maven itself is not needed: the bundled wrapper downloads it.

```bash
./mvnw package                               # macOS/Linux: compiles, runs 12 tests, builds the jar
java -jar target/restaurant-billing.jar      # data is stored in ./data/restaurant.mv.db
```

**Windows:** see [RUN-ON-WINDOWS.md](RUN-ON-WINDOWS.md) for a step-by-step guide (`.\mvnw.cmd package`).

The first run seeds 6 tables and 10 menu items. Delete `data/` to start fresh.

## Typical flow

1. **Orders → Open order for table**: the table becomes OCCUPIED.
2. **Orders → Add / Remove item**: edits are saved immediately. **Undo last edit** reverts them one at a time.
3. **Billing → Generate bill**: choose a discount, review the breakdown and confirm. The order is closed for edits.
4. **Billing → Record payment**: choose Cash, Card or UPI. The bill becomes PAID and the table becomes AVAILABLE.

## Where each requirement lives

| Requirement | Implementation |
|---|---|
| Table, MenuItem, Order, Bill classes | `model/Table`, `model/MenuItem`, `model/Order` (+ `OrderItem`), `model/Bill` |
| Inheritance & polymorphism for discounts | abstract `billing/Discount` → `NoDiscount`, `PercentageDiscount`, `FlatDiscount` |
| Inheritance & polymorphism for taxes | abstract `billing/Tax` → `GstTax`, `ServiceCharge` (overrides `amountOn` to waive small bills) |
| Bill calculation | `billing/BillCalculator`: subtotal → discount → taxes on the discounted amount → total |
| Collections | `List<OrderItem>` per order, `Map<Category, List<MenuItem>>` grouped menu, `Map<Integer, Deque<OrderEdit>>` edit history |
| Stack for order edits | `service/OrderService`: each add/remove is pushed onto a per-order stack. Undo pops it and applies the reverse |
| JDBC CRUD | `dao/TableDao`, `MenuItemDao`, `OrderDao`, `BillDao` (PreparedStatements) |
| Transaction-safe billing | `db/Database.inTransaction` commits or rolls back. `BillingService.generateBill` locks the order row (`SELECT ... FOR UPDATE`), inserts the bill and closes the order atomically. `payBill` marks the bill paid and frees the table atomically. `bills.order_id` is UNIQUE, so an order can never be billed twice |
| Table availability | Opening an order occupies the table. Payment or cancellation frees it, in the same transaction |

Money uses `BigDecimal` rounded to 2 decimals (HALF_UP). The tax setup is in `Main`: GST 5%, plus a 10% service charge that is waived below 500.

## Tests

`mvn test` runs:
- `BillCalculatorTest`: discount and tax math, the service-charge waiver, the flat-discount minimum and cap.
- `ServiceIntegrationTest` (in-memory H2): table occupancy, undo order, price snapshot, billing and payment state changes, and a forced constraint failure that proves the whole billing transaction rolls back.
