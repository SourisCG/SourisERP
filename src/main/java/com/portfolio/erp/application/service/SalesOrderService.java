package com.portfolio.erp.application.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.erp.domain.audit.Audited;
import com.portfolio.erp.domain.exception.ResourceNotFoundException;
import com.portfolio.erp.domain.model.Customer;
import com.portfolio.erp.domain.model.PageResult;
import com.portfolio.erp.domain.model.Product;
import com.portfolio.erp.domain.model.SalesOrder;
import com.portfolio.erp.domain.model.SalesOrderLine;
import com.portfolio.erp.domain.model.SalesOrderStatus;
import com.portfolio.erp.domain.ports.in.InventoryUseCase;
import com.portfolio.erp.domain.ports.in.SalesOrderUseCase;
import com.portfolio.erp.domain.ports.out.CustomerRepositoryPort;
import com.portfolio.erp.domain.ports.out.ProductRepositoryPort;
import com.portfolio.erp.domain.ports.out.SalesOrderRepositoryPort;
import com.portfolio.erp.domain.ports.out.WarehouseRepositoryPort;

@Service
@Transactional(readOnly = true)
public class SalesOrderService implements SalesOrderUseCase {

    private final SalesOrderRepositoryPort orders;
    private final CustomerRepositoryPort customers;
    private final ProductRepositoryPort products;
    private final WarehouseRepositoryPort warehouses;
    private final InventoryUseCase inventory;

    public SalesOrderService(SalesOrderRepositoryPort orders,
                             CustomerRepositoryPort customers,
                             ProductRepositoryPort products,
                             WarehouseRepositoryPort warehouses,
                             InventoryUseCase inventory) {
        this.orders = orders;
        this.customers = customers;
        this.products = products;
        this.warehouses = warehouses;
        this.inventory = inventory;
    }

    @Override
    public PageResult<SalesOrder> list(String search, SalesOrderStatus status, Long customerId, int page, int size) {
        return orders.search(search, status, customerId, page, size);
    }

    @Override
    public SalesOrder get(Long id) {
        return requireOrder(id);
    }

    @Override
    @Transactional
    @Audited(action = "SALES_ORDER_CREATE", entityType = "SalesOrder")
    public SalesOrder create(SalesOrderCommand command) {
        Customer customer = requireCustomer(command.customerId());
        SalesOrder order = SalesOrder.draft(nextNumber(), customer.getId(), command.orderDate(),
                command.notes(), buildLines(command.lines()));
        return orders.save(order);
    }

    @Override
    @Transactional
    public SalesOrder update(Long id, SalesOrderCommand command) {
        SalesOrder order = requireOrder(id);
        requireCustomer(command.customerId());
        order.updateHeader(command.orderDate(), command.notes());
        order.replaceLines(buildLines(command.lines()));
        return orders.save(order);
    }

    @Override
    @Transactional
    @Audited(action = "SALES_ORDER_CONFIRM", entityType = "SalesOrder")
    public SalesOrder confirm(Long id) {
        SalesOrder order = requireOrder(id);
        order.confirm();
        order.getLines().forEach(line -> inventory.reserve(
                line.getProductId(), line.getWarehouseId(), line.getQuantity(), "SALES_ORDER", id));
        return orders.save(order);
    }

    @Override
    @Transactional
    @Audited(action = "SALES_ORDER_CANCEL", entityType = "SalesOrder")
    public SalesOrder cancel(Long id) {
        SalesOrder order = requireOrder(id);
        boolean releaseStock = order.getStatus() == SalesOrderStatus.CONFIRMED;
        order.cancel();
        if (releaseStock) {
            order.getLines().forEach(line -> inventory.release(
                    line.getProductId(), line.getWarehouseId(), line.getQuantity(), "SALES_ORDER", id));
        }
        return orders.save(order);
    }

    @Override
    @Transactional
    public SalesOrder markInvoiced(Long id) {
        SalesOrder order = requireOrder(id);
        order.markInvoiced();
        order.getLines().forEach(line -> inventory.ship(
                line.getProductId(), line.getWarehouseId(), line.getQuantity(), "SALES_ORDER", id));
        return orders.save(order);
    }

    private List<SalesOrderLine> buildLines(List<LineCommand> commands) {
        List<SalesOrderLine> lines = new ArrayList<>();
        for (LineCommand command : commands) {
            Product product = products.findById(command.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("error.product.notFound"));
            warehouses.findById(command.warehouseId())
                    .orElseThrow(() -> new ResourceNotFoundException("error.warehouse.notFound"));
            BigDecimal unitPrice = command.unitPrice() != null ? command.unitPrice() : product.getSalePrice();
            BigDecimal discount = command.discount() != null ? command.discount() : BigDecimal.ZERO;
            lines.add(new SalesOrderLine(null, product.getId(), product.getSku(), product.getName(),
                    command.warehouseId(), command.quantity(), unitPrice, discount, product.getTaxRate()));
        }
        return lines;
    }

    private SalesOrder requireOrder(Long id) {
        return orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("error.salesOrder.notFound"));
    }

    private Customer requireCustomer(Long customerId) {
        return customers.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("error.customer.notFound"));
    }

    private String nextNumber() {
        return "SO-%d-%05d".formatted(LocalDate.now().getYear(), orders.nextOrderNumber());
    }
}
