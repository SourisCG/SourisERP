package com.portfolio.erp.infrastructure.in.demo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.application.config.DemoProperties;
import com.portfolio.erp.domain.model.Category;
import com.portfolio.erp.domain.model.Customer;
import com.portfolio.erp.domain.model.InventoryItem;
import com.portfolio.erp.domain.model.Invoice;
import com.portfolio.erp.domain.model.InvoiceLine;
import com.portfolio.erp.domain.model.InvoicePayment;
import com.portfolio.erp.domain.model.InvoiceStatus;
import com.portfolio.erp.domain.model.MovementType;
import com.portfolio.erp.domain.model.Product;
import com.portfolio.erp.domain.model.PurchaseOrder;
import com.portfolio.erp.domain.model.PurchaseOrderLine;
import com.portfolio.erp.domain.model.PurchaseOrderStatus;
import com.portfolio.erp.domain.model.SalesOrder;
import com.portfolio.erp.domain.model.SalesOrderLine;
import com.portfolio.erp.domain.model.SalesOrderStatus;
import com.portfolio.erp.domain.model.StockMovement;
import com.portfolio.erp.domain.model.Supplier;
import com.portfolio.erp.domain.model.UnitOfMeasure;
import com.portfolio.erp.domain.model.Warehouse;
import com.portfolio.erp.domain.ports.out.CategoryRepositoryPort;
import com.portfolio.erp.domain.ports.out.CustomerRepositoryPort;
import com.portfolio.erp.domain.ports.out.DemoDataPort;
import com.portfolio.erp.domain.ports.out.InventoryRepositoryPort;
import com.portfolio.erp.domain.ports.out.InvoiceRepositoryPort;
import com.portfolio.erp.domain.ports.out.ProductRepositoryPort;
import com.portfolio.erp.domain.ports.out.PurchaseOrderRepositoryPort;
import com.portfolio.erp.domain.ports.out.SalesOrderRepositoryPort;
import com.portfolio.erp.domain.ports.out.StockMovementRepositoryPort;
import com.portfolio.erp.domain.ports.out.SupplierRepositoryPort;
import com.portfolio.erp.domain.ports.out.WarehouseRepositoryPort;

/**
 * Builds a rich, deterministic 12-month business narrative so every screen and
 * report looks alive from the first second. Executed on startup (demo mode)
 * and on every demo reset.
 */
@Component
public class DemoDataSeeder implements DemoDataPort {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private static final String[] CATEGORY_NAMES = {
            "Laptops", "Monitors", "Peripherals", "Networking", "Storage",
            "Office Furniture", "Paper & Stationery", "Ink & Toner", "Phones", "Accessories"
    };
    private static final String[] ADJECTIVES = {
            "Pro", "Ultra", "Essential", "Business", "Compact", "Advanced", "Classic", "Prime", "Eco", "Max"
    };
    private static final String[] NOUNS = {
            "Laptop", "Monitor 27\"", "Keyboard", "Mouse", "Switch 24p", "SSD 1TB", "Desk", "Chair",
            "Paper A4", "Toner Black", "Phone X", "Headset", "Dock USB-C", "Webcam HD", "Router WiFi 6",
            "Cable HDMI", "Notebook", "Pen Pack", "Label Printer", "UPS 900VA"
    };
    private static final String[] CUSTOMER_NAMES = {
            "ACME Corporation", "Globex Industries", "Initech Solutions", "Umbrella Logistics", "Stark Manufacturing",
            "Wayne Consulting", "Wonka Foods", "Soylent Retail", "Cyberdyne Systems", "Tyrell Analytics",
            "Aperture Labs", "Black Mesa Research", "Oscorp Chemicals", "Lexcorp Media", "Massive Dynamic",
            "Hooli Cloud", "Pied Piper Data", "Vandelay Imports", "Dunder Mifflin Paper", "Sterling Cooper Ads"
    };
    private static final String[] SUPPLIER_NAMES = {
            "Global Tech Distribution", "Nordic Components", "Iberia Office Supply", "FastChip Wholesale",
            "Prime Paper Co", "PowerNet Systems", "Atlas Furniture", "BrightInk Supplies",
            "Alpha Peripherals", "DataStore Logistics", "Vertex Networks", "Sunrise Electronics"
    };
    private static final String[] WAREHOUSE_DATA = {"WH-MAD", "Madrid Central", "WH-BCN", "Barcelona Port", "WH-VAL", "Valencia Hub"};
    private static final String[] PAYMENT_METHODS = {"TRANSFER", "CARD", "CASH", "TRANSFER", "CARD"};

    private final JdbcClient jdbc;
    private final DemoProperties properties;
    private final CategoryRepositoryPort categories;
    private final ProductRepositoryPort products;
    private final WarehouseRepositoryPort warehouses;
    private final CustomerRepositoryPort customers;
    private final SupplierRepositoryPort suppliers;
    private final InventoryRepositoryPort inventory;
    private final StockMovementRepositoryPort movements;
    private final SalesOrderRepositoryPort salesOrders;
    private final PurchaseOrderRepositoryPort purchaseOrders;
    private final InvoiceRepositoryPort invoices;

    public DemoDataSeeder(JdbcClient jdbc,
                          DemoProperties properties,
                          CategoryRepositoryPort categories,
                          ProductRepositoryPort products,
                          WarehouseRepositoryPort warehouses,
                          CustomerRepositoryPort customers,
                          SupplierRepositoryPort suppliers,
                          InventoryRepositoryPort inventory,
                          StockMovementRepositoryPort movements,
                          SalesOrderRepositoryPort salesOrders,
                          PurchaseOrderRepositoryPort purchaseOrders,
                          InvoiceRepositoryPort invoices) {
        this.jdbc = jdbc;
        this.properties = properties;
        this.categories = categories;
        this.products = products;
        this.warehouses = warehouses;
        this.customers = customers;
        this.suppliers = suppliers;
        this.inventory = inventory;
        this.movements = movements;
        this.salesOrders = salesOrders;
        this.purchaseOrders = purchaseOrders;
        this.invoices = invoices;
    }

    @Override
    public boolean isSeeded() {
        Long count = jdbc.sql("select count(*) from products").query(Long.class).single();
        return count != null && count > 0;
    }

    @Override
    public void clearBusinessData() {
        jdbc.sql("""
                TRUNCATE TABLE invoice_payments, invoice_lines, invoices,
                               sales_order_lines, sales_orders,
                               purchase_order_lines, purchase_orders,
                               stock_movements, inventory_items,
                               products, categories, customers, suppliers, warehouses, audit_log
                RESTART IDENTITY
                """).update();
    }

    @Override
    @Transactional
    public void seed() {
        Random random = new Random(42);

        seedDemoUsers();
        List<Category> categoryList = seedCategories();
        List<Product> productList = seedProducts(random, categoryList);
        List<Warehouse> warehouseList = seedWarehouses();
        seedCustomers(random);
        List<Supplier> supplierList = seedSuppliers();

        List<SalesOrder> orders = seedSalesHistory(random, productList, warehouseList);
        seedPurchases(random, productList, warehouseList, supplierList);
        seedInvoiceHistory(random, orders);
        rebuildInventory(random, productList, warehouseList, orders);
        seedAuditTrail();
    }

    private void seedDemoUsers() {
        record DemoUser(String username, String email, String firstName, String lastName, String hash, String role) {
        }
        List<DemoUser> demoUsers = List.of(
                new DemoUser("sales", "sales@erp.local", "Sofia", "Sales",
                        "$2y$10$G1J1xpjEB60/cuou2IjH6OtqOE4NN3l/tZe8PlR/wyteGaDDcQoY2", "SALES"),
                new DemoUser("warehouse", "warehouse@erp.local", "Walter", "Warehouse",
                        "$2y$10$FEzgHWf09K0nWD8N8JQkVe5mkSWaWT53dOTJQnA38ZjoBSxOzZSPa", "WAREHOUSE"),
                new DemoUser("accountant", "accountant@erp.local", "Ana", "Accountant",
                        "$2y$10$gK6b9Fps3FRgdcV15mvEqum.TcFTeX0vhyalTUAhzp4OV2Es23zhi", "ACCOUNTANT"),
                new DemoUser("viewer", "viewer@erp.local", "Vera", "Viewer",
                        "$2y$10$L8hC0kJVeRg.K0yDg2paW.2J4U3XqqDn9PaM3H3/yoziN1FL2uvfG", "VIEWER"));

        for (DemoUser user : demoUsers) {
            jdbc.sql("""
                    insert into users (username, email, password_hash, first_name, last_name, enabled, created_at, updated_at)
                    values (:username, :email, :hash, :firstName, :lastName, true, now(), now())
                    on conflict (username) do nothing
                    """)
                    .param("username", user.username())
                    .param("email", user.email())
                    .param("hash", user.hash())
                    .param("firstName", user.firstName())
                    .param("lastName", user.lastName())
                    .update();
            jdbc.sql("""
                    insert into user_roles (user_id, role_id)
                    select u.id, r.id from users u, roles r
                    where u.username = :username and r.name = :role
                    on conflict do nothing
                    """)
                    .param("username", user.username())
                    .param("role", user.role())
                    .update();
        }
    }

    private List<Category> seedCategories() {
        List<Category> result = new ArrayList<>();
        for (String name : CATEGORY_NAMES) {
            result.add(categories.save(Category.newCategory(name, "Demo category " + name, null)));
        }
        return result;
    }

    private List<Product> seedProducts(Random random, List<Category> categoryList) {
        List<Product> result = new ArrayList<>();
        int index = 0;
        for (Category category : categoryList) {
            for (int i = 0; i < 5; i++) {
                index++;
                String name = ADJECTIVES[random.nextInt(ADJECTIVES.length)] + " " + NOUNS[random.nextInt(NOUNS.length)];
                BigDecimal cost = BigDecimal.valueOf(5 + random.nextInt(300)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal price = cost.multiply(BigDecimal.valueOf(1.30 + random.nextDouble() * 0.5))
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal tax = switch (random.nextInt(3)) {
                    case 0 -> new BigDecimal("10.00");
                    case 1 -> new BigDecimal("4.00");
                    default -> new BigDecimal("21.00");
                };
                Product product = Product.newProduct("SKU-%04d".formatted(index), name,
                        "Demo product for " + category.getName(), category.getId(), UnitOfMeasure.UNIT,
                        price, cost, tax, random.nextInt(10) != 0);
                result.add(products.save(product));
            }
        }
        return result;
    }

    private List<Warehouse> seedWarehouses() {
        List<Warehouse> result = new ArrayList<>();
        for (int i = 0; i < WAREHOUSE_DATA.length; i += 2) {
            result.add(warehouses.save(Warehouse.newWarehouse(WAREHOUSE_DATA[i], WAREHOUSE_DATA[i + 1],
                    "Demo address " + (i / 2 + 1), true)));
        }
        return result;
    }

    private List<Customer> seedCustomers(Random random) {
        List<Customer> result = new ArrayList<>();
        int index = 0;
        for (String name : CUSTOMER_NAMES) {
            index++;
            String code = "CUST-%03d".formatted(index);
            result.add(customers.save(Customer.newCustomer(code, name, "B%08d".formatted(10000000 + index),
                    code.toLowerCase() + "@demo.example", "+34 600 %06d".formatted(index),
                    "Demo street " + index + ", Demo City", true)));
        }
        return result;
    }

    private List<Supplier> seedSuppliers() {
        List<Supplier> result = new ArrayList<>();
        int index = 0;
        for (String name : SUPPLIER_NAMES) {
            index++;
            String code = "SUP-%03d".formatted(index);
            result.add(suppliers.save(Supplier.newSupplier(code, name, "A%08d".formatted(20000000 + index),
                    code.toLowerCase() + "@demo.example", null, "Supplier address " + index, true)));
        }
        return result;
    }

    private List<SalesOrder> seedSalesHistory(Random random, List<Product> productList, List<Warehouse> warehouseList) {
        List<SalesOrder> result = new ArrayList<>();
        List<Customer> customerList = customers.search(null, true, 0, 100).content();
        LocalDate today = LocalDate.now();

        for (int monthsAgo = 11; monthsAgo >= 0; monthsAgo--) {
            int ordersThisMonth = 8 + random.nextInt(7);
            for (int i = 0; i < ordersThisMonth; i++) {
                Customer customer = customerList.get(random.nextInt(customerList.size()));
                LocalDate orderDate = today.minusMonths(monthsAgo)
                        .withDayOfMonth(1 + random.nextInt(27));
                List<SalesOrderLine> lines = new ArrayList<>();
                int lineCount = 1 + random.nextInt(3);
                for (int l = 0; l < lineCount; l++) {
                    Product product = productList.get(random.nextInt(productList.size()));
                    Warehouse warehouse = warehouseList.get(random.nextInt(warehouseList.size()));
                    lines.add(new SalesOrderLine(null, product.getId(), product.getSku(), product.getName(),
                            warehouse.getId(), 1 + random.nextInt(5), product.getSalePrice(),
                            BigDecimal.ZERO, product.getTaxRate()));
                }

                SalesOrderStatus status;
                if (monthsAgo >= 2) {
                    status = SalesOrderStatus.INVOICED;
                } else if (monthsAgo == 1) {
                    status = random.nextInt(4) == 0 ? SalesOrderStatus.DRAFT : SalesOrderStatus.CONFIRMED;
                } else {
                    status = random.nextInt(3) == 0 ? SalesOrderStatus.DRAFT : SalesOrderStatus.CONFIRMED;
                }

                SalesOrder order = new SalesOrder(null, nextSalesNumber(), customer.getId(), customer.getName(),
                        status, orderDate, "Demo order", lines, null, null, null);
                SalesOrder saved = salesOrders.save(order);
                result.add(saved);

                if (status == SalesOrderStatus.INVOICED) {
                    for (SalesOrderLine line : saved.getLines()) {
                        movements.save(StockMovement.of(line.getProductId(), line.getWarehouseId(),
                                MovementType.SALE, line.getQuantity(), "SALES_ORDER", saved.getId(), null));
                    }
                }
            }
        }
        return result;
    }

    private void seedInvoiceHistory(Random random, List<SalesOrder> orders) {
        int index = 0;
        for (SalesOrder order : orders) {
            if (order.getStatus() != SalesOrderStatus.INVOICED) {
                continue;
            }
            index++;
            LocalDate issueDate = order.getOrderDate().plusDays(1);
            LocalDate dueDate = issueDate.plusDays(30);
            int monthsAgo = (int) java.time.temporal.ChronoUnit.MONTHS.between(order.getOrderDate(), LocalDate.now());

            BigDecimal paid;
            InvoiceStatus status;
            if (monthsAgo >= 5) {
                paid = order.total();
                status = InvoiceStatus.PAID;
            } else if (monthsAgo >= 3) {
                if (random.nextInt(10) < 7) {
                    paid = order.total();
                    status = InvoiceStatus.PAID;
                } else {
                    paid = order.total().multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
                    status = InvoiceStatus.PARTIALLY_PAID;
                }
            } else if (random.nextBoolean()) {
                paid = order.total().multiply(new BigDecimal("0.4")).setScale(2, RoundingMode.HALF_UP);
                status = InvoiceStatus.PARTIALLY_PAID;
            } else {
                paid = BigDecimal.ZERO;
                status = InvoiceStatus.ISSUED;
            }

            List<InvoiceLine> lines = order.getLines().stream()
                    .map(line -> new InvoiceLine(null, line.getProductId(), line.getSku(), line.getProductName(),
                            line.getQuantity(), line.getUnitPrice(), line.getTaxRate()))
                    .toList();
            List<InvoicePayment> payments = new ArrayList<>();
            if (paid.signum() > 0) {
                payments.add(new InvoicePayment(null, paid, PAYMENT_METHODS[random.nextInt(PAYMENT_METHODS.length)],
                        "PAY-%05d".formatted(index), issueDate.plusDays(3 + random.nextInt(10)).atStartOfDay()
                                .atZone(java.time.ZoneOffset.UTC).toInstant()));
            }

            Invoice invoice = new Invoice(null, nextInvoiceNumber(), order.getId(), order.getNumber(),
                    order.getCustomerId(), order.getCustomerName(), status, issueDate, dueDate,
                    order.subtotal(), order.taxTotal(), order.total(), paid, lines, payments, null);
            invoices.save(invoice);
        }
    }

    private void seedPurchases(Random random, List<Product> productList, List<Warehouse> warehouseList,
                               List<Supplier> supplierList) {
        LocalDate today = LocalDate.now();
        for (int monthsAgo = 11; monthsAgo >= 0; monthsAgo--) {
            for (int i = 0; i < 3; i++) {
                Supplier supplier = supplierList.get(random.nextInt(supplierList.size()));
                Warehouse warehouse = warehouseList.get(random.nextInt(warehouseList.size()));
                LocalDate orderDate = today.minusMonths(monthsAgo).withDayOfMonth(1 + random.nextInt(27));

                List<PurchaseOrderLine> lines = new ArrayList<>();
                for (int l = 0; l < 1 + random.nextInt(3); l++) {
                    Product product = productList.get(random.nextInt(productList.size()));
                    lines.add(new PurchaseOrderLine(null, product.getId(), product.getSku(), product.getName(),
                            warehouse.getId(), 5 + random.nextInt(20), product.getCostPrice()));
                }
                PurchaseOrder order = new PurchaseOrder(null, nextPurchaseNumber(), supplier.getId(),
                        supplier.getName(), PurchaseOrderStatus.RECEIVED, orderDate, "Demo purchase", lines,
                        null, null, null);
                PurchaseOrder saved = purchaseOrders.save(order);
                for (PurchaseOrderLine line : saved.getLines()) {
                    movements.save(StockMovement.of(line.getProductId(), line.getWarehouseId(),
                            MovementType.PURCHASE_RECEIPT, line.getQuantity(), "PURCHASE_ORDER", saved.getId(), null));
                }
            }
        }
    }

    private void rebuildInventory(Random random, List<Product> productList, List<Warehouse> warehouseList,
                                  List<SalesOrder> orders) {
        List<StockMovement> allMovements = movements.search(null, null, null, 0, 10000).content();
        for (Product product : productList) {
            for (Warehouse warehouse : warehouseList) {
                int quantity = 10 + random.nextInt(60);
                for (StockMovement movement : allMovements) {
                    if (!movement.productId().equals(product.getId())
                            || !movement.warehouseId().equals(warehouse.getId())) {
                        continue;
                    }
                    quantity += switch (movement.type()) {
                        case SALE -> -movement.quantity();
                        case PURCHASE_RECEIPT -> movement.quantity();
                        default -> 0;
                    };
                }
                if (quantity <= 0) {
                    continue;
                }
                InventoryItem item = new InventoryItem(null, product.getId(), product.getSku(), product.getName(),
                        warehouse.getId(), warehouse.getCode(), quantity, 0, null, null);
                inventory.save(item);
            }
        }
    }

    private void seedAuditTrail() {
        jdbc.sql("""
                insert into audit_log (entity_type, action, entity_ref, username, user_id, after_json, created_at)
                select 'SalesOrder', 'SALES_ORDER_CREATE', so.id::text, 'sales', u.id,
                       jsonb_build_object('number', so.number, 'status', so.status), so.created_at
                from sales_orders so, users u
                where u.username = 'sales'
                order by so.id desc
                limit 5
                """).update();
        jdbc.sql("""
                insert into audit_log (entity_type, action, entity_ref, username, user_id, after_json, created_at)
                select 'Invoice', 'INVOICE_CREATE', i.id::text, 'accountant', u.id,
                       jsonb_build_object('number', i.number, 'total', i.total), i.created_at
                from invoices i, users u
                where u.username = 'accountant'
                order by i.id desc
                limit 5
                """).update();
    }

    private String nextSalesNumber() {
        return "SO-%d-%05d".formatted(LocalDate.now().getYear(), salesOrders.nextOrderNumber());
    }

    private String nextPurchaseNumber() {
        return "PO-%d-%05d".formatted(LocalDate.now().getYear(), purchaseOrders.nextOrderNumber());
    }

    private String nextInvoiceNumber() {
        return "INV-%d-%05d".formatted(LocalDate.now().getYear(), invoices.nextInvoiceNumber());
    }
}
